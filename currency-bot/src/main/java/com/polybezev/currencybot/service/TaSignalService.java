package com.polybezev.currencybot.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaSignalService {

    private final RestTemplate restTemplate;

    public record SignalResult(String coin, double rsi, double macd, double macdSignal,
                               String signal) {}

    public SignalResult analyze(String coinId, String coinName) {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId +
                "/ohlc?vs_currency=usd&days=14";
        String json = restTemplate.getForObject(url, String.class);

        JsonArray ohlc = JsonParser.parseString(json).getAsJsonArray();
        BaseBarSeries series = new BaseBarSeries(coinId);

        for (var el : ohlc) {
            JsonArray bar = el.getAsJsonArray();
            long timestamp = bar.get(0).getAsLong();
            double open    = bar.get(1).getAsDouble();
            double high    = bar.get(2).getAsDouble();
            double low     = bar.get(3).getAsDouble();
            double close   = bar.get(4).getAsDouble();

            ZonedDateTime time = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC);
            series.addBar(BaseBar.builder()
                    .timePeriod(Duration.ofMinutes(30))
                    .endTime(time)
                    .openPrice(series.numOf(open))
                    .highPrice(series.numOf(high))
                    .lowPrice(series.numOf(low))
                    .closePrice(series.numOf(close))
                    .volume(series.numOf(0))
                    .build());
        }

        int last = series.getEndIndex();
        ClosePriceIndicator close = new ClosePriceIndicator(series);

        RSIIndicator rsiInd = new RSIIndicator(close, 14);
        MACDIndicator macdInd = new MACDIndicator(close, 12, 26);
        EMAIndicator signalLine = new EMAIndicator(macdInd, 9);

        double rsi = rsiInd.getValue(last).doubleValue();
        double macd = macdInd.getValue(last).doubleValue();
        double macdSig = signalLine.getValue(last).doubleValue();

        String signal;
        if (rsi < 30 && macd > macdSig) signal = "🟢 BUY";
        else if (rsi > 70 && macd < macdSig) signal = "🔴 SELL";
        else if (rsi < 45 && macd > macdSig) signal = "🟡 WEAK BUY";
        else if (rsi > 55 && macd < macdSig) signal = "🟡 WEAK SELL";
        else signal = "⚪ HOLD";

        return new SignalResult(coinName, rsi, macd, macdSig, signal);
    }
}
