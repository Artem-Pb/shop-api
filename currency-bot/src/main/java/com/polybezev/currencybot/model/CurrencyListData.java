package com.polybezev.currencybot.model;

import java.util.List;

public record CurrencyListData(List<CurrencyListEntry> currencies, String feedDate) {}
