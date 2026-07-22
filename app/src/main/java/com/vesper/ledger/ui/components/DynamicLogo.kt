package com.vesper.ledger.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vesper.ledger.R

@Composable
fun DynamicLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    cornerRadius: Dp = 16.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Vesper Logo",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    )
}
