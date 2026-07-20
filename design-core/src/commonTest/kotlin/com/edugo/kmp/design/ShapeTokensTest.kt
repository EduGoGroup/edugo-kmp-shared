package com.edugo.kmp.design

import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.ComponentShapes
import com.edugo.kmp.design.tokens.CornerRadius
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShapeTokensTest {
    @Test
    fun cornerRadiusScaleIsOrdered() {
        assertTrue(CornerRadius.none < CornerRadius.extraSmall)
        assertTrue(CornerRadius.extraSmall < CornerRadius.small)
        assertTrue(CornerRadius.small < CornerRadius.medium)
        assertTrue(CornerRadius.medium < CornerRadius.large)
        assertTrue(CornerRadius.large < CornerRadius.largeIncreased)
        assertTrue(CornerRadius.largeIncreased < CornerRadius.extraLarge)
        assertTrue(CornerRadius.extraLarge < CornerRadius.full)
    }

    @Test
    fun cornerRadiusValuesMatchSpec() {
        assertEquals(0.dp, CornerRadius.none)
        assertEquals(4.dp, CornerRadius.extraSmall)
        assertEquals(8.dp, CornerRadius.small)
        assertEquals(12.dp, CornerRadius.medium)
        assertEquals(16.dp, CornerRadius.large)
        assertEquals(20.dp, CornerRadius.largeIncreased)
        assertEquals(28.dp, CornerRadius.extraLarge)
        assertEquals(50.dp, CornerRadius.full)
    }

    @Test
    fun componentShapesMapToCorrectRadius() {
        // Remapeo MD3 Expressive — D-046.3 (plan 046): card 20.dp, chip/fab pill.
        assertEquals(CornerRadius.extraSmall, ComponentShapes.checkbox)
        assertEquals(CornerRadius.full, ComponentShapes.chip)
        assertEquals(CornerRadius.largeIncreased, ComponentShapes.card)
        assertEquals(CornerRadius.large, ComponentShapes.button)
        assertEquals(CornerRadius.full, ComponentShapes.fab)
        assertEquals(CornerRadius.extraLarge, ComponentShapes.bottomSheet)
        assertEquals(CornerRadius.extraLarge, ComponentShapes.dialog)
    }
}
