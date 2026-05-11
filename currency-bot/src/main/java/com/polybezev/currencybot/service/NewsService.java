package com.polybezev.currencybot.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Service
@Slf4j
public class NewsService {

    @Value("${newsapi.api-key}")
    private String apiKey;

    public String getTopNews() throws IOException {
        URL url = new URL(
                "https://newsapi.org/v2/everything?q=bitcoin+crypto" +
                "&sortBy=publishedAt&pageSize=5&language=en&apiKey=" + apiKey
        );

        JsonObject root = JsonParser.parseReader(
                new InputStreamReader(url.openStream())
        ).getAsJsonObject();

        String status = root.get("status").getAsString();
        if (!"ok".equals(status)) {
            log.warn("NewsAPI error: {}", root.get("message").getAsString());
            return "Новости временно недоступны.";
        }

        JsonArray articles = root.getAsJsonArray("articles");
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(5, articles.size());

        for (int i = 0; i < limit; i++) {
            String title =
                    articles.get(i).getAsJsonObject().get("title").getAsString();
            sb.append("• ").append(title).append("\n");
        }

        return sb.toString();
    }
}
