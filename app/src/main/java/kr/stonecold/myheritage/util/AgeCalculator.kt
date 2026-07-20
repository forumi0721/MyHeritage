package kr.stonecold.myheritage.util

import kr.stonecold.myheritage.data.FactEntity
import java.util.Calendar

object AgeCalculator {
    data class AgeInfo(
        val ageText: String? = null, // "54세" 또는 "82세 별세"
        val age: Int? = null,
        val birthYear: Int? = null,
        val deathYear: Int? = null,
        val isDeceased: Boolean = false
    )

    fun calculateAgeInfo(facts: List<FactEntity>): AgeInfo {
        val birthFact = facts.firstOrNull { it.label?.trim() == "출생" || it.type == 1 }
        val deathFact = facts.firstOrNull { it.label?.trim() == "사망" || it.type == 2 }

        if (birthFact == null) return AgeInfo()

        val birthYmd = parseYearMonthDay(birthFact.startDate) ?: return AgeInfo()
        val birthYear = birthYmd.first
        val birthMonth = birthYmd.second
        val birthDay = birthYmd.third

        if (deathFact != null) {
            val deathYmd = parseYearMonthDay(deathFact.startDate)
            if (deathYmd != null) {
                val deathYear = deathYmd.first
                val deathMonth = deathYmd.second
                val deathDay = deathYmd.third

                var age = deathYear - birthYear
                if (deathMonth < birthMonth || (deathMonth == birthMonth && deathDay < birthDay)) {
                    age--
                }
                val finalAge = if (age < 0) 0 else age
                return AgeInfo(
                    ageText = "${finalAge}세 별세",
                    age = finalAge,
                    birthYear = birthYear,
                    deathYear = deathYear,
                    isDeceased = true
                )
            }
        }

        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)

        var age = currentYear - birthYear
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--
        }
        val finalAge = if (age < 0) 0 else age

        return AgeInfo(
            ageText = "${finalAge}세",
            age = finalAge,
            birthYear = birthYear,
            deathYear = null,
            isDeceased = false
        )
    }

    private fun parseYearMonthDay(dateVal: Long): Triple<Int, Int, Int>? {
        if (dateVal <= 0) return null
        return if (dateVal in 10000000..99991231) {
            val year = (dateVal / 10000).toInt()
            val month = ((dateVal % 10000) / 100).toInt()
            val day = (dateVal % 100).toInt()
            Triple(year, month, day)
        } else {
            val cal = Calendar.getInstance()
            cal.timeInMillis = dateVal
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        }
    }
}
