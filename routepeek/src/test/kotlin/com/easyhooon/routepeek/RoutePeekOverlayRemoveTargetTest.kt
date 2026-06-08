package com.easyhooon.routepeek

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// JVM unit tests for the remove-target hit-test helper, not Compose UI gesture tests.
class RoutePeekOverlayRemoveTargetTest {
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

    @Test
    fun returnsTrueWhenCenterOnIncludedBoundary() {
        val collapsedBounds = Rect(
            left = 70f,
            top = 70f,
            right = 90f,
            bottom = 90f,
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
    fun returnsFalseWhenCenterOnExcludedBoundary() {
        val collapsedBounds = Rect(
            left = 110f,
            top = 110f,
            right = 130f,
            bottom = 130f,
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
    fun returnsHandledForZeroSizeRect() {
        val zeroSizeCollapsedBounds = Rect(
            left = 100f,
            top = 100f,
            right = 100f,
            bottom = 100f,
        )
        val removeTargetBounds = Rect(
            left = 80f,
            top = 80f,
            right = 120f,
            bottom = 120f,
        )
        val zeroSizeRemoveTargetBounds = Rect(
            left = 100f,
            top = 100f,
            right = 100f,
            bottom = 100f,
        )

        assertTrue(isCollapsedInRemoveTarget(zeroSizeCollapsedBounds, removeTargetBounds))
        assertFalse(isCollapsedInRemoveTarget(removeTargetBounds, zeroSizeRemoveTargetBounds))
    }

    @Test
    fun returnsHandledForNegativeCoordinates() {
        val collapsedBounds = Rect(
            left = -30f,
            top = -30f,
            right = -10f,
            bottom = -10f,
        )
        val removeTargetBounds = Rect(
            left = -40f,
            top = -40f,
            right = 0f,
            bottom = 0f,
        )

        assertTrue(isCollapsedInRemoveTarget(collapsedBounds, removeTargetBounds))
    }
}
