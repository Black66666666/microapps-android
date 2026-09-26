package com.microapps.meetingmeter

import org.junit.Assert.assertEquals
import org.junit.Test

class MeetingLogicTest {
    @Test fun oneHourCostIsPeopleTimesRate() = assertEquals(240.0, meetingCost(6, 40.0, 3_600_000), 0.0001)
    @Test fun invalidInputsCostZero() = assertEquals(0.0, meetingCost(0, 40.0, 10_000), 0.0001)
    @Test fun durationFormattingWorksAcrossHour() = assertEquals("1:01:01", formatDuration(3_661_000))
}
