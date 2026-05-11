package com.polybezev.currencybot.service;

import com.polybezev.currencybot.model.CryptoPriceModel;
import com.polybezev.currencybot.model.CurrencyModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {
    private final CryptoService cryptoService;
    private final CurrencyService currencyService;

    public String getMarketSnapshot() throws IOException {
        CryptoPriceModel btc = cryptoService.getCryptoPrice("bitcoin");
        CurrencyModel usd = currencyService.getCurrency("USD");
        CurrencyModel eur = currencyService.getCurrency("EUR");
        CurrencyModel cny = currencyService.getCurrency("CNY");

        return String.format(
                "BTC: %.0f ₽ / %.0f $ (изменение за 24ч: %+.2f%%)\n" +
                        "USD: %.2f ₽\n" +
                        "EUR: %.2f ₽\n" +
                        "CNY: %.2f ₽",
                btc.getPriceRub(), btc.getPriceUsd(), btc.getChange24h(),
                usd.getValue(),
                eur.getValue(),
                cny.getValue()
        );
    }
}
