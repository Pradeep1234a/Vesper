package com.vesper.ledger.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vesper.ledger.data.secure.AppIconManager

@Composable
fun DynamicLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    cornerRadius: Dp = 16.dp
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
    val appIcon = sharedPrefs.getString("appIcon", "default") ?: "default"

    val backgroundRes = AppIconManager.getIconBackgroundRes(appIcon)
    val foregroundRes = AppIconManager.getIconForegroundRes(appIcon)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        // Render Background layer
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        // Render Foreground layer
        Image(
            painter = painterResource(id = foregroundRes),
            contentDescription = "Vesper Logo",
            modifier = Modifier.fillMaxSize()
        )
    }
}
