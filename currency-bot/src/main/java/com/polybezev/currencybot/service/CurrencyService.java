package com.polybezev.currencybot.service;

import com.polybezev.currencybot.model.CurrencyModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CurrencyService {

    @Autowired
    @Lazy
    private CurrencyService self;

    @Autowired
    private CryptoService cryptoService;

    @Cacheable("currency")
    public CurrencyModel getCurrency(String currencyCode) throws IOException {
        Gson gson = new Gson();
        URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");

        JsonObject root = JsonParser.parseReader(
                new InputStreamReader(url.openStream())
        ).getAsJsonObject();

        String dateStr = root.get("Date").getAsString();

        JsonObject allValutes = root.getAsJsonObject("Valute");

        if (!allValutes.has(currencyCode)) {
            throw new IllegalArgumentException("Валюта '" + currencyCode + "' не найдена!");
        }

        JsonObject currencyJson = allValutes.getAsJsonObject(currencyCode);

        CurrencyModel model = gson.fromJson(currencyJson, CurrencyModel.class);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            Date date = sdf.parse(dateStr);
            model.setDate(date);
        } catch (ParseException e) {
            System.err.println("Ошибка выгрузки даты: " + dateStr);
        }

        return model;
    }

    public double convertCurrency(double amount, String from, String to) throws IOException {
        double rub = toRub(amount, from);
        return fromRub(rub, to);
    }

    @Cacheable("currencyList")
    public String getFormattedCurrencyList() throws IOException {
        URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");

        JsonObject root = JsonParser.parseReader(
                new InputStreamReader(url.openStream())
        ).getAsJsonObject();

        JsonObject valutes = root.getAsJsonObject("Valute");

        StringBuilder result = new StringBuilder();
        result.append("Доступные валюты ЦБ РФ:\n\n");

        List<String> currencyCodes = new ArrayList<>(valutes.keySet());
        Collections.sort(currencyCodes);

        int count = 0;
        for (String code : currencyCodes) {
            JsonObject currency = valutes.getAsJsonObject(code);
            String name = currency.get("Name").getAsString();
//            double value = currency.get("Value").getAsDouble();
//            int nominal = currency.get("Nominal").getAsInt();

            String emoji = getCurrencyEmoji(code);
            result.append(String.format("%s %s - %s\n", emoji, code, name));

            count++;

            if (count >= 60) {
                result.append("\n... и еще ").append(currencyCodes.size() - 30).append(" валют");
                break;
            }
        }

        result.append("\n\nВсего: ").append(valutes.size()).append(" валют");
        result.append("\n Данные на: ").append(root.get("Date").getAsString());
        result.append("\n\n💡 Используйте код валюты для получения курса");
        result.append("\nНапример: USD или /curse USD");

        return result.toString();
    }

    private static String getCurrencyEmoji(String currencyCode) {
        return switch (currencyCode) {
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
            case "BYN" -> "\uD83C\uDDE7\uD83C\uDDFE";
            default -> "•";
        };
    }

    private double toRub(double amount, String currency) throws IOException {
        if (currency.equals("RUB")) {
            return amount;
        } else if (currency.equals("BTC")) {
            return amount * cryptoService.getCryptoPrice("bitcoin").getPriceRub();
        }

        CurrencyModel rate = self.getCurrency(currency);
        return amount * rate.getValue() / rate.getNominal();
    }

    private double fromRub(double amountRub, String currency) throws IOException {
        if (currency.equals("RUB")) {
            return amountRub;
        } else if (currency.equals("BTC")) {
            return amountRub / cryptoService.getCryptoPrice("bitcoin").getPriceRub();
        }

        CurrencyModel rate = self.getCurrency(currency);
        return amountRub / (rate.getValue() / rate.getNominal());
    }
}
