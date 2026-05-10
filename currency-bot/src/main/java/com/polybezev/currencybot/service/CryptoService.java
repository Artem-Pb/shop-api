package com.polybezev.currencybot.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.polybezev.currencybot.model.CryptoPriceModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Service
public class CryptoService {

    @Cacheable("crypto")
    public CryptoPriceModel getCryptoPrice(String coinId) throws IOException {
        URL url = new URL("https://api.coingecko.com/api/v3/simple/price?ids="
                + coinId + "&vs_currencies=rub,usd&include_24hr_change=true");

        JsonObject root = JsonParser.parseReader(
                new InputStreamReader(url.openStream())
        ).getAsJsonObject();

        JsonObject coin = root.getAsJsonObject(coinId);

        CryptoPriceModel model = new CryptoPriceModel();

        model.setPriceRub(coin.get("rub").getAsDouble());
        model.setPriceUsd(coin.get("usd").getAsDouble());
        model.setChange24h(coin.get("rub_24h_change").getAsDouble());

        return model;
    }
}
