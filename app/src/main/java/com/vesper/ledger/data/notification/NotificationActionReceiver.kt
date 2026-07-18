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
        if (action == "DISMISS_NOTIFICATION") {
            val notifyId = intent.getLongExtra("NOTIFICATION_ID", -1L)
            if (notifyId != -1L) {
                VesperNotificationApi.trackDismissed(context, notifyId)
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
        }
    }
}
