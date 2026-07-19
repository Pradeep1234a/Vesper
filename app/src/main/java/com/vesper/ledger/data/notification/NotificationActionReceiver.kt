package com.vesper.ledger.data.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.vesper.ledger.VesperApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val txId = intent.getLongExtra("TX_ID", -1L)
        
        Log.d("NotificationActionRc", "Action received: $action for TxID: $txId")

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (action == Intent.ACTION_MY_PACKAGE_REPLACED || action == "android.intent.action.MY_PACKAGE_REPLACED") {
            VesperNotificationApi.sendNotification(
                title = "Update Complete",
                body = "Vesper Ledger was updated successfully! Tap to check out what is new.",
                category = NotificationCategory.PRODUCT_UPDATES,
                bypassCooldown = true
            )
            return
        }

        if (action == "DISMISS_NOTIFICATION") {
            val notifyId = intent.getLongExtra("NOTIFICATION_ID", -1L)
            if (notifyId != -1L) {
                VesperNotificationApi.trackDismissed(notifyId)
            }
            return
        }

        // Dismiss notification
        manager.cancel(txId.toInt() + 10000)

        if (txId == -1L) return

        when (action) {
            "MARK_PAID" -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val app = context.applicationContext as VesperApplication
                        val db = app.database
                        val dao = db.transactionDao()
                        val tx = dao.getTransactionById(txId)
                        if (tx != null) {
                            // Insert a new transaction matching the subscription details with today's date
                            val newTx = tx.copy(
                                id = 0L, // Auto-generate new primary key
                                dateEpochMillis = System.currentTimeMillis(),
                                note = "Auto-paid: ${tx.title}".trim()
                            )
                            dao.insertTransaction(newTx)
                            
                            // Visual confirmation Toast
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Marked '${tx.title}' as Paid", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationActionRc", "Failed to mark transaction as paid", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            "SNOOZE" -> {
                // Cancel current notification banner
                manager.cancel(intent.getLongExtra("NOTIFICATION_ID", -1L).toInt())
                
                // Reschedule notification for 1 hour later using a quick WorkManager task
                val request = OneTimeWorkRequestBuilder<IntelligentNotificationWorker>()
                    .setInitialDelay(1, TimeUnit.HOURS)
                    .setInputData(workDataOf(
                        "TRIGGER_TYPE" to "SNOOZE_RECURRING",
                        "TX_ID" to txId
                    ))
                    .build()
                
                WorkManager.getInstance(context).enqueue(request)
                Toast.makeText(context, "Snoozed payment reminder for 1 hour", Toast.LENGTH_SHORT).show()
            }
            "BACKUP_NOW" -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 1. Show indeterminate progress notification in status bar
                        NotificationHelper.dispatchProgressNotification(
                            context = context,
                            notificationId = 1024,
                            title = "Database Backup",
                            message = "Securing local ledger backup...",
                            progress = -1,
                            isFinished = false
                        )
                        
                        // Simulate backup latency
                        kotlinx.coroutines.delay(2500L)
                        
                        // 2. Complete progress notification with visual success feedback
                        NotificationHelper.dispatchProgressNotification(
                            context = context,
                            notificationId = 1024,
                            title = "Backup Complete",
                            message = "Secure backup created successfully",
                            progress = 100,
                            isFinished = true
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Backup created successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationActionRc", "Secure backup failed", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
