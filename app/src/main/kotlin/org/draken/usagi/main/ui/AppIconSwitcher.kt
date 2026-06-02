package org.draken.usagi.main.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object AppIconSwitcher {
	private var pendingUseAlt: Boolean? = null
	private var startedActivitiesCount = 0
	private var isLifecycleRegistered = false

	fun isEggIconActive(context: Context): Boolean {
		val pm = context.packageManager
		val state = pm.getComponentEnabledSetting(ComponentName(context, EGG_ACTIVITY))
		return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
	}

	fun toggleIcon(context: Context) {
		val useEgg = !isEggIconActive(context)
		pendingUseAlt = useEgg

		val appContext = context.applicationContext as android.app.Application
		if (!isLifecycleRegistered) {
			appContext.registerActivityLifecycleCallbacks(object : android.app.Application.ActivityLifecycleCallbacks {
				override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
				override fun onActivityDestroyed(activity: android.app.Activity) {}
				
				override fun onActivityStarted(activity: android.app.Activity) {
					startedActivitiesCount++
				}
				
				override fun onActivityStopped(activity: android.app.Activity) {
					startedActivitiesCount--
					if (startedActivitiesCount <= 0) {
						applyPendingChange(appContext)
						appContext.unregisterActivityLifecycleCallbacks(this)
						isLifecycleRegistered = false
					}
				}
				
				override fun onActivityResumed(activity: android.app.Activity) {}
				override fun onActivityPaused(activity: android.app.Activity) {}
				override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
			})
			isLifecycleRegistered = true
		}
	}

	private fun applyPendingChange(context: Context) {
		val useEgg = pendingUseAlt ?: return
		pendingUseAlt = null
		val pm = context.packageManager
		pm.setComponentEnabledSetting(
			ComponentName(context, DEFAULT_ACTIVITY),
			if (useEgg) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			PackageManager.DONT_KILL_APP,
		)
		pm.setComponentEnabledSetting(
			ComponentName(context, EGG_ACTIVITY),
			if (useEgg) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP,
		)
	}

	private const val DEFAULT_ACTIVITY = "org.draken.usagi.main.ui.MainActivity.DefaultIcon"
	private const val EGG_ACTIVITY = "org.draken.usagi.main.ui.MainActivity.EasterEgg"
}
