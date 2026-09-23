package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class MaterialItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val defaultPrice: Double = 0.0,
    val unit: String = "Adet",
    val category: String = ""
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("defaultPrice", defaultPrice)
        put("unit", unit)
        put("category", category)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): MaterialItem = MaterialItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            defaultPrice = obj.optDouble("defaultPrice", 0.0),
            unit = obj.optString("unit", "Adet"),
            category = obj.optString("category", "")
        )
    }
}

class MaterialCatalogManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _materialsFlow = MutableStateFlow<List<MaterialItem>>(emptyList())
    val materialsFlow: StateFlow<List<MaterialItem>> = _materialsFlow.asStateFlow()

    init {
        loadMaterials()
    }

    private fun loadMaterials() {
        val jsonStr = prefs.getString(KEY_MATERIALS, null)
        val list = mutableListOf<MaterialItem>()
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(MaterialItem.fromJsonObject(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // İlk kurulumda örnek elektrik / aydınlatma / kargo malzemeleri
        if (list.isEmpty()) {
            val defaults = listOf(
                MaterialItem(name = "Gold Kasa Led Aplik", defaultPrice = 180.0),
                MaterialItem(name = "Siyah Kasa Led Aplik", defaultPrice = 170.0),
                MaterialItem(name = "100 cm Wallwasher", defaultPrice = 280.0),
                MaterialItem(name = "50 cm Wallwasher", defaultPrice = 160.0),
                MaterialItem(name = "3 Çipli Şerit Led (Metre)", defaultPrice = 35.0),
                MaterialItem(name = "12V 10A Led Trafo", defaultPrice = 210.0),
                MaterialItem(name = "12V 20A Slim Led Trafo", defaultPrice = 320.0),
                MaterialItem(name = "Ray Spot Armatür", defaultPrice = 125.0)
            )
            list.addAll(defaults)
            saveListToPrefs(list)
        }

        _materialsFlow.value = list
    }

    fun addOrUpdateMaterial(name: String, price: Double = 0.0, unit: String = "Adet", category: String = "") {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val current = _materialsFlow.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.name.equals(trimmed, ignoreCase = true) }
        if (existingIndex >= 0) {
            val old = current[existingIndex]
            current[existingIndex] = old.copy(
                defaultPrice = if (price > 0) price else old.defaultPrice,
                unit = if (unit.isNotBlank()) unit else old.unit,
                category = if (category.isNotBlank()) category else old.category
            )
        } else {
            current.add(0, MaterialItem(name = trimmed, defaultPrice = price, unit = unit, category = category))
        }
        saveListToPrefs(current)
        _materialsFlow.value = current
    }

    fun deleteMaterial(id: String) {
        val current = _materialsFlow.value.filter { it.id != id }
        saveListToPrefs(current)
        _materialsFlow.value = current
    }

    private fun saveListToPrefs(list: List<MaterialItem>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJsonObject()) }
        prefs.edit().putString(KEY_MATERIALS, array.toString()).apply()
    }

    /**
     * Kullanıcı yazarken hem kayıtlı katalogdan hem de geçmiş borç / alacak kalemlerinden
     * otomatik tamamlama önerilerini harmanlayıp döner.
     */
    fun getSuggestions(
        query: String,
        debtItems: List<DebtItem> = emptyList(),
        receivableItems: List<ReceivableItem> = emptyList()
    ): List<MaterialItem> {
        val queryTrimmed = query.trim().lowercase()
        val map = linkedMapOf<String, MaterialItem>()

        // 1. Önce kayıtlı katalog malzemeleri
        for (item in _materialsFlow.value) {
            val key = item.name.trim().lowercase()
            if (queryTrimmed.isEmpty() || key.contains(queryTrimmed)) {
                map[key] = item
            }
        }

        // 2. Geçmiş borç kalemleri
        for (item in debtItems) {
            val key = item.title.trim().lowercase()
            if (key.isNotBlank() && (queryTrimmed.isEmpty() || key.contains(queryTrimmed))) {
                if (!map.containsKey(key)) {
                    map[key] = MaterialItem(name = item.title.trim(), defaultPrice = item.amount)
                }
            }
        }

        // 3. Geçmiş alacak kalemleri
        for (item in receivableItems) {
            val key = item.title.trim().lowercase()
            if (key.isNotBlank() && (queryTrimmed.isEmpty() || key.contains(queryTrimmed))) {
                if (!map.containsKey(key)) {
                    map[key] = MaterialItem(name = item.title.trim(), defaultPrice = item.amount)
                }
            }
        }

        return map.values.toList().take(8)
    }

    companion object {
        private const val PREFS_NAME = "material_catalog_prefs"
        private const val KEY_MATERIALS = "saved_materials_json"

        @Volatile
        private var instance: MaterialCatalogManager? = null

        fun getInstance(context: Context): MaterialCatalogManager {
            return instance ?: synchronized(this) {
                instance ?: MaterialCatalogManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
