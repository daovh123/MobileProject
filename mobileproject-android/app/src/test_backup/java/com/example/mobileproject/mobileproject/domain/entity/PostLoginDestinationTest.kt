package com.example.mobileproject.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class PostLoginDestinationTest {

    @Test
    fun `profile incomplete navigates to profile screen`() {
        val session = AuthSession(
            token = "t-1",
            username = "alice",
            email = "alice@example.com",
            profileCompleted = false,
            coupleConnected = false,
        )

        assertEquals(PostLoginDestination.PROFILE, session.resolvePostLoginDestination())
    }

    @Test
    fun `profile complete but not coupled navigates to home`() {
        val session = AuthSession(
            token = "t-2",
            username = "bob",
            email = "bob@example.com",
            profileCompleted = true,
            coupleConnected = false,
        )

        assertEquals(PostLoginDestination.HOME, session.resolvePostLoginDestination())
    }

    @Test
    fun `profile complete and coupled navigates to home`() {
        val session = AuthSession(
            token = "t-3",
            username = "tom",
            email = "tom@example.com",
            profileCompleted = true,
            coupleConnected = true,
        )

        assertEquals(PostLoginDestination.HOME, session.resolvePostLoginDestination())
    }
}
