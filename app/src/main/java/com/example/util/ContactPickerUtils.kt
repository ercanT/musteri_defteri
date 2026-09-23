package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PickedContact(
    val name: String,
    val phoneNumber: String
)

object ContactPickerUtils {

    fun createPickContactIntent(): Intent {
        return Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
    }

    /**
     * Queries the specific contact item returned by Intent.ACTION_PICK
     * and extracts the contact's name and selected phone number.
     */
    fun extractContact(context: Context, contactUri: Uri): PickedContact? {
        var name: String? = null
        var phone: String? = null

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (nameIdx != -1) {
                        name = cursor.getString(nameIdx)
                    }
                    if (phoneIdx != -1) {
                        phone = cursor.getString(phoneIdx)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (name.isNullOrBlank() && phone.isNullOrBlank()) {
            return null
        }

        return PickedContact(
            name = name.orEmpty().trim(),
            phoneNumber = formatPhoneNumberDisplay(phone.orEmpty())
        )
    }

    /**
     * Cleans phone number display into a standard format (e.g. 0532 123 45 67)
     */
    private fun formatPhoneNumberDisplay(raw: String): String {
        val trimmed = raw.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }
        return when {
            digitsOnly.startsWith("90") && digitsOnly.length == 12 -> {
                val sub = digitsOnly.substring(2)
                "0${sub.take(3)} ${sub.drop(3).take(3)} ${sub.drop(6).take(2)} ${sub.drop(8)}"
            }
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> {
                val sub = digitsOnly.substring(1)
                "0${sub.take(3)} ${sub.drop(3).take(3)} ${sub.drop(6).take(2)} ${sub.drop(8)}"
            }
            digitsOnly.length == 10 && digitsOnly.startsWith("5") -> {
                "0${digitsOnly.take(3)} ${digitsOnly.drop(3).take(3)} ${digitsOnly.drop(6).take(2)} ${digitsOnly.drop(8)}"
            }
            else -> trimmed
        }
    }
}

/**
 * Reusable Compose component button that launches the system Contact Picker
 * and returns the picked contact's name and phone number.
 */
@Composable
fun PickContactButton(
    onContactPicked: (name: String, phone: String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Rehberden Kişi / Firma Seç"
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val contact = ContactPickerUtils.extractContact(context, uri)
                if (contact != null) {
                    onContactPicked(contact.name, contact.phoneNumber)
                    Toast.makeText(
                        context,
                        "Rehberden aktarıldı: ${contact.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Kişi bilgisi okunamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Surface(
        onClick = {
            try {
                launcher.launch(ContactPickerUtils.createPickContactIntent())
            } catch (e: Exception) {
                Toast.makeText(context, "Telefon rehberi açılamadı", Toast.LENGTH_SHORT).show()
            }
        },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier.testTag("btn_pick_from_contacts")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ContactPhone,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
