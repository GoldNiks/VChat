package com.vchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TpsUtilTest {

    @Test
    void fullSpeedServerReports20() {
        assertEquals("20.0", TpsUtil.format(50f));
        assertEquals("20.0", TpsUtil.format(25f));
    }

    @Test
    void laggingServerScalesDown() {
        assertEquals("10.0", TpsUtil.format(100f));
        assertEquals("15.0", TpsUtil.format(66.67f));
        assertEquals("5.0", TpsUtil.format(200f));
    }

    @Test
    void nonPositiveAverageReportsFullSpeed() {
        assertEquals("20.0", TpsUtil.format(0f));
        assertEquals("20.0", TpsUtil.format(-5f));
    }
}