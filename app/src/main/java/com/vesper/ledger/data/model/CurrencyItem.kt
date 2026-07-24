package com.vesper.ledger.data.model

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val flagEmoji: String
)

object CurrencyData {
    val defaultCurrency = CurrencyItem("USD", "$", "US Dollar", "United States", "🇺🇸")

    val currencies = listOf(
        CurrencyItem("USD", "$", "US Dollar", "United States", "🇺🇸"),
        CurrencyItem("INR", "₹", "Indian Rupee", "India", "🇮🇳"),
        CurrencyItem("EUR", "€", "Euro", "European Union", "🇪🇺"),
        CurrencyItem("GBP", "£", "British Pound", "United Kingdom", "🇬🇧"),
        CurrencyItem("JPY", "¥", "Japanese Yen", "Japan", "🇯🇵"),
        CurrencyItem("AUD", "$", "Australian Dollar", "Australia", "🇦🇺"),
        CurrencyItem("CAD", "$", "Canadian Dollar", "Canada", "🇨🇦"),
        CurrencyItem("CHF", "CHF", "Swiss Franc", "Switzerland", "🇨🇭"),
        CurrencyItem("CNY", "¥", "Chinese Yuan", "China", "🇨🇳"),
        CurrencyItem("SGD", "$", "Singapore Dollar", "Singapore", "🇸🇬"),
        CurrencyItem("AED", "د.إ", "UAE Dirham", "United Arab Emirates", "🇦🇪"),
        CurrencyItem("SAR", "﷼", "Saudi Riyal", "Saudi Arabia", "🇸🇦"),
        CurrencyItem("BRL", "R$", "Brazilian Real", "Brazil", "🇧🇷"),
        CurrencyItem("ZAR", "R", "South African Rand", "South Africa", "🇿🇦"),
        CurrencyItem("MXN", "$", "Mexican Peso", "Mexico", "🇲🇽"),
        CurrencyItem("KRW", "₩", "South Korean Won", "South Korea", "🇰🇷"),
        CurrencyItem("RUB", "₽", "Russian Ruble", "Russia", "🇷🇺"),
        CurrencyItem("TRY", "₺", "Turkish Lira", "Turkey", "🇹🇷"),
        CurrencyItem("NZD", "$", "New Zealand Dollar", "New Zealand", "🇳🇿"),
        CurrencyItem("SEK", "kr", "Swedish Krona", "Sweden", "🇸🇪"),
        CurrencyItem("NOK", "kr", "Norwegian Krone", "Norway", "🇳🇴"),
        CurrencyItem("DKK", "kr", "Danish Krone", "Denmark", "🇩🇰"),
        CurrencyItem("PLN", "zł", "Polish Zloty", "Poland", "🇵🇱"),
        CurrencyItem("THB", "฿", "Thai Baht", "Thailand", "🇹🇭"),
        CurrencyItem("IDR", "Rp", "Indonesian Rupiah", "Indonesia", "🇮🇩"),
        CurrencyItem("MYR", "RM", "Malaysian Ringgit", "Malaysia", "🇲🇾"),
        CurrencyItem("PHP", "₱", "Philippine Peso", "Philippines", "🇵🇭"),
        CurrencyItem("VND", "₫", "Vietnamese Dong", "Vietnam", "🇻🇳"),
        CurrencyItem("EGP", "E£", "Egyptian Pound", "Egypt", "🇪🇬"),
        CurrencyItem("NGN", "₦", "Nigerian Naira", "Nigeria", "🇳🇬"),
        CurrencyItem("PKR", "Rs", "Pakistani Rupee", "Pakistan", "🇵🇰"),
        CurrencyItem("BDT", "৳", "Bangladeshi Taka", "Bangladesh", "🇧🇩"),
        CurrencyItem("ILS", "₪", "Israeli New Shekel", "Israel", "🇮🇱"),
        CurrencyItem("ARS", "$", "Argentine Peso", "Argentina", "🇦🇷"),
        CurrencyItem("COP", "$", "Colombian Peso", "Colombia", "🇨🇴"),
        CurrencyItem("CLP", "$", "Chilean Peso", "Chile", "🇨🇱"),
        CurrencyItem("PEN", "S/", "Peruvian Sol", "Peru", "🇵🇪"),
        CurrencyItem("CZK", "Kč", "Czech Koruna", "Czech Republic", "🇨🇿"),
        CurrencyItem("HUF", "Ft", "Hungarian Forint", "Hungary", "🇭🇺"),
        CurrencyItem("RON", "lei", "Romanian Leu", "Romania", "🇷🇴"),
        CurrencyItem("QAR", "﷼", "Qatari Riyal", "Qatar", "🇶🇦"),
        CurrencyItem("KWD", "د.ك", "Kuwaiti Dinar", "Kuwait", "🇰🇼"),
        CurrencyItem("BHD", ".د.ب", "Bahraini Dinar", "Bahrain", "🇧🇭"),
        CurrencyItem("OMR", "﷼", "Omani Rial", "Oman", "🇴🇲")
    )
}
