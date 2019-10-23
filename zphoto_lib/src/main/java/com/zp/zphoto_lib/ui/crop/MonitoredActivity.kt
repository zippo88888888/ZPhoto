package com.zp.zphoto_lib.ui.crop

import android.app.Activity
import android.os.Bundle
import java.util.ArrayList

internal abstract class MonitoredActivity : Activity() {

    private val listeners = ArrayList<LifeCycleListener>()

    interface LifeCycleListener {
        fun onActivityCreated(activity: MonitoredActivity)
        fun onActivityDestroyed(activity: MonitoredActivity)
        fun onActivityStarted(activity: MonitoredActivity)
        fun onActivityStopped(activity: MonitoredActivity)
    }

    open class LifeCycleAdapter : LifeCycleListener {
        override fun onActivityCreated(activity: MonitoredActivity) {}
        override fun onActivityDestroyed(activity: MonitoredActivity) {}
        override fun onActivityStarted(activity: MonitoredActivity) {}
        override fun onActivityStopped(activity: MonitoredActivity) {}
    }

    open fun addLifeCycleListener(listener: LifeCycleListener) {
        if (listeners.contains(listener)) return
        listeners.add(listener)
    }

    open fun removeLifeCycleListener(listener: LifeCycleListener) {
        listeners.remove(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (listener in listeners) {
            listener.onActivityCreated(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (listener in listeners) {
            listener.onActivityDestroyed(this)
        }
    }

    override fun onStart() {
        super.onStart()
        for (listener in listeners) {
            listener.onActivityStarted(this)
        }
    }

    override fun onStop() {
        super.onStop()
        for (listener in listeners) {
            listener.onActivityStopped(this)
        }
    }

}