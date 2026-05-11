package com.polybezev.currencybot.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class CurrencyModel {

    @SerializedName("ID")
    private String id;

    @SerializedName("NumCode")
    private String numCode;

    @SerializedName("CharCode")
    private String charCode;

    @SerializedName("Nominal")
    private Integer nominal;

    @SerializedName("Name")
    private String name;

    @SerializedName("Value")
    private Double value;

    @SerializedName("Previous")
    private Double previous;

    @SerializedName("Date")
    private Date date;

    public Double getDiff() {
        if (value == null || previous == null) return null;
        return value - previous;
    }

    public Double getPercentChange() {
        if (value == null || previous == null || previous == 0.0) return null;
        return ((value / previous) - 1) * 100;
    }
}
