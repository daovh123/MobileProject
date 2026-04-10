package com.example.mobileproject

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.mobileproject.presentation.notification.ChatNotificationGate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
			override fun onStart(owner: LifecycleOwner) {
				ChatNotificationGate.setAppInForeground(true)
			}

			override fun onStop(owner: LifecycleOwner) {
				ChatNotificationGate.setAppInForeground(false)
				ChatNotificationGate.setChatRouteActive(false)
			}
		})
	}
}
