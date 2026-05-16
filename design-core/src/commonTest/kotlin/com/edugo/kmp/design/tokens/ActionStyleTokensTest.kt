package com.edugo.kmp.design.tokens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActionStyleTokensTest {
    @Test
    fun primaryFilledMapsToFilledOnPrimaryOverPrimary() {
        val config = actionRenderConfigFor("primary", DSControlType.FILLED_BUTTON)
        assertEquals(DSVariant.FILLED, config.variant)
        assertEquals(ColorRole.ON_PRIMARY, config.tintRole)
        assertEquals(ColorRole.PRIMARY, config.containerRole)
    }

    @Test
    fun primaryIconMapsToIconWithPrimaryTintAndNoContainer() {
        val config = actionRenderConfigFor("primary", DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, config.variant)
        assertEquals(ColorRole.PRIMARY, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun secondaryOutlinedMapsToOutlinedPrimary() {
        val config = actionRenderConfigFor("secondary", DSControlType.OUTLINED_BUTTON)
        assertEquals(DSVariant.OUTLINED, config.variant)
        assertEquals(ColorRole.PRIMARY, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun secondaryIconMapsToIconOnSurfaceVariant() {
        val config = actionRenderConfigFor("secondary", DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, config.variant)
        assertEquals(ColorRole.ON_SURFACE_VARIANT, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun tonalFilledMapsToTonalContainer() {
        val config = actionRenderConfigFor("tonal", DSControlType.FILLED_BUTTON)
        assertEquals(DSVariant.TONAL, config.variant)
        assertEquals(ColorRole.ON_SECONDARY_CONTAINER, config.tintRole)
        assertEquals(ColorRole.SECONDARY_CONTAINER, config.containerRole)
    }

    @Test
    fun tonalIconMapsToIconOnSecondaryContainer() {
        val config = actionRenderConfigFor("tonal", DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, config.variant)
        assertEquals(ColorRole.ON_SECONDARY_CONTAINER, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun destructiveOutlinedMapsToOutlinedError() {
        val config = actionRenderConfigFor("destructive", DSControlType.OUTLINED_BUTTON)
        assertEquals(DSVariant.OUTLINED, config.variant)
        assertEquals(ColorRole.ERROR, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun destructiveIconMapsToIconError() {
        val config = actionRenderConfigFor("destructive", DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, config.variant)
        assertEquals(ColorRole.ERROR, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun destructiveFilledMapsToFilledOnErrorOverError() {
        val config = actionRenderConfigFor("destructive", DSControlType.FILLED_BUTTON)
        assertEquals(DSVariant.FILLED, config.variant)
        assertEquals(ColorRole.ON_ERROR, config.tintRole)
        assertEquals(ColorRole.ERROR, config.containerRole)
    }

    @Test
    fun successFilledMapsToFilledOnSuccessOverSuccess() {
        val config = actionRenderConfigFor("success", DSControlType.FILLED_BUTTON)
        assertEquals(DSVariant.FILLED, config.variant)
        assertEquals(ColorRole.ON_SUCCESS, config.tintRole)
        assertEquals(ColorRole.SUCCESS, config.containerRole)
    }

    @Test
    fun successIconMapsToIconSuccess() {
        val config = actionRenderConfigFor("success", DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, config.variant)
        assertEquals(ColorRole.SUCCESS, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun iconOnlyMapsToIconNeutral() {
        val config = actionRenderConfigFor("icon-only", DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, config.variant)
        assertEquals(ColorRole.ON_SURFACE_VARIANT, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun nullStyleFallsBackToSecondaryForControlType() {
        val outlined = actionRenderConfigFor(null, DSControlType.OUTLINED_BUTTON)
        assertEquals(DSVariant.OUTLINED, outlined.variant)
        assertEquals(ColorRole.PRIMARY, outlined.tintRole)
        assertNull(outlined.containerRole)

        val icon = actionRenderConfigFor(null, DSControlType.ICON_BUTTON)
        assertEquals(DSVariant.ICON, icon.variant)
        assertEquals(ColorRole.ON_SURFACE_VARIANT, icon.tintRole)
        assertNull(icon.containerRole)
    }

    @Test
    fun unknownStyleFallsBackToSecondaryForControlType() {
        val config = actionRenderConfigFor("does-not-exist", DSControlType.OUTLINED_BUTTON)
        assertEquals(DSVariant.OUTLINED, config.variant)
        assertEquals(ColorRole.PRIMARY, config.tintRole)
        assertNull(config.containerRole)
    }

    @Test
    fun primaryDefaultControlTypeIsFilled() {
        // controlType TEXT_BUTTON cae al default de primary (FILLED).
        val config = actionRenderConfigFor("primary", DSControlType.TEXT_BUTTON)
        assertEquals(DSVariant.FILLED, config.variant)
        assertEquals(ColorRole.ON_PRIMARY, config.tintRole)
        assertEquals(ColorRole.PRIMARY, config.containerRole)
    }
}
