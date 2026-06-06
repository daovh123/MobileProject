package com.example.mobileproject

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.mobileproject.presentation.notification.ChatNotificationGate
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point annotated with [@HiltAndroidApp] to trigger Hilt's code generation
 * and create the application-level dependency container. All singleton-scoped bindings
 * provided via [@InstallIn(SingletonComponent::class)] are available from here.
 *
 * Registers a process-lifecycle observer to track foreground/background transitions
 * so that [ChatNotificationGate] can suppress chat notifications while the app is visible
 * and reset chat-route state when the app goes to the background.
 */
@HiltAndroidApp
class MainApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		// Observe process-level lifecycle (app-wide foreground/background, not per-Activity).
		ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

			/** Called when the app moves to the foreground (any Activity becomes visible). */
			override fun onStart(owner: LifecycleOwner) {
				ChatNotificationGate.setAppInForeground(true)
			}

			/** Called when the app moves to the background (last Activity stopped). */
			override fun onStop(owner: LifecycleOwner) {
				ChatNotificationGate.setAppInForeground(false)
				// Reset active chat route so notifications resume when the user returns.
				ChatNotificationGate.setChatRouteActive(false)
			}
		})
	}
}
