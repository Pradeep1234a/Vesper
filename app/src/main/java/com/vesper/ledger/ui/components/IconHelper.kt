package com.vesper.ledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconByName(name: String): ImageVector {
    return when (name) {
        "work" -> Icons.Default.Work
        "trending_up" -> Icons.Default.TrendingUp
        "card_giftcard" -> Icons.Default.CardGiftcard
        "restaurant" -> Icons.Default.Restaurant
        "home" -> Icons.Default.Home
        "bolt" -> Icons.Default.Bolt
        "directions_car" -> Icons.Default.DirectionsCar
        "sports_esports" -> Icons.Default.SportsEsports
        "shopping_bag" -> Icons.Default.ShoppingBag
        "medical_services" -> Icons.Default.MedicalServices
        "more_horiz" -> Icons.Default.MoreHoriz
        "category" -> Icons.Default.Category
        "settings" -> Icons.Default.Settings
        "savings" -> Icons.Default.Savings
        "search" -> Icons.Default.Search
        "add" -> Icons.Default.Add
        "delete" -> Icons.Default.Delete
        "calendar_today" -> Icons.Default.CalendarToday
        "arrow_back" -> Icons.Default.ArrowBack
        else -> Icons.Default.Category
    }
}
