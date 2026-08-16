package io.github.v1rusdev.simplemvi.android

import androidx.lifecycle.SavedStateHandle
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedStateHandleExtTest {

    @Test
    fun `returns the stored value without calling the default`() {
        val handle = SavedStateHandle(mapOf("profile_id" to "stored"))
        var defaultCalls = 0

        val value = handle.getOrPut("profile_id") {
            defaultCalls++
            "fallback"
        }

        assertEquals("stored", value)
        assertEquals(0, defaultCalls)
    }

    @Test
    fun `stores the default when the key is missing`() {
        val handle = SavedStateHandle()

        val value = handle.getOrPut("profile_id") { "me" }

        assertEquals("me", value)
        assertEquals("me", handle.get<String>("profile_id"))
    }

    @Test
    fun `computes the default only once for a non null value`() {
        val handle = SavedStateHandle()
        var defaultCalls = 0

        repeat(3) {
            handle.getOrPut<String>("profile_id") {
                defaultCalls++
                "me"
            }
        }

        assertEquals(1, defaultCalls)
    }

    @Test
    fun `recomputes the default on every call when the stored value is null`() {
        val handle = SavedStateHandle(mapOf("nickname" to null))
        var defaultCalls = 0

        repeat(3) {
            handle.getOrPut<String?>("nickname") {
                defaultCalls++
                null
            }
        }

        // A stored null is indistinguishable from a missing key, so the default is not cached.
        // This documents the known limitation described in getOrPut's KDoc.
        assertEquals(3, defaultCalls)
    }
}
