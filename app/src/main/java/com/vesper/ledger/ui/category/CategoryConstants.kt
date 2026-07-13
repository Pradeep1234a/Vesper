package com.vesper.ledger.ui.category

import androidx.compose.ui.graphics.Color

val PRESET_COLORS = listOf(
    "#EF4444", // Red
    "#F97316", // Orange
    "#F59E0B", // Amber
    "#10B981", // Green
    "#06B6D4", // Cyan
    "#3B82F6", // Blue
    "#8B5CF6", // Purple
    "#EC4899", // Pink
    "#64748B", // Slate/Gray
    "#14B8A6", // Teal
    "#A855F7", // Purple-Light
    "#F43F5E"  // Rose
)

val ICON_CATEGORIES = mapOf(
    "Food" to listOf(
        "restaurant", "local_pizza", "lunch_dining", "coffee", "cake", 
        "fastfood", "icecream", "bakery_dining", "local_bar", "wine_bar", "kitchen"
    ),
    "Shopping" to listOf(
        "shopping_bag", "shopping_cart", "storefront", "sell", "credit_card", 
        "wallet", "card_giftcard", "receipt_long", "loyalty", "local_mall"
    ),
    "Transport" to listOf(
        "directions_car", "directions_bus", "directions_bike", "local_taxi", 
        "train", "flight", "directions_boat", "commute", "car_rental", "ev_station"
    ),
    "Travel" to listOf(
        "flight_takeoff", "hotel", "explore", "map", "beach_access", 
        "terrain", "rv_hookup", "luggage", "compass_calibration", "hiking"
    ),
    "Health" to listOf(
        "medical_services", "health_and_safety", "fitness_center", "monitor_heart", 
        "spa", "local_hospital", "vaccines", "psychology", "healing", "dentist"
    ),
    "Bills" to listOf(
        "bolt", "water_drop", "receipt", "tv", "cell_tower", 
        "wifi", "power", "plumbing", "hvac", "heat_pump"
    ),
    "Entertainment" to listOf(
        "sports_esports", "movie", "music_note", "theater_comedy", "sports_soccer", 
        "palette", "casino", "piano", "camera_alt", "brush"
    ),
    "Subscriptions" to listOf(
        "subscriptions", "calendar_today", "event_repeat", "autorenew", 
        "star", "card_membership", "notification_important"
    ),
    "Salary & Income" to listOf(
        "work", "monetization_on", "payments", "account_balance_wallet", 
        "attach_money", "handshake", "currency_rupee", "currency_yen", "euro"
    ),
    "Investments" to listOf(
        "trending_up", "show_chart", "query_stats", "analytics", 
        "pie_chart", "bar_chart", "timeline", "insights", "savings"
    ),
    "Business" to listOf(
        "business", "domain", "store", "work_outline", "assessment", 
        "campaign", "leaderboard", "add_business", "real_estate_agent"
    ),
    "Education" to listOf(
        "school", "menu_book", "history_edu", "class", "science", 
        "psychology_alt", "cast_for_education", "auto_stories"
    ),
    "Technology" to listOf(
        "computer", "smartphone", "devices", "router", "headphones", 
        "watch", "keyboard", "mouse", "developer_board", "memory", "gamepad"
    ),
    "Pets" to listOf(
        "pets", "cruelty_free", "nature", "bug_report"
    ),
    "Government" to listOf(
        "account_balance", "gavel", "policy", "shield", "flag"
    ),
    "Insurance" to listOf(
        "security", "verified_user", "admin_panel_settings", "lock"
    )
)

val RECOMMENDED_EXPENSE_ICONS = listOf(
    "restaurant", "shopping_bag", "directions_car", "bolt", "sports_esports", "medical_services"
)

val RECOMMENDED_INCOME_ICONS = listOf(
    "work", "trending_up", "card_giftcard", "monetization_on"
)
