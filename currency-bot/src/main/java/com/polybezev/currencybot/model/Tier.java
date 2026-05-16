package com.polybezev.currencybot.model;

public enum Tier {
    FREE(0, "Бесплатно"),
    TIER_1(200, "TIER 1 — Новости и AI"),
    TIER_2(600, "TIER 2 — Торговые сигналы"),
    TIER_3(1500, "TIER 3 — Автоторговля");

    public final int starsPrice;
    public final String label;

    Tier(int starsPrice, String label) {
        this.starsPrice = starsPrice;
        this.label = label;
    }
}
