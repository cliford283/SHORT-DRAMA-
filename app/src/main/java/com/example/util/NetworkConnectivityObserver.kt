package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class NetworkStatus {
  Available,
  Lost,
  Unavailable
}

class NetworkConnectivityObserver(context: Context) {

  private val connectivityManager =
    context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  fun observe(): Flow<NetworkStatus> = callbackFlow {
    // Check initial state
    val isInitiallyConnected = isCurrentlyConnected()
    trySend(if (isInitiallyConnected) NetworkStatus.Available else NetworkStatus.Unavailable)

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)
        trySend(NetworkStatus.Available)
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
      }

      override fun onLost(network: Network) {
        super.onLost(network)
        trySend(NetworkStatus.Lost)
      }

      override fun onUnavailable() {
        super.onUnavailable()
        trySend(NetworkStatus.Unavailable)
      }
    }

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    connectivityManager.registerNetworkCallback(request, callback)

    awaitClose {
      try {
        connectivityManager.unregisterNetworkCallback(callback)
      } catch (e: Exception) {
        // Ignored if already unregistered
      }
    }
  }.distinctUntilChanged()

  private fun isCurrentlyConnected(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }
}
