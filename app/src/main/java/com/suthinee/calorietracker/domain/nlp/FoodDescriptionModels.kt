package com.suthinee.calorietracker.domain.nlp

/** A follow-up question the parser wants to ask, e.g. "how much rice?". Always skippable. */
data class FollowUpQuestion(
    val prompt: String,
    val options: List<PortionOption>
)

data class PortionOption(
    val label: String,
    val multiplier: Double?
)

/** Result of parsing a free-text food description before any follow-up question is answered. */
data class ParsedFoodDescription(
    val rawText: String,
    val suggestedName: String,
    val baseCalories: Int,
    val addonCalories: Int,
    val matchedKnownDish: Boolean,
    val followUpQuestion: FollowUpQuestion?
) {
    /** Calories if the follow-up question is skipped or answered "unsure" (assumes a standard single portion). */
    val defaultEstimatedCalories: Int
        get() = baseCalories + addonCalories

    fun estimatedCalories(multiplier: Double?): Int {
        val m = multiplier ?: 1.0
        return Math.round(baseCalories * m).toInt() + addonCalories
    }
}

val STANDARD_PORTION_OPTIONS = listOf(
    PortionOption("ครึ่งจาน", 0.5),
    PortionOption("1 จาน", 1.0),
    PortionOption("1.5 จาน", 1.5),
    PortionOption("ไม่แน่ใจ", null)
)
