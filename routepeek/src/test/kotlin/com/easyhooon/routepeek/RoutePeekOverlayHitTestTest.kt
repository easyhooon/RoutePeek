package com.easyhooon.routepeek

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePeekOverlayHitTestTest {
    @Test
    fun returnsTrueWhenCollapsedCenterIsInsideRemoveTarget() {
        val collapsedBounds = Rect(
            left = 90f,
            top = 90f,
            right = 110f,
            bottom = 110f,
        )
        val removeTargetBounds = Rect(
            left = 80f,
            top = 80f,
            right = 120f,
            bottom = 120f,
        )

        assertTrue(isCollapsedInRemoveTarget(collapsedBounds, removeTargetBounds))
    }

    @Test
    fun returnsFalseWhenCollapsedCenterIsOutsideRemoveTarget() {
        val collapsedBounds = Rect(
            left = 10f,
            top = 10f,
            right = 30f,
            bottom = 30f,
        )
        val removeTargetBounds = Rect(
            left = 80f,
            top = 80f,
            right = 120f,
            bottom = 120f,
        )

        assertFalse(isCollapsedInRemoveTarget(collapsedBounds, removeTargetBounds))
    }

    @Test
    fun returnsFalseWhenBoundsAreMissing() {
        val bounds = Rect(
            left = 80f,
            top = 80f,
            right = 120f,
            bottom = 120f,
        )

        assertFalse(isCollapsedInRemoveTarget(collapsedBounds = null, removeTargetBounds = bounds))
        assertFalse(isCollapsedInRemoveTarget(collapsedBounds = bounds, removeTargetBounds = null))
    }
}
