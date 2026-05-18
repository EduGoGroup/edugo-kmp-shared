package com.edugo.kmp.network.connectivity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Android implementation using [ConnectivityManager.registerDefaultNetworkCallback].
 *
 * Adicionalmente registra un `ActivityLifecycleCallbacks` para auto-recuperar
 * el estado real cada vez que una Activity entra a `onResume`. Bajo presión
 * severa del sistema (slow dispatch del system_server, doze parcial) el
 * `NetworkCallback` puede recibir un `onLost` sin el `onAvailable` siguiente,
 * dejando el StateFlow desincronizado del estado real de red.
 */
internal class AndroidNetworkObserver(
    private val context: Context
) : NetworkObserver {

    private val _status = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    override val status: StateFlow<NetworkStatus> = _status

    private var callback: ConnectivityManager.NetworkCallback? = null
    private var activityCallbacks: Application.ActivityLifecycleCallbacks? = null

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun start() {
        if (callback != null) return

        refresh()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _status.value = NetworkStatus.AVAILABLE
            }

            override fun onLost(network: Network) {
                _status.value = NetworkStatus.UNAVAILABLE
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                _status.value = NetworkStatus.LOSING
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        callback = networkCallback

        registerLifecycleHook()
    }

    override fun stop() {
        callback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            callback = null
        }
        (context.applicationContext as? Application)?.let { app ->
            activityCallbacks?.let { app.unregisterActivityLifecycleCallbacks(it) }
        }
        activityCallbacks = null
    }

    override fun refresh() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        _status.value =
            if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                NetworkStatus.AVAILABLE
            } else {
                NetworkStatus.UNAVAILABLE
            }
    }

    private fun registerLifecycleHook() {
        val app = context.applicationContext as? Application ?: return
        val callbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                refresh()
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        app.registerActivityLifecycleCallbacks(callbacks)
        activityCallbacks = callbacks
    }
}

/**
 * Holds the Android application context for creating [NetworkObserver].
 * Must be initialized in Application.onCreate() before Koin starts.
 */
public object NetworkContextHolder {
    internal var context: Context? = null

    public fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}

/**
 * Android implementation uses [NetworkContextHolder] to obtain the application context.
 * Call [NetworkContextHolder.initialize] in Application.onCreate() before using this.
 */
public actual fun createNetworkObserver(): NetworkObserver {
    val context = NetworkContextHolder.context
        ?: throw IllegalStateException(
            "NetworkContextHolder not initialized. Call NetworkContextHolder.initialize(context) in Application.onCreate()."
        )
    return AndroidNetworkObserver(context)
}

/**
 * Factory function for DI usage with Android Context.
 */
public fun createAndroidNetworkObserver(context: Context): NetworkObserver {
    return AndroidNetworkObserver(context)
}
