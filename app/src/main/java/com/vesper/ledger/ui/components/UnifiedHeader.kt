package com.vesper.ledger.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip

@Composable
fun RootHeader(
    title: String,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onMenuClick != null) {
            // Hamburger icon starts exactly at 16.dp matching left edge of cards
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMenuClick.invoke() }
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .then(if (onMenuClick == null) Modifier.padding(start = 16.dp) else Modifier)
        ) {
            DynamicLogo(
                size = 28.dp,
                cornerRadius = 7.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
        
        // Right visual group: Notification (8dp gap) + Avatar (16dp end margin)
        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 16.dp),
                content = actions
            )
        }
    }
}

@Composable
fun ChildHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back icon starts exactly at 16.dp matching left edge of cards
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBackClick() }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.weight(1f)
        )
        
        // Right visual group: Actions aligned with 16dp end margin
        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 16.dp),
                content = actions
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    )
}
