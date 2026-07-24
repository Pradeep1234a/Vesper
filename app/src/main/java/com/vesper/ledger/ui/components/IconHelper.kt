package com.vesper.ledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

val ICON_CATEGORIES = mapOf(
    "Food" to listOf(
        "restaurant", "pizza", "coffee", "bakery", "icecream", "bar", "wine", "cake", "egg", "fastfood",
        "ramen", "soup", "breakfast", "brunch", "dinner", "liquor", "beverage", "kitchen", "takeout", "kebab"
    ),
    "Shopping" to listOf(
        "shopping_bag", "shopping_cart", "storefront", "mall", "sell", "receipt", "tag", "store", "loyalty", "checkroom",
        "toy", "florist", "giftcard", "basket", "gift", "camera", "laptop", "phone", "watch", "palette"
    ),
    "Transport" to listOf(
        "car", "bus", "run", "bike", "subway", "flight", "taxi", "train", "boat", "tram",
        "motorcycle", "pedal_bike", "ev_station", "gas_station", "map", "commute", "navigation", "hotel", "luggage", "explore"
    ),
    "Utilities" to listOf(
        "card", "receipt_long", "bolt", "water", "tv", "router", "android_phone", "home", "work", "mail",
        "call", "antenna", "thermostat", "plumbing", "construction", "handyman", "propane", "electrical", "cleaning", "wind"
    ),
    "Entertainment" to listOf(
        "gamepad", "movie", "music", "comedy", "video", "brush", "camera_roll", "compass", "casino", "event",
        "ticket", "live_tv", "radio", "headphones", "speaker", "mic", "volume", "play", "forum", "group"
    ),
    "Health" to listOf(
        "medical", "heart", "heart_border", "spa", "fitness", "hospital", "meditation", "virus", "healing", "vaccine",
        "psychology", "elderly", "baby", "pregnant", "accessible", "hands", "mask", "medication", "liquid", "soccer"
    ),
    "Education" to listOf(
        "school", "class", "book", "menu_book", "contacts", "cast", "business", "work_outline", "domain", "campaign",
        "leaderboard", "assessment", "timeline", "pie", "chart", "trending_up", "trending_down", "money", "calculate", "analytics"
    ),
    "Financials" to listOf(
        "savings", "bank", "wallet", "paid", "monetization", "exchange", "price_change", "price_check", "payments", "wallet_alt",
        "score", "atm", "profile", "person", "groups", "settings", "lock", "security", "notifications", "star"
    )
)

fun getIconByName(name: String): ImageVector {
    return when (name.lowercase().trim()) {
        // Food & Dining
        "restaurant" -> Icons.Outlined.Restaurant
        "pizza", "local_pizza" -> Icons.Outlined.LocalPizza
        "coffee" -> Icons.Outlined.Coffee
        "bakery" -> Icons.Outlined.BakeryDining
        "icecream" -> Icons.Outlined.Icecream
        "bar", "local_bar" -> Icons.Outlined.LocalBar
        "wine" -> Icons.Outlined.WineBar
        "cake" -> Icons.Outlined.Cake
        "egg" -> Icons.Outlined.Egg
        "fastfood" -> Icons.Outlined.Fastfood
        "ramen" -> Icons.Outlined.RamenDining
        "soup" -> Icons.Outlined.SoupKitchen
        "breakfast" -> Icons.Outlined.BreakfastDining
        "brunch" -> Icons.Outlined.BrunchDining
        "dinner" -> Icons.Outlined.DinnerDining
        "liquor" -> Icons.Outlined.Liquor
        "beverage" -> Icons.Outlined.EmojiFoodBeverage
        "kitchen" -> Icons.Outlined.Kitchen
        "takeout" -> Icons.Outlined.TakeoutDining
        "kebab" -> Icons.Outlined.KebabDining

        // Shopping & Retail
        "shopping_bag" -> Icons.Outlined.ShoppingBag
        "shopping_cart" -> Icons.Outlined.ShoppingCart
        "storefront" -> Icons.Outlined.Storefront
        "mall", "local_mall" -> Icons.Outlined.LocalMall
        "sell" -> Icons.Outlined.Sell
        "receipt" -> Icons.Outlined.Receipt
        "tag" -> Icons.Outlined.Tag
        "store" -> Icons.Outlined.Store
        "loyalty" -> Icons.Outlined.Loyalty
        "checkroom" -> Icons.Outlined.Checkroom
        "toy" -> Icons.Outlined.SmartToy
        "florist", "local_florist" -> Icons.Outlined.LocalFlorist
        "giftcard", "card_giftcard" -> Icons.Outlined.CardGiftcard
        "basket" -> Icons.Outlined.ShoppingBasket
        "gift" -> Icons.Outlined.CardGiftcard
        "camera" -> Icons.Outlined.Camera
        "laptop" -> Icons.Outlined.Laptop
        "phone" -> Icons.Outlined.Phonelink
        "watch" -> Icons.Outlined.Watch
        "palette" -> Icons.Outlined.Palette

        // Transport & Travel
        "car", "directions_car" -> Icons.Outlined.DirectionsCar
        "bus", "directions_bus" -> Icons.Outlined.DirectionsBus
        "run", "directions_run" -> Icons.Outlined.DirectionsRun
        "bike", "directions_bike" -> Icons.Outlined.DirectionsBike
        "subway", "directions_subway" -> Icons.Outlined.DirectionsSubway
        "flight" -> Icons.Outlined.Flight
        "taxi", "local_taxi" -> Icons.Outlined.LocalTaxi
        "train" -> Icons.Outlined.Train
        "boat", "directions_boat" -> Icons.Outlined.DirectionsBoat
        "tram" -> Icons.Outlined.Tram
        "motorcycle" -> Icons.Outlined.TwoWheeler
        "pedal_bike" -> Icons.Outlined.PedalBike
        "ev_station" -> Icons.Outlined.EvStation
        "gas_station", "local_gas_station" -> Icons.Outlined.LocalGasStation
        "map" -> Icons.Outlined.Map
        "commute" -> Icons.Outlined.Commute
        "navigation" -> Icons.Outlined.Navigation
        "hotel" -> Icons.Outlined.Hotel
        "luggage" -> Icons.Outlined.Luggage
        "explore" -> Icons.Outlined.Explore

        // Utilities & Housing
        "card", "credit_card" -> Icons.Outlined.CreditCard
        "receipt_long" -> Icons.Outlined.ReceiptLong
        "bolt" -> Icons.Outlined.Bolt
        "water" -> Icons.Outlined.WaterDrop
        "tv" -> Icons.Outlined.Tv
        "router" -> Icons.Outlined.Router
        "android_phone" -> Icons.Outlined.PhoneAndroid
        "home" -> Icons.Outlined.Home
        "work", "work_outline" -> Icons.Outlined.WorkOutline
        "mail" -> Icons.Outlined.LocalPostOffice
        "call" -> Icons.Outlined.LocalPhone
        "antenna" -> Icons.Outlined.SettingsInputAntenna
        "thermostat" -> Icons.Outlined.Thermostat
        "plumbing" -> Icons.Outlined.Plumbing
        "construction" -> Icons.Outlined.Construction
        "handyman" -> Icons.Outlined.Handyman
        "propane" -> Icons.Outlined.Propane
        "electrical" -> Icons.Outlined.ElectricalServices
        "cleaning" -> Icons.Outlined.CleaningServices
        "wind" -> Icons.Outlined.WindPower

        // Entertainment & Media
        "gamepad", "sports_esports" -> Icons.Outlined.SportsEsports
        "movie" -> Icons.Outlined.Movie
        "music" -> Icons.Outlined.MusicNote
        "comedy" -> Icons.Outlined.TheaterComedy
        "video" -> Icons.Outlined.VideogameAsset
        "brush" -> Icons.Outlined.Brush
        "camera_roll" -> Icons.Outlined.CameraRoll
        "compass" -> Icons.Outlined.CompassCalibration
        "casino" -> Icons.Outlined.Casino
        "event" -> Icons.Outlined.Event
        "ticket" -> Icons.Outlined.ConfirmationNumber
        "live_tv" -> Icons.Outlined.LiveTv
        "radio" -> Icons.Outlined.Radio
        "headphones" -> Icons.Outlined.Headphones
        "speaker" -> Icons.Outlined.Speaker
        "mic" -> Icons.Outlined.Mic
        "volume" -> Icons.Outlined.VolumeUp
        "play" -> Icons.Outlined.PlayCircle
        "forum" -> Icons.Outlined.Forum
        "group" -> Icons.Outlined.Group

        // Health & Wellness
        "medical", "medical_services" -> Icons.Outlined.MedicalServices
        "heart" -> Icons.Outlined.Favorite
        "heart_border" -> Icons.Outlined.FavoriteBorder
        "spa" -> Icons.Outlined.Spa
        "fitness" -> Icons.Outlined.FitnessCenter
        "hospital", "local_hospital" -> Icons.Outlined.LocalHospital
        "meditation" -> Icons.Outlined.SelfImprovement
        "virus" -> Icons.Outlined.Coronavirus
        "healing" -> Icons.Outlined.Healing
        "vaccine" -> Icons.Outlined.Healing
        "psychology" -> Icons.Outlined.Psychology
        "elderly" -> Icons.Outlined.Elderly
        "baby" -> Icons.Outlined.BabyChangingStation
        "pregnant" -> Icons.Outlined.PregnantWoman
        "accessible" -> Icons.Outlined.Accessible
        "hands" -> Icons.Outlined.CleanHands
        "mask" -> Icons.Outlined.Masks
        "medication" -> Icons.Outlined.Medication
        "liquid" -> Icons.Outlined.Medication
        "soccer" -> Icons.Outlined.SportsSoccer

        // Education & Business
        "school" -> Icons.Outlined.School
        "class" -> Icons.Outlined.Class
        "book" -> Icons.Outlined.AutoStories
        "menu_book" -> Icons.Outlined.MenuBook
        "contacts" -> Icons.Outlined.ImportContacts
        "cast" -> Icons.Outlined.CastForEducation
        "business" -> Icons.Outlined.Business
        "domain" -> Icons.Outlined.Domain
        "campaign" -> Icons.Outlined.Campaign
        "leaderboard" -> Icons.Outlined.Leaderboard
        "assessment" -> Icons.Outlined.Assessment
        "timeline" -> Icons.Outlined.Timeline
        "pie" -> Icons.Outlined.PieChart
        "chart" -> Icons.Outlined.ShowChart
        "trending_up" -> Icons.Outlined.TrendingUp
        "trending_down" -> Icons.Outlined.TrendingDown
        "money" -> Icons.Outlined.AttachMoney
        "calculate" -> Icons.Outlined.Calculate
        "analytics" -> Icons.Outlined.Analytics

        // Financials & Accounts
        "savings" -> Icons.Outlined.Savings
        "bank", "account_balance" -> Icons.Outlined.AccountBalance
        "wallet", "account_balance_wallet" -> Icons.Outlined.AccountBalanceWallet
        "paid" -> Icons.Outlined.Paid
        "monetization" -> Icons.Outlined.MonetizationOn
        "exchange" -> Icons.Outlined.CurrencyExchange
        "price_change" -> Icons.Outlined.PriceChange
        "price_check" -> Icons.Outlined.PriceCheck
        "payments" -> Icons.Outlined.Payments
        "wallet_alt" -> Icons.Outlined.Wallet
        "score" -> Icons.Outlined.CreditScore
        "atm" -> Icons.Outlined.Atm
        "profile" -> Icons.Outlined.AccountBox
        "person" -> Icons.Outlined.Person
        "groups" -> Icons.Outlined.Groups
        "settings" -> Icons.Outlined.Settings
        "lock" -> Icons.Outlined.Lock
        "security" -> Icons.Outlined.Security
        "notifications" -> Icons.Outlined.Notifications
        "star" -> Icons.Outlined.Star
        "more_horiz" -> Icons.Outlined.MoreHoriz
        "category" -> Icons.Outlined.Category

        else -> Icons.Outlined.Category
    }
}

fun safeParseColor(hex: String?, fallback: Color = Color(0xFF71717A)): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val formattedHex = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(formattedHex))
    } catch (e: Exception) {
        fallback
    }
}
