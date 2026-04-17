package com.example.financipline

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.financipline.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Button 1: Request Notification Listener Permission
        binding.btnPermission.setOnClickListener {
            if (!isNotificationServiceEnabled()) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            } else {
                Toast.makeText(this, "Ear is already Active!", Toast.LENGTH_SHORT).show()
            }
        }

        // Button 2: Send a fake Payment Notification to test the "Ear"
        binding.btnTestPing.setOnClickListener {
            sendTestPaymentNotification()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }

    private fun sendTestPaymentNotification() {
        val channelId = "payment_test"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Payments", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("PhonePe")
            .setContentText("Paid ₹250.00 to Starbucks") // The "Ear" should catch this!
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(1, builder.build())
    }
}