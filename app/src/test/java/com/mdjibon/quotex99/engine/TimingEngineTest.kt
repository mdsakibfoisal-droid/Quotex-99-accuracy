package com.mdjibon.quotex99.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimingEngineTest {
    @Test fun cycleIsAlignedToFifteenSecondWallClockBoundaries() {
        assertEquals(0L, TimingEngine.elapsedInCycle(0L))
        assertEquals(1_000L, TimingEngine.elapsedInCycle(16_000L))
        assertEquals(14_999L, TimingEngine.elapsedInCycle(29_999L))
        assertEquals(0L, TimingEngine.elapsedInCycle(30_000L))
    }

    @Test fun remainingTimeRollsOverAtBoundary() {
        assertEquals(15_000L, TimingEngine.remainingInCycle(30_000L))
        assertEquals(1L, TimingEngine.remainingInCycle(29_999L))
    }

    @Test fun lastFiveSecondWindowIsPreciselyBounded() {
        assertFalse(TimingEngine.isInLastFiveSeconds(9_999L))
        assertTrue(TimingEngine.isInLastFiveSeconds(10_000L))
        assertTrue(TimingEngine.isInLastFiveSeconds(14_999L))
        assertFalse(TimingEngine.isInLastFiveSeconds(15_000L))
    }

    @Test fun negativeWallClockValuesUsePositiveCycleOffsets() {
        assertEquals(14_000L, TimingEngine.elapsedInCycle(-1_000L))
    }
}
