package com.microapps.meetingmeter

fun meetingCost(people: Int, hourlyRate: Double, elapsedMs: Long): Double {
    if (people <= 0 || hourlyRate <= 0 || elapsedMs <= 0) return 0.0
    return people * hourlyRate * (elapsedMs / 1000.0) / 3600.0
}

fun formatDuration(ms: Long): String {
    val total = ms.coerceAtLeast(0) / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
