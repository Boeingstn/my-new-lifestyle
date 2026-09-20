package com.suthinee.calorietracker.domain.nlp

private data class DishDefinition(
    val keyword: String,
    val name: String,
    val baseCalories: Int,
    val hasPortion: Boolean
)

private data class AddonDefinition(
    val keyword: String,
    val name: String,
    val calories: Int
)

/**
 * Rule-based estimator for free-text meal descriptions (Thai and English).
 *
 * No AI/ML model or network call is used: it matches known dish keywords, adds
 * calories for detected add-ons (fried egg, extra rice, etc.), and only asks a
 * follow-up question ("how much rice?") when portion size actually changes the
 * estimate. Every question can be skipped.
 */
object FoodDescriptionParser {

    // Ordered most-specific first so e.g. "ข้าวผัดกะเพรา" doesn't get shadowed by a shorter generic keyword.
    private val dishes = listOf(
        DishDefinition("ข้าวกะเพรา", "ข้าวกะเพรา (Pad Kra Pao rice)", 500, hasPortion = true),
        DishDefinition("กะเพรา", "กะเพรา (Pad Kra Pao)", 450, hasPortion = true),
        DishDefinition("ข้าวผัดปู", "ข้าวผัดปู (crab fried rice)", 600, hasPortion = true),
        DishDefinition("ข้าวผัด", "ข้าวผัด (fried rice)", 550, hasPortion = true),
        DishDefinition("ข้าวมันไก่", "ข้าวมันไก่ (Khao Man Gai)", 600, hasPortion = true),
        DishDefinition("ข้าวหมูแดง", "ข้าวหมูแดง (red pork rice)", 520, hasPortion = true),
        DishDefinition("ข้าวหมูกรอบ", "ข้าวหมูกรอบ (crispy pork rice)", 580, hasPortion = true),
        DishDefinition("แกงเขียวหวาน", "แกงเขียวหวาน (green curry with rice)", 520, hasPortion = true),
        DishDefinition("แกงเผ็ด", "แกงเผ็ด (red curry with rice)", 500, hasPortion = true),
        DishDefinition("ผัดไทย", "ผัดไทย (Pad Thai)", 500, hasPortion = true),
        DishDefinition("ผัดซีอิ๊ว", "ผัดซีอิ๊ว (Pad See Ew)", 520, hasPortion = true),
        DishDefinition("ส้มตำ", "ส้มตำ (Som Tam)", 120, hasPortion = false),
        DishDefinition("ต้มยำ", "ต้มยำ (Tom Yum)", 250, hasPortion = false),
        DishDefinition("ต้มข่า", "ต้มข่า (Tom Kha)", 300, hasPortion = false),
        DishDefinition("ก๋วยเตี๋ยว", "ก๋วยเตี๋ยว (noodle soup)", 350, hasPortion = false),
        DishDefinition("สลัด", "สลัด (salad)", 200, hasPortion = false),
        DishDefinition("โจ๊ก", "โจ๊ก (rice porridge)", 250, hasPortion = false),
        DishDefinition("ข้าวต้ม", "ข้าวต้ม (rice soup)", 260, hasPortion = false),
        DishDefinition("fried rice", "Fried rice", 550, hasPortion = true),
        DishDefinition("pad thai", "Pad Thai", 500, hasPortion = true),
        DishDefinition("noodle", "Noodle soup", 350, hasPortion = false),
        DishDefinition("salad", "Salad", 200, hasPortion = false),
        DishDefinition("rice", "Rice-based meal", 500, hasPortion = true)
    )

    private val addons = listOf(
        AddonDefinition("ไข่ดาว", "ไข่ดาว (fried egg)", 90),
        AddonDefinition("ไข่เจียว", "ไข่เจียว (omelette)", 150),
        AddonDefinition("ไข่ต้ม", "ไข่ต้ม (boiled egg)", 70),
        AddonDefinition("เพิ่มข้าว", "เพิ่มข้าว (extra rice)", 150),
        AddonDefinition("fried egg", "Fried egg", 90),
        AddonDefinition("omelette", "Omelette", 150),
        AddonDefinition("boiled egg", "Boiled egg", 70),
        AddonDefinition("extra rice", "Extra rice", 150)
    )

    private const val FALLBACK_NAME = "อาหารตามที่อธิบาย (unmatched, please confirm calories)"
    private const val FALLBACK_CALORIES = 350

    fun parse(rawText: String): ParsedFoodDescription {
        val text = rawText.trim()
        val normalized = text.lowercase()

        val matchedDish = dishes.firstOrNull { normalized.contains(it.keyword.lowercase()) }

        val matchedAddons = addons.filter { addon ->
            normalized.contains(addon.keyword.lowercase()) &&
                matchedDish?.keyword?.lowercase()?.contains(addon.keyword.lowercase()) != true
        }
        val addonCalories = matchedAddons.sumOf { it.calories }
        val addonNamesSuffix = if (matchedAddons.isEmpty()) "" else " + " + matchedAddons.joinToString(" + ") { it.name }

        val hasPortion = matchedDish?.hasPortion ?: true
        val baseCalories = matchedDish?.baseCalories ?: FALLBACK_CALORIES
        val suggestedName = (matchedDish?.name ?: text.ifBlank { FALLBACK_NAME }) + addonNamesSuffix

        return ParsedFoodDescription(
            rawText = text,
            suggestedName = suggestedName,
            baseCalories = baseCalories,
            addonCalories = addonCalories,
            matchedKnownDish = matchedDish != null,
            followUpQuestion = if (hasPortion) {
                FollowUpQuestion(
                    prompt = "ปริมาณข้าวประมาณเท่าไหร่?",
                    options = STANDARD_PORTION_OPTIONS
                )
            } else {
                null
            }
        )
    }
}
