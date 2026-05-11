package com.polybezev.currencybot.util;

public final class CurrencyFlags {
    private CurrencyFlags() {}

    public static String getFlag(String code) {
        return switch (code) {
            case "USD" -> "🇺🇸";
            case "EUR" -> "🇪🇺";
            case "GBP" -> "🇬🇧";
            case "JPY" -> "🇯🇵";
            case "CNY" -> "🇨🇳";
            case "CHF" -> "🇨🇭";
            case "CAD" -> "🇨🇦";
            case "AUD" -> "🇦🇺";
            case "NZD" -> "🇳🇿";
            case "RUB" -> "🇷🇺";
            case "BYN" -> "🇧🇾";
            case "HKD" -> "🇭🇰";
            case "TRY" -> "🇹🇷";
            case "AED" -> "🇦🇪";
            case "KZT" -> "🇰🇿";
            case "AMD" -> "🇦🇲";
            case "AZN" -> "🇦🇿";
            case "BDT" -> "🇧🇩";
            case "BHD" -> "🇧🇭";
            case "BOB" -> "🇧🇴";
            case "BRL" -> "🇧🇷";
            case "CUP" -> "🇨🇺";
            case "CZK" -> "🇨🇿";
            case "DKK" -> "🇩🇰";
            case "DZD" -> "🇩🇿";
            case "EGP" -> "🇪🇬";
            case "ETB" -> "🇪🇹";
            case "GEL" -> "🇬🇪";
            case "HUF" -> "🇭🇺";
            case "IDR" -> "🇮🇩";
            case "INR" -> "🇮🇳";
            case "IRR" -> "🇮🇷";
            case "KGS" -> "🇰🇬";
            case "KRW" -> "🇰🇷";
            case "MDL" -> "🇲🇩";
            case "MMK" -> "🇲🇲";
            case "MNT" -> "🇲🇳";
            case "NGN" -> "🇳🇬";
            case "NOK" -> "🇳🇴";
            case "OMR" -> "🇴🇲";
            case "PLN" -> "🇵🇱";
            case "QAR" -> "🇶🇦";
            case "RON" -> "🇷🇴";
            case "RSD" -> "🇷🇸";
            case "SAR" -> "🇸🇦";
            case "SEK" -> "🇸🇪";
            case "SGD" -> "🇸🇬";
            case "THB" -> "🇹🇭";
            case "TJS" -> "🇹🇯";
            case "TMT" -> "🇹🇲";
            case "UAH" -> "🇺🇦";
            case "UZS" -> "🇺🇿";
            case "VND" -> "🇻🇳";
            case "ZAR" -> "🇿🇦";
            case "XDR" -> "🌐";
            default -> "•";
        };
    }
}
