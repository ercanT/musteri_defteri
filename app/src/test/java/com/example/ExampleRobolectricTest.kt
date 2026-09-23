package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CustomerEntity
import com.example.data.CustomerStatus
import com.example.data.DebtEntity
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Müşteri Defteri", appName)
  }

  @Test
  fun `verify kdv calculations - 1000 TL with 20 percent VAT yields 833_33 TL without VAT`() {
    val kdvDahil = 1000.0
    val kdvHaric = CurrencyUtils.calculateKdvHaric(kdvDahil)
    val kdvTutari = CurrencyUtils.calculateKdvTutari(kdvDahil)

    assertEquals(833.333, kdvHaric, 0.01)
    assertEquals(166.666, kdvTutari, 0.01)
    assertEquals("1.000,00 ₺", CurrencyUtils.formatCurrency(kdvDahil))
    assertEquals("833,33 ₺", CurrencyUtils.formatCurrency(kdvHaric))
  }

  @Test
  fun `verify customer amount persistence in database`() = runBlocking {
    val dao = db.customerDao()
    val id = dao.insertCustomer(
      CustomerEntity(
        fullName = "Fatih Sultan",
        phoneNumber = "05321112233",
        address = "Fatih/İstanbul",
        extraNotes = "Özel sipariş",
        status = CustomerStatus.PARASI_GELDI.name,
        amount = 1000.0
      )
    ).toInt()

    val saved = dao.getCustomerById(id)
    assertTrue(saved != null)
    assertEquals(1000.0, saved!!.amount, 0.001)
  }

  @Test
  fun `verify status sorting order - parasi geldi first, hazirlaniyor second, tamamlandi last`() = runBlocking {
    val dao = db.customerDao()

    // Insert out of desired order
    dao.insertCustomer(
      CustomerEntity(
        fullName = "Tamamlanan Müşteri",
        phoneNumber = "05001112233",
        address = "Ankara",
        extraNotes = "Teslim edildi",
        status = CustomerStatus.TAMAMLANDI.name
      )
    )
    dao.insertCustomer(
      CustomerEntity(
        fullName = "Hazırlanan Müşteri",
        phoneNumber = "05002223344",
        address = "İzmir",
        extraNotes = "Üretimde",
        status = CustomerStatus.HAZIRLANIYOR.name
      )
    )
    dao.insertCustomer(
      CustomerEntity(
        fullName = "Ödeme Yapan Müşteri",
        phoneNumber = "05003334455",
        address = "İstanbul",
        extraNotes = "Havale alındı",
        status = CustomerStatus.PARASI_GELDI.name
      )
    )

    val list = dao.getAllCustomers().first()
    assertEquals(3, list.size)
    // 1st: PARASI_GELDI
    assertEquals(CustomerStatus.PARASI_GELDI.name, list[0].status)
    assertEquals("Ödeme Yapan Müşteri", list[0].fullName)
    // 2nd: HAZIRLANIYOR
    assertEquals(CustomerStatus.HAZIRLANIYOR.name, list[1].status)
    assertEquals("Hazırlanan Müşteri", list[1].fullName)
    // 3rd: TAMAMLANDI
    assertEquals(CustomerStatus.TAMAMLANDI.name, list[2].status)
    assertEquals("Tamamlanan Müşteri", list[2].fullName)
  }

  @Test
  fun `verify favorite customer comes first above all statuses`() = runBlocking {
    val dao = db.customerDao()

    // 1. Regular customer with PARASI_GELDI (not favorite)
    dao.insertCustomer(
      CustomerEntity(
        fullName = "Normal Parası Geldi Müşteri",
        phoneNumber = "05001111111",
        address = "İst",
        extraNotes = "",
        status = CustomerStatus.PARASI_GELDI.name,
        isFavorite = false
      )
    )

    // 2. Favorite customer with TAMAMLANDI status (completed, but marked favorite)
    dao.insertCustomer(
      CustomerEntity(
        fullName = "Önemli Favori Müşteri",
        phoneNumber = "05002222222",
        address = "Ank",
        extraNotes = "",
        status = CustomerStatus.TAMAMLANDI.name,
        isFavorite = true
      )
    )

    val list = dao.getAllCustomers().first()
    assertEquals(2, list.size)
    // The favorite one must be at the very top (index 0) even though its status is TAMAMLANDI
    assertEquals("Önemli Favori Müşteri", list[0].fullName)
    assertTrue(list[0].isFavorite)
    assertEquals("Normal Parası Geldi Müşteri", list[1].fullName)

    // Test toggling favorite
    dao.updateFavorite(list[1].id, true)
    dao.updateFavorite(list[0].id, false)
    val updatedList = dao.getAllCustomers().first()
    assertEquals("Normal Parası Geldi Müşteri", updatedList[0].fullName)
    assertTrue(updatedList[0].isFavorite)
  }

  @Test
  fun `verify customer deletion by primary key`() = runBlocking {
    val dao = db.customerDao()
    val id = dao.insertCustomer(
      CustomerEntity(
        fullName = "Silinecek Müşteri",
        phoneNumber = "05550001122",
        address = "Bursa",
        extraNotes = "Eski kayıt",
        status = CustomerStatus.HAZIRLANIYOR.name
      )
    ).toInt()

    val beforeDelete = dao.getAllCustomers().first()
    assertTrue(beforeDelete.any { it.id == id })

    dao.deleteCustomerById(id)

    val afterDelete = dao.getAllCustomers().first()
    assertTrue(afterDelete.none { it.id == id })
  }

  @Test
  fun `verify demo seeding is disabled and debts are created manually`() = runBlocking {
    val debtDao = db.debtDao()
    val repo = com.example.data.DebtRepository(debtDao)

    // Verify seed does not auto-insert demo data
    repo.seedSampleIfEmpty()
    val debts = debtDao.getAllDebts().first()
    assertTrue(debts.isEmpty())
  }

  @Test
  fun `verify debt items calculation and payment deduction - manual debt creation`() = runBlocking {
    val debtDao = db.debtDao()
    val repo = com.example.data.DebtRepository(debtDao)

    // Manually insert debt: Zilci Mustafa with 250 TL + 70 TL = 320 TL and extra notes
    val sampleItems = listOf(
      com.example.data.DebtItem(title = "100 cm wallwasher", amount = 250.0, quantity = 1),
      com.example.data.DebtItem(title = "60 cm wallwasher", amount = 70.0, quantity = 1)
    )
    val debtId = repo.insertDebt(
      com.example.data.DebtEntity(
        personOrCompany = "Zilci Mustafa",
        phoneNumber = "05321112233",
        notes = "Dükkandan teslim alındı, acil malzeme",
        itemsJson = com.example.data.DebtEntity.itemsToJson(sampleItems),
        paymentsJson = com.example.data.DebtEntity.paymentsToJson(emptyList())
      )
    )

    val debts = debtDao.getAllDebts().first()
    assertEquals(1, debts.size)
    val zilci = debts[0]
    assertEquals("Zilci Mustafa", zilci.personOrCompany)
    assertEquals("Dükkandan teslim alındı, acil malzeme", zilci.notes)

    val items = zilci.getItems()
    assertEquals(2, items.size)
    assertEquals(250.0, items[0].amount, 0.001)
    assertEquals(70.0, items[1].amount, 0.001)

    val totalAmount = zilci.calculateTotalAmount()
    assertEquals(320.0, totalAmount, 0.001)
    assertEquals(320.0, zilci.calculateRemainingBalance(), 0.001)
    assertTrue(!zilci.isFullyPaid())

    // Make payment: 100 TL
    val paymentResult = repo.addPaymentToDebt(
      zilci.id,
      com.example.data.DebtPayment(amount = 100.0, note = "EFT yapıldı")
    )
    assertTrue(paymentResult)

    val updatedDebt = debtDao.getDebtById(zilci.id)!!
    assertEquals(100.0, updatedDebt.calculateTotalPaid(), 0.001)
    // 320 - 100 = 220 TL remaining!
    assertEquals(220.0, updatedDebt.calculateRemainingBalance(), 0.001)
    assertTrue(!updatedDebt.isFullyPaid())

    // Pay remainder: 220 TL
    repo.addPaymentToDebt(
      zilci.id,
      com.example.data.DebtPayment(amount = 220.0, note = "Kalan elden ödendi")
    )

    val finalDebt = debtDao.getDebtById(zilci.id)!!
    assertEquals(320.0, finalDebt.calculateTotalPaid(), 0.001)
    assertEquals(0.0, finalDebt.calculateRemainingBalance(), 0.001)
    assertTrue(finalDebt.isFullyPaid())
  }

  @Test
  fun `verify whatsapp payment notification message formatting with explanation and remaining balance`() {
    // Case 1: Partial payment with explanation
    val msgWithNote = com.example.util.WhatsAppUtils.buildPaymentNotificationMessage(
      personOrCompany = "Zilci Mustafa",
      paidAmount = 100.0,
      paymentNote = "EFT ile gönderildi",
      remainingBalance = 220.0
    )

    assertTrue(msgWithNote.contains("Zilci Mustafa"))
    assertTrue(msgWithNote.contains("100"))
    assertTrue(msgWithNote.contains("EFT ile gönderildi"))
    assertTrue(msgWithNote.contains("220"))
    assertTrue(msgWithNote.contains("Kalan Bakiyemiz"))

    // Case 2: Full payment with zero remaining balance
    val msgFull = com.example.util.WhatsAppUtils.buildPaymentNotificationMessage(
      personOrCompany = "Zilci Mustafa",
      paidAmount = 220.0,
      paymentNote = "",
      remainingBalance = 0.0
    )

    assertTrue(msgFull.contains("Zilci Mustafa"))
    assertTrue(msgFull.contains("220"))
    assertTrue(!msgFull.contains("Ödeme Açıklaması"))
    assertTrue(msgFull.contains("0,00") || msgFull.contains("0.00"))
    assertTrue(msgFull.contains("kapanmıştır") || msgFull.contains("ödenmiştir"))

    // Case 3: Phone number cleaning
    assertEquals("905321234567", com.example.util.WhatsAppUtils.cleanPhoneNumber("0532 123 45 67"))
    assertEquals("905321234567", com.example.util.WhatsAppUtils.cleanPhoneNumber("+90 532 123 45 67"))
    assertEquals("905321234567", com.example.util.WhatsAppUtils.cleanPhoneNumber("5321234567"))
  }

  @Test
  fun `verify debt summary message includes extra notes field and items`() {
    val items = listOf(
      com.example.data.DebtItem(title = "100 cm wallwasher", amount = 250.0, quantity = 1),
      com.example.data.DebtItem(title = "60 cm wallwasher", amount = 70.0, quantity = 2)
    )
    val msg = com.example.util.WhatsAppUtils.buildDebtSummaryMessage(
      personOrCompany = "Zilci Mustafa",
      totalItemsAmount = 390.0,
      totalPaidAmount = 100.0,
      remainingBalance = 290.0,
      notes = "Haftaya cuma kalan teslim edilecek",
      items = items
    )

    assertTrue(msg.contains("Zilci Mustafa"))
    assertTrue(msg.contains("Ekstra Not / Açıklama"))
    assertTrue(msg.contains("Haftaya cuma kalan teslim edilecek"))
    assertTrue(msg.contains("100 cm wallwasher"))
    assertTrue(msg.contains("60 cm wallwasher"))
    assertTrue(msg.contains("390"))
    assertTrue(msg.contains("290"))
  }

  @Test
  fun `verify debt note only whatsapp message`() {
    val noteMsg = com.example.util.WhatsAppUtils.buildDebtNoteOnlyMessage(
      personOrCompany = "Zilci Mustafa",
      notes = "Sipariş no: 84920 depodan çıkış yapıldı.",
      remainingBalance = 150.0
    )

    assertTrue(noteMsg.contains("Zilci Mustafa"))
    assertTrue(noteMsg.contains("Sipariş no: 84920 depodan çıkış yapıldı."))
    assertTrue(noteMsg.contains("150"))
  }

  @Test
  fun `verify customer record ercan toka with phone and status`() = runBlocking {
    val dao = db.customerDao()
    val ercan = CustomerEntity(
      fullName = "Ercan Toka",
      phoneNumber = "05321112233",
      address = "Organize Sanayi Bölgesi",
      extraNotes = "Öncelikli kargo",
      status = CustomerStatus.PARASI_GELDI.name,
      amount = 2500.0,
      isFavorite = true
    )
    val id = dao.insertCustomer(ercan)
    val fetched = dao.getCustomerById(id.toInt())
    org.junit.Assert.assertNotNull(fetched)
    assertEquals("Ercan Toka", fetched?.fullName)
    assertEquals("05321112233", fetched?.phoneNumber)
    assertTrue(fetched!!.isFavorite)
    assertEquals(CustomerStatus.PARASI_GELDI.name, fetched.status)
  }

  @Test
  fun `verify debt balance calculation and currency format for 4000 TL`() {
    val debt = DebtEntity(
      id = 1,
      personOrCompany = "Örnek Tedarikçi Ltd.",
      phoneNumber = "05551234567",
      itemsJson = """[{"id":"item-1","title":"Profil Malzeme","amount":4000.0,"quantity":1}]""",
      paymentsJson = "[]"
    )
    assertEquals(4000.0, debt.calculateTotalAmount(), 0.01)
    assertEquals(0.0, debt.calculateTotalPaid(), 0.01)
    assertEquals(4000.0, debt.calculateRemainingBalance(), 0.01)
    assertFalse(debt.isFullyPaid())

    val formatted = com.example.util.CurrencyUtils.formatCurrency(debt.calculateRemainingBalance())
    assertTrue(formatted.contains("4.000") || formatted.contains("4000"))
    assertTrue(formatted.contains("₺") || formatted.contains("TL"))
  }

  @Test
  fun `verify editing a wrong payment updates total paid and remaining balance`() = runBlocking {
    val initialDebt = DebtEntity(
      id = 10,
      personOrCompany = "Ahmet Usta",
      phoneNumber = "05330000000",
      itemsJson = """[{"id":"it-1","title":"Kablo","amount":1000.0,"quantity":1}]""",
      paymentsJson = """[{"id":"pay-1","amount":100.0,"note":"Yanlış girildi"}]"""
    )
    db.debtDao().insertDebt(initialDebt)

    val debtRepo = com.example.data.DebtRepository(db.debtDao())
    val loaded = debtRepo.getDebtById(10)
    org.junit.Assert.assertNotNull(loaded)
    assertEquals(100.0, loaded!!.calculateTotalPaid(), 0.01)
    assertEquals(900.0, loaded.calculateRemainingBalance(), 0.01)

    // User corrects payment from 100 TL to 400 TL
    val updatedPayment = com.example.data.DebtPayment(
      id = "pay-1",
      amount = 400.0,
      note = "Düzeltildi: Havale yapıldı"
    )
    val success = debtRepo.updatePaymentInDebt(10, updatedPayment)
    assertTrue(success)

    val refreshed = debtRepo.getDebtById(10)
    org.junit.Assert.assertNotNull(refreshed)
    assertEquals(400.0, refreshed!!.calculateTotalPaid(), 0.01)
    assertEquals(600.0, refreshed.calculateRemainingBalance(), 0.01)
  }

  @Test
  fun `verify active filters default to Parasi Geldi for Cargo and Acik Borclar for Debts`() {
    val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val custRepo = com.example.data.CustomerRepository(db.customerDao())
    val custVm = com.example.ui.CustomerViewModel(app, custRepo)

    // Cargo screen defaults to PARASI_GELDI (not Tümü)
    assertEquals(CustomerStatus.PARASI_GELDI, custVm.selectedStatusFilter.value)

    val debtRepo = com.example.data.DebtRepository(db.debtDao())
    val debtVm = com.example.ui.DebtViewModel(app, debtRepo)

    // Debt screen defaults to UNPAID (Açık Borçlar)
    assertEquals(com.example.ui.DebtFilter.UNPAID, debtVm.selectedFilter.value)
  }

  @Test
  fun `verify customer cargo whatsapp message includes name address and extra notes`() {
    val message = com.example.util.WhatsAppUtils.buildCustomerCargoMessage(
      fullName = "Ercan Toka",
      address = "Atatürk Mah. Cumhuriyet Cad. No:15 Kadıköy / İstanbul",
      extraNotes = "Kırılabilir cam kargo, kapıda teslim ediniz"
    )

    // Verify all requested components are included
    assertTrue(message.contains("Ercan Toka"))
    assertTrue(message.contains("Atatürk Mah. Cumhuriyet Cad. No:15 Kadıköy / İstanbul"))
    assertTrue(message.contains("Kırılabilir cam kargo, kapıda teslim ediniz"))
  }

  @Test
  fun `verify receivable whatsapp message matches user requested template for ercan toka and lion ticaret`() {
    // Test with Ercan Toka
    val msgErcan = com.example.util.WhatsAppUtils.buildReceivableWhatsAppMessage(
      personOrCompany = "Ercan Toka",
      remainingBalance = 250.0
    )
    assertTrue(msgErcan.contains("Ercan Toka"))
    assertTrue(msgErcan.contains("250"))
    assertTrue(msgErcan.contains("gün içinde ödeme yapılıp tarafımıza bilgi veriniz", ignoreCase = true))
    assertTrue(msgErcan.contains("iyi günler dilerim", ignoreCase = true))

    // Test with Lion Ticaret
    val msgLion = com.example.util.WhatsAppUtils.buildReceivableWhatsAppMessage(
      personOrCompany = "Lion Ticaret",
      remainingBalance = 250.0
    )
    assertTrue(msgLion.contains("Lion Ticaret"))
    assertTrue(msgLion.contains("250"))
    assertTrue(msgLion.contains("gün içinde ödeme yapılıp tarafımıza bilgi veriniz", ignoreCase = true))
    assertTrue(msgLion.contains("iyi günler dilerim", ignoreCase = true))
  }

  @Test
  fun `verify receivable entity calculation and repository operations`() = runBlocking {
    val repo = com.example.data.ReceivableRepository(db.receivableDao())

    val id = repo.insertReceivable(
      com.example.data.ReceivableEntity(
        personOrCompany = "Lion Ticaret",
        phoneNumber = "05321112233",
        notes = "Toptan alım"
      )
    )

    // Add item: 250 TL
    val added = repo.addItemToReceivable(
      id,
      com.example.data.ReceivableItem(title = "Koli", amount = 250.0, quantity = 1)
    )
    assertTrue(added)

    val rec = repo.getReceivableById(id)
    org.junit.Assert.assertNotNull(rec)
    assertEquals(250.0, rec!!.calculateTotalAmount(), 0.01)
    assertEquals(250.0, rec.calculateRemainingBalance(), 0.01)
    assertFalse(rec.isFullyPaid())

    // Add tahsilat / payment: 250 TL
    val paid = repo.addPaymentToReceivable(
      id,
      com.example.data.ReceivablePayment(amount = 250.0, note = "EFT ile ödendi")
    )
    assertTrue(paid)

    val recUpdated = repo.getReceivableById(id)
    org.junit.Assert.assertNotNull(recUpdated)
    assertEquals(250.0, recUpdated!!.calculateTotalPaid(), 0.01)
    assertEquals(0.0, recUpdated.calculateRemainingBalance(), 0.01)
    assertTrue(recUpdated.isFullyPaid())
  }

  @Test
  fun `verify receivable viewmodel default filter is UNPAID`() {
    val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val repo = com.example.data.ReceivableRepository(db.receivableDao())
    val vm = com.example.ui.ReceivableViewModel(app, repo)

    // Default filter should be UNPAID (Açık Alacaklar)
    assertEquals(com.example.ui.ReceivableFilter.UNPAID, vm.selectedFilter.value)
  }
}

