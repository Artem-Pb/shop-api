package com.polybezev.currencybot.model;

import lombok.Data;

@Data
public class CryptoPriceModel {
    private String symbol;
    private double priceRub;
    private double priceUsd;
    private double change24h;
}
