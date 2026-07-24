package com.vesper.ledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

val ICON_CATEGORIES = mapOf(
    "Food & Dining" to listOf(
        "restaurant", "pizza", "coffee", "bakery", "icecream", "bar", "wine", "cake", "egg", "fastfood",
        "ramen", "soup", "breakfast", "brunch", "dinner", "liquor", "beverage", "kitchen", "takeout", "kebab",
        "local_dining", "lunch", "burger", "cookie", "apple", "tea", "donut", "cocktail", "pub", "supermarket"
    ),
    "Shopping & Fashion" to listOf(
        "shopping_bag", "shopping_cart", "storefront", "mall", "sell", "receipt", "tag", "store", "loyalty", "checkroom",
        "toy", "florist", "giftcard", "basket", "gift", "camera", "laptop", "phone", "watch", "palette",
        "diamond", "glasses", "shirt", "dress", "shoes", "umbrella", "backpack", "gem", "ring", "crown"
    ),
    "Transport & Travel" to listOf(
        "car", "bus", "run", "bike", "subway", "flight", "taxi", "train", "boat", "tram",
        "motorcycle", "pedal_bike", "ev_station", "gas_station", "map", "commute", "navigation", "hotel", "luggage", "explore",
        "airport", "plane", "compass", "globe", "direction", "parking", "highway", "anchor", "passport", "ticket"
    ),
    "Utilities & Home" to listOf(
        "card", "receipt_long", "bolt", "water", "tv", "router", "android_phone", "home", "work", "mail",
        "call", "antenna", "thermostat", "plumbing", "construction", "handyman", "propane", "electrical", "cleaning", "wind",
        "lightbulb", "key", "lock", "house", "apartment", "garage", "door", "ac", "bed", "chair"
    ),
    "Entertainment & Media" to listOf(
        "gamepad", "movie", "music", "comedy", "video", "brush", "camera_roll", "compass", "casino", "event",
        "ticket", "live_tv", "radio", "headphones", "speaker", "mic", "volume", "play", "forum", "group",
        "theater", "concert", "stadium", "sports", "gaming", "guitar", "piano", "film", "art", "magic"
    ),
    "Health & Fitness" to listOf(
        "medical", "heart", "heart_border", "spa", "fitness", "hospital", "meditation", "virus", "healing", "vaccine",
        "psychology", "elderly", "baby", "pregnant", "accessible", "hands", "mask", "medication", "liquid", "soccer",
        "gym", "pharmacy", "dental", "optics", "doctor", "ambulance", "first_aid", "cross", "wellness", "running"
    ),
    "Education & Career" to listOf(
        "school", "class", "book", "menu_book", "contacts", "cast", "business", "work_outline", "domain", "campaign",
        "leaderboard", "assessment", "timeline", "pie", "chart", "trending_up", "trending_down", "money", "calculate", "analytics",
        "diploma", "desk", "laptop_mac", "science", "trophy", "certificate", "grad_cap", "library", "pen", "notebook"
    ),
    "Financials & Money" to listOf(
        "savings", "bank", "wallet", "paid", "monetization", "exchange", "price_change", "price_check", "payments", "wallet_alt",
        "score", "atm", "profile", "person", "groups", "settings", "lock", "security", "notifications", "star",
        "credit_card", "account_balance", "coin", "currency", "cash", "dollar", "euro", "pound", "rupee", "tax"
    ),
    "Sports & Activities" to listOf(
        "soccer", "basketball", "tennis", "golf", "swimming", "running", "hiking", "fishing", "bowling", "fitness_center",
        "sports_motorsports", "baseball", "volleyball", "skateboarding", "surfing", "martial_arts", "trophy", "medal", "champion", "badge"
    ),
    "Tech & Gadgets" to listOf(
        "laptop", "phone", "tablet", "desktop", "memory", "cpu", "battery", "wifi", "bluetooth", "cloud",
        "code", "terminal", "hard_drive", "printer", "scanner", "camera", "drone", "tv", "smart_home", "headphones"
    ),
    "Family & Life" to listOf(
        "child", "baby", "family", "pets", "dog", "cat", "house", "yard", "garden", "party",
        "birthday", "wedding", "ring", "gift", "photo", "album", "holiday", "vacation", "sunset", "beach"
    ),
    "Tools & Office" to listOf(
        "briefcase", "folder", "file", "print", "mail", "send", "edit", "pen", "paperclip", "calendar",
        "clock", "alarm", "timer", "calculator", "ruler", "scissors", "pin", "flag", "bookmark", "archive"
    )
)

fun getIconByName(name: String): ImageVector {
    return when (name.lowercase().trim()) {
        // Food & Dining
        "restaurant" -> Icons.Outlined.Restaurant
        "pizza" -> Icons.Outlined.LocalPizza
        "coffee" -> Icons.Outlined.Coffee
        "bakery" -> Icons.Outlined.BakeryDining
        "icecream" -> Icons.Outlined.Icecream
        "bar" -> Icons.Outlined.LocalBar
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
        "beverage", "tea" -> Icons.Outlined.EmojiFoodBeverage
        "kitchen" -> Icons.Outlined.Kitchen
        "takeout" -> Icons.Outlined.TakeoutDining
        "kebab" -> Icons.Outlined.KebabDining
        "local_dining", "lunch" -> Icons.Outlined.LocalDining
        "burger" -> Icons.Outlined.LunchDining
        "cookie", "donut" -> Icons.Outlined.Cookie
        "apple" -> Icons.Outlined.Apple
        "cocktail", "pub" -> Icons.Outlined.Liquor
        "supermarket" -> Icons.Outlined.Store

        // Shopping & Fashion
        "shopping_bag" -> Icons.Outlined.ShoppingBag
        "shopping_cart" -> Icons.Outlined.ShoppingCart
        "storefront" -> Icons.Outlined.Storefront
        "mall" -> Icons.Outlined.LocalMall
        "sell" -> Icons.Outlined.Sell
        "receipt" -> Icons.Outlined.Receipt
        "tag" -> Icons.Outlined.Tag
        "store" -> Icons.Outlined.Store
        "loyalty" -> Icons.Outlined.Loyalty
        "checkroom", "shirt", "dress" -> Icons.Outlined.Checkroom
        "toy" -> Icons.Outlined.SmartToy
        "florist" -> Icons.Outlined.LocalFlorist
        "giftcard", "gift" -> Icons.Outlined.CardGiftcard
        "basket" -> Icons.Outlined.ShoppingBasket
        "camera" -> Icons.Outlined.Camera
        "laptop", "laptop_mac", "desktop" -> Icons.Outlined.Laptop
        "phone", "android_phone", "tablet" -> Icons.Outlined.Phonelink
        "watch", "smartwatch" -> Icons.Outlined.Watch
        "palette", "art" -> Icons.Outlined.Palette
        "diamond", "gem" -> Icons.Outlined.Diamond
        "glasses" -> Icons.Outlined.Glasses
        "shoes", "backpack" -> Icons.Outlined.ShoppingBag
        "umbrella" -> Icons.Outlined.Umbrella
        "ring", "crown" -> Icons.Outlined.CardGiftcard

        // Transport & Travel
        "car", "taxi", "highway" -> Icons.Outlined.DirectionsCar
        "bus" -> Icons.Outlined.DirectionsBus
        "run", "running" -> Icons.Outlined.DirectionsRun
        "bike", "pedal_bike" -> Icons.Outlined.DirectionsBike
        "subway", "train", "tram" -> Icons.Outlined.DirectionsSubway
        "flight", "airport", "plane" -> Icons.Outlined.Flight
        "boat", "anchor" -> Icons.Outlined.DirectionsBoat
        "motorcycle" -> Icons.Outlined.TwoWheeler
        "ev_station" -> Icons.Outlined.EvStation
        "gas_station", "parking" -> Icons.Outlined.LocalGasStation
        "map", "direction", "navigation" -> Icons.Outlined.Map
        "commute" -> Icons.Outlined.Commute
        "hotel" -> Icons.Outlined.Hotel
        "luggage", "passport" -> Icons.Outlined.Luggage
        "explore", "compass", "globe" -> Icons.Outlined.Explore

        // Utilities & Home
        "card", "credit_card" -> Icons.Outlined.CreditCard
        "receipt_long" -> Icons.Outlined.ReceiptLong
        "bolt", "electrical", "lightbulb" -> Icons.Outlined.Bolt
        "water" -> Icons.Outlined.WaterDrop
        "tv", "live_tv" -> Icons.Outlined.Tv
        "router", "wifi" -> Icons.Outlined.Router
        "home", "house", "apartment" -> Icons.Outlined.Home
        "work", "business", "briefcase" -> Icons.Outlined.Work
        "mail" -> Icons.Outlined.Mail
        "call" -> Icons.Outlined.Call
        "antenna" -> Icons.Outlined.Antenna
        "thermostat", "ac" -> Icons.Outlined.Thermostat
        "plumbing" -> Icons.Outlined.Plumbing
        "construction", "handyman" -> Icons.Outlined.Construction
        "propane" -> Icons.Outlined.Propane
        "cleaning" -> Icons.Outlined.CleaningServices
        "wind" -> Icons.Outlined.Air
        "key", "lock" -> Icons.Outlined.Key
        "garage", "door" -> Icons.Outlined.DoorSliding
        "bed", "chair" -> Icons.Outlined.Bed

        // Entertainment & Media
        "gamepad", "gaming" -> Icons.Outlined.SportsEsports
        "movie", "film", "theater" -> Icons.Outlined.Movie
        "music", "guitar", "piano" -> Icons.Outlined.MusicNote
        "comedy" -> Icons.Outlined.TheaterComedy
        "video" -> Icons.Outlined.Videocam
        "brush" -> Icons.Outlined.Brush
        "camera_roll" -> Icons.Outlined.CameraRoll
        "casino" -> Icons.Outlined.Casino
        "event", "concert" -> Icons.Outlined.Event
        "ticket" -> Icons.Outlined.ConfirmationNumber
        "radio" -> Icons.Outlined.Radio
        "headphones" -> Icons.Outlined.Headphones
        "speaker" -> Icons.Outlined.Speaker
        "mic" -> Icons.Outlined.Mic
        "volume" -> Icons.Outlined.VolumeUp
        "play" -> Icons.Outlined.PlayArrow
        "forum" -> Icons.Outlined.Forum
        "group", "family" -> Icons.Outlined.Group
        "magic" -> Icons.Outlined.AutoAwesome

        // Health & Fitness
        "medical", "doctor", "hospital", "first_aid" -> Icons.Outlined.MedicalServices
        "heart", "heart_border", "wellness" -> Icons.Outlined.Favorite
        "spa" -> Icons.Outlined.Spa
        "fitness", "gym", "fitness_center" -> Icons.Outlined.FitnessCenter
        "meditation" -> Icons.Outlined.SelfImprovement
        "virus" -> Icons.Outlined.Coronavirus
        "healing", "cross" -> Icons.Outlined.Healing
        "vaccine", "medication", "pharmacy", "liquid" -> Icons.Outlined.Vaccines
        "psychology" -> Icons.Outlined.Psychology
        "elderly" -> Icons.Outlined.Elderly
        "baby", "child" -> Icons.Outlined.ChildCare
        "pregnant" -> Icons.Outlined.PregnantWoman
        "accessible" -> Icons.Outlined.Accessible
        "hands" -> Icons.Outlined.CleanHands
        "mask" -> Icons.Outlined.Masks
        "dental" -> Icons.Outlined.MedicalServices
        "optics" -> Icons.Outlined.RemoveRedEye
        "ambulance" -> Icons.Outlined.LocalHospital

        // Education & Career
        "school", "grad_cap" -> Icons.Outlined.School
        "class", "desk" -> Icons.Outlined.Class
        "book", "menu_book", "notebook", "library" -> Icons.Outlined.Book
        "contacts" -> Icons.Outlined.Contacts
        "cast" -> Icons.Outlined.Cast
        "work_outline" -> Icons.Outlined.WorkOutline
        "domain" -> Icons.Outlined.Domain
        "campaign" -> Icons.Outlined.Campaign
        "leaderboard", "analytics" -> Icons.Outlined.Leaderboard
        "assessment" -> Icons.Outlined.Assessment
        "timeline" -> Icons.Outlined.Timeline
        "pie", "chart" -> Icons.Outlined.PieChart
        "trending_up" -> Icons.Outlined.TrendingUp
        "trending_down" -> Icons.Outlined.TrendingDown
        "money", "coin", "cash", "currency", "dollar", "euro", "pound", "rupee" -> Icons.Outlined.AttachMoney
        "calculate" -> Icons.Outlined.Calculate
        "diploma", "certificate" -> Icons.Outlined.CardMembership
        "science" -> Icons.Outlined.Science
        "trophy", "champion", "medal" -> Icons.Outlined.EmojiEvents

        // Financials & Money
        "savings" -> Icons.Outlined.Savings
        "bank", "account_balance" -> Icons.Outlined.AccountBalance
        "wallet", "wallet_alt" -> Icons.Outlined.AccountBalanceWallet
        "paid" -> Icons.Outlined.Paid
        "monetization" -> Icons.Outlined.MonetizationOn
        "exchange", "transfer" -> Icons.Outlined.CurrencyExchange
        "price_change" -> Icons.Outlined.PriceChange
        "price_check" -> Icons.Outlined.PriceCheck
        "payments" -> Icons.Outlined.Payments
        "score" -> Icons.Outlined.Score
        "atm" -> Icons.Outlined.Atm
        "profile", "person" -> Icons.Outlined.Person
        "groups" -> Icons.Outlined.Groups
        "settings" -> Icons.Outlined.Settings
        "security" -> Icons.Outlined.Security
        "notifications" -> Icons.Outlined.Notifications
        "star" -> Icons.Outlined.Star
        "tax" -> Icons.Outlined.Receipt

        // Sports & Activities
        "soccer" -> Icons.Outlined.SportsSoccer
        "basketball" -> Icons.Outlined.SportsBasketball
        "tennis" -> Icons.Outlined.SportsTennis
        "golf" -> Icons.Outlined.SportsGolf
        "swimming" -> Icons.Outlined.Pool
        "hiking" -> Icons.Outlined.Hiking
        "fishing" -> Icons.Outlined.Phishing
        "bowling" -> Icons.Outlined.SportsBaseball
        "sports_motorsports" -> Icons.Outlined.SportsMotorsports
        "baseball" -> Icons.Outlined.SportsBaseball
        "volleyball" -> Icons.Outlined.SportsVolleyball
        "skateboarding" -> Icons.Outlined.Skateboarding
        "surfing" -> Icons.Outlined.Surfing
        "martial_arts" -> Icons.Outlined.SportsKabaddi
        "badge" -> Icons.Outlined.Badge

        // Tech & Gadgets
        "memory", "cpu" -> Icons.Outlined.Memory
        "battery" -> Icons.Outlined.BatteryFull
        "bluetooth" -> Icons.Outlined.Bluetooth
        "cloud" -> Icons.Outlined.Cloud
        "code", "terminal" -> Icons.Outlined.Code
        "hard_drive" -> Icons.Outlined.Storage
        "printer" -> Icons.Outlined.Printer
        "scanner" -> Icons.Outlined.Scanner
        "drone" -> Icons.Outlined.AirplanemodeActive
        "smart_home" -> Icons.Outlined.SmartButton

        // Family & Life
        "pets", "dog", "cat" -> Icons.Outlined.Pets
        "yard", "garden" -> Icons.Outlined.Yard
        "party", "birthday", "holiday", "vacation" -> Icons.Outlined.Celebration
        "wedding" -> Icons.Outlined.Favorite
        "photo", "album" -> Icons.Outlined.Photo
        "sunset" -> Icons.Outlined.WbSunny
        "beach" -> Icons.Outlined.BeachAccess

        // Tools & Office
        "folder" -> Icons.Outlined.Folder
        "file" -> Icons.Outlined.InsertDriveFile
        "print" -> Icons.Outlined.Print
        "send" -> Icons.Outlined.Send
        "edit" -> Icons.Outlined.Edit
        "pen" -> Icons.Outlined.Create
        "paperclip" -> Icons.Outlined.AttachFile
        "calendar" -> Icons.Outlined.CalendarToday
        "clock", "time" -> Icons.Outlined.AccessTime
        "alarm" -> Icons.Outlined.Alarm
        "timer" -> Icons.Outlined.Timer
        "ruler" -> Icons.Outlined.Straighten
        "scissors" -> Icons.Outlined.ContentCut
        "pin" -> Icons.Outlined.PushPin
        "flag" -> Icons.Outlined.Flag
        "bookmark" -> Icons.Outlined.Bookmark
        "archive" -> Icons.Outlined.Archive

        else -> Icons.Outlined.Category
    }
}
