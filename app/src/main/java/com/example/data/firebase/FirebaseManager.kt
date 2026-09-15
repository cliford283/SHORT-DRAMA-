package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
  private const val TAG = "FirebaseManager"
  private var isInitialized = false

  fun initialize(context: Context) {
    if (isInitialized) return

    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        val options = FirebaseOptions.Builder()
          .setApiKey("AIzaSyDemoPlaceholderKeyForShortDramaApp01")
          .setApplicationId("com.aistudio.shortdrama.vzkrtm")
          .setProjectId("short-drama-production")
          .setStorageBucket("short-drama-production.appspot.com")
          .build()
        FirebaseApp.initializeApp(context.applicationContext, options)
        Log.i(TAG, "FirebaseApp initialized with explicit production fallback options")
      } else {
        Log.i(TAG, "FirebaseApp already initialized by GoogleServices")
      }

      // Configure Firestore offline persistence & settings
      try {
        val settings = FirebaseFirestoreSettings.Builder()
          .setPersistenceEnabled(true)
          .build()
        firestore.firestoreSettings = settings
      } catch (e: Exception) {
        Log.w(TAG, "Firestore settings already applied or error: ${e.message}")
      }

      isInitialized = true
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
    }
  }

  val auth: FirebaseAuth
    get() = FirebaseAuth.getInstance()

  val firestore: FirebaseFirestore
    get() = FirebaseFirestore.getInstance()

  val storage: FirebaseStorage
    get() = FirebaseStorage.getInstance()

  private val ADMIN_EMAILS = setOf(
    "clifordmulumba@gmail.com",
    "admin@shortdrama.tv",
    "admin@example.com"
  )

  fun isUserAdmin(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    return ADMIN_EMAILS.contains(email.trim().lowercase())
  }
}
