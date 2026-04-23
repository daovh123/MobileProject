package com.example.mobileproject.presentation.notification

object ChatNotificationGate {

    @Volatile
    private var appInForeground: Boolean = false

    @Volatile
    private var chatRouteActive: Boolean = false

    fun setHomeActivityResumed(resumed: Boolean) {
        setAppInForeground(resumed)
    }

    fun setAppInForeground(inForeground: Boolean) {
        appInForeground = inForeground
    }

    fun setChatRouteActive(active: Boolean) {
        chatRouteActive = active
    }

    fun isAppInForeground(): Boolean {
        return appInForeground
    }

    fun isChatRouteActive(): Boolean {
        return chatRouteActive
    }

    fun shouldSuppressIncomingChatNotifications(): Boolean {
        return appInForeground && chatRouteActive
    }

    fun shouldShowInAppIncomingChatNotifications(): Boolean {
        return appInForeground && !chatRouteActive
    }
}
