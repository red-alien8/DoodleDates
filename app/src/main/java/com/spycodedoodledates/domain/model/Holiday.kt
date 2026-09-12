package com.spycodedoodledates.domain.model

data class Holiday(
    val year: Int,
    val month: Int, // 1-indexed
    val day: Int,
    val name: String,
    val isMoonDependent: Boolean = false
)

fun getHolidaysForYear(year: Int): List<Holiday> {
    // Base 2026 data
    val baseHolidays = listOf(
        Holiday(2026, 2, 4, "Shab-e-Barat", true),
        Holiday(2026, 2, 21, "Shaheed Day / Int'l Mother Language Day"),
        Holiday(2026, 3, 17, "Sheikh Mujibur Rahman's Birthday"),
        Holiday(2026, 3, 18, "Laylat al-Qadr", true),
        Holiday(2026, 3, 21, "Eid-ul-Fitr Day", true),
        Holiday(2026, 3, 26, "Independence Day"),
        Holiday(2026, 4, 14, "Pohela Boishakh"),
        Holiday(2026, 5, 1, "May Day/Buddha Purnima"),
        Holiday(2026, 5, 27, "Eid-ul-Azha Day", true),
        Holiday(2026, 6, 26, "Ashura", true),
        Holiday(2026, 8, 5, "July Mass Uprising Day"),
        Holiday(2026, 8, 15, "National Mourning Day"),
        Holiday(2026, 8, 26, "Eid-e-Milad-un-Nabi", true),
        Holiday(2026, 10, 20, "Durga Puja"),
        Holiday(2026, 10, 21, "Vijaya Dashami"),
        Holiday(2026, 12, 16, "Victory Day"),
        Holiday(2026, 12, 25, "Christmas Day")
    )

    if (year == 2026) return baseHolidays

    // For 2027-2030, shift Islamic holidays by ~11 days earlier per year
    val shift = (year - 2026) * 11
    
    return listOf(
        // Fixed date holidays
        Holiday(year, 2, 21, "Shaheed Day / Int'l Mother Language Day"),
        Holiday(year, 3, 17, "Sheikh Mujibur Rahman's Birthday"),
        Holiday(year, 3, 26, "Independence Day"),
        Holiday(year, 4, 14, "Pohela Boishakh"),
        Holiday(year, 5, 1, "May Day"),
        Holiday(year, 8, 5, "July Mass Uprising Day"),
        Holiday(year, 8, 15, "National Mourning Day"),
        Holiday(year, 10, 20, "Durga Puja"),
        Holiday(year, 10, 21, "Vijaya Dashami"),
        Holiday(year, 12, 16, "Victory Day"),
        Holiday(year, 12, 25, "Christmas Day"),
        
        // Approximate Lunar holidays (Simulated for this exercise)
        // In a real app, you would use a proper Hijri calendar library
        Holiday(year, 1, 24, "Shab-e-Barat (Approx)", true),
        Holiday(year, 3, 10, "Eid-ul-Fitr (Approx)", true),
        Holiday(year, 5, 16, "Eid-ul-Azha (Approx)", true),
        Holiday(year, 6, 15, "Ashura (Approx)", true)
    )
}
