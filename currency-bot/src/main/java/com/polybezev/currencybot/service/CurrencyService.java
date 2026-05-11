package com.polybezev.currencybot.service;

import com.polybezev.currencybot.model.CurrencyListData;
import com.polybezev.currencybot.model.CurrencyListEntry;
import com.polybezev.currencybot.model.CurrencyModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
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
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateStr);
            model.setDate(date);
        } catch (ParseException e) {
            log.warn("Failed to parse date from CBR response: {}", dateStr);
        }

        return model;
    }

    @Cacheable("currencyList")
    public CurrencyListData getCurrencyList() throws IOException {
        URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");

        JsonObject root = JsonParser.parseReader(
                new InputStreamReader(url.openStream())
        ).getAsJsonObject();

        String feedDate = root.get("Date").getAsString();
        JsonObject valutes = root.getAsJsonObject("Valute");

        List<String> codes = new ArrayList<>(valutes.keySet());
        Collections.sort(codes);

        List<CurrencyListEntry> entries = new ArrayList<>();
        for (String code : codes) {
            String name = valutes.getAsJsonObject(code).get("Name").getAsString();
            entries.add(new CurrencyListEntry(code, name));
        }

        return new CurrencyListData(entries, feedDate);
    }

    public double convertCurrency(double amount, String from, String to) throws IOException {
        return fromRub(toRub(amount, from), to);
    }

    private double toRub(double amount, String currency) throws IOException {
        if (currency.equals("RUB")) return amount;
        if (currency.equals("BTC")) return amount * cryptoService.getCryptoPrice("bitcoin").getPriceRub();
        CurrencyModel rate = self.getCurrency(currency);
        return amount * rate.getValue() / rate.getNominal();
    }

    private double fromRub(double amountRub, String currency) throws IOException {
        if (currency.equals("RUB")) return amountRub;
        if (currency.equals("BTC")) return amountRub / cryptoService.getCryptoPrice("bitcoin").getPriceRub();
        CurrencyModel rate = self.getCurrency(currency);
        return amountRub / (rate.getValue() / rate.getNominal());
    }
}
