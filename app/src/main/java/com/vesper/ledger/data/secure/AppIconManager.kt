package com.vesper.ledger.data.secure

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object AppIconManager {

    val iconKeys = listOf("default", "flux", "material", "glass", "coin", "analytics", "vault", "ledger")

    fun setAppIcon(context: Context, iconKey: String) {
        if (iconKey !in iconKeys) return

        val pm = context.packageManager
        val packageName = context.packageName

        iconKeys.forEach { key ->
            val aliasName = if (key == "default") {
                "$packageName.MainActivityDefault"
            } else {
                "$packageName.MainActivity${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            }
            val component = ComponentName(packageName, aliasName)
            val state = if (key == iconKey) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            
            try {
                pm.setComponentEnabledSetting(
                    component,
                    state,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                Log.e("AppIconManager", "Failed to set state for $aliasName", e)
            }
        }
        Log.d("AppIconManager", "App icon successfully changed to: $iconKey")
    }

    fun getIconForegroundRes(iconKey: String): Int {
        return when (iconKey) {
            "flux" -> com.vesper.ledger.R.drawable.ic_flux_foreground
            "material" -> com.vesper.ledger.R.drawable.ic_material_foreground
            "glass" -> com.vesper.ledger.R.drawable.ic_glass_foreground
            "coin" -> com.vesper.ledger.R.drawable.ic_coin_foreground
            "analytics" -> com.vesper.ledger.R.drawable.ic_analytics_foreground
            "vault" -> com.vesper.ledger.R.drawable.ic_vault_foreground
            "ledger" -> com.vesper.ledger.R.drawable.ic_ledger_foreground
            else -> com.vesper.ledger.R.drawable.ic_launcher_foreground
        }
    }

    fun getIconBackgroundRes(iconKey: String): Int {
        return when (iconKey) {
            "flux" -> com.vesper.ledger.R.drawable.ic_flux_background
            "material" -> com.vesper.ledger.R.drawable.ic_material_background
            "glass" -> com.vesper.ledger.R.drawable.ic_glass_background
            "coin" -> com.vesper.ledger.R.drawable.ic_coin_background
            "analytics" -> com.vesper.ledger.R.drawable.ic_analytics_background
            "vault" -> com.vesper.ledger.R.drawable.ic_vault_background
            "ledger" -> com.vesper.ledger.R.drawable.ic_ledger_background
            else -> com.vesper.ledger.R.drawable.ic_launcher_background
        }
    }
}
