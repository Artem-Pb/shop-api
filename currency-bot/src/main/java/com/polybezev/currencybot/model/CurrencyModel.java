package com.polybezev.currencybot.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class CurrencyModel {

    @SerializedName("Date")
    private Date date;

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

    public Double getDiff() {
        if (value == null) {
            return null;
        }

        return value - previous;
    }

    public Double getPercentChange() {
        if (value == null || previous == null || previous == 0.0) {
            return null;
        }
        return ((value / previous) - 1) * 100;
    }

    public String getChangeSymbol() {
        Double diff = getDiff();
        if (diff == null) return "";
        return diff >= 0 ? "📈" : "📉";
    }

    public String getFormattedDiff() {
        Double diff = getDiff();
        if (diff == null) return "—";
        return String.format("%+.4f", diff);
    }

    public String getFormattedPercentChange() {
        Double percent = getPercentChange();
        if (percent == null) return "—";
        return String.format("%+.2f%%", percent);
    }

    public String getPrettyDate() {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        return sdf.format(date);
    }
}
