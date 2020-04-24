package com.google.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppDetails {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    private final String appName;
    private final long numReviews;
    private Date lastUpdated;
    private final int daysSinceLastUpdate;
    private final float score;

    public AppDetails(String appName, String numReviews, String lastUpdated) {
        this.appName = appName.trim();
        this.numReviews = Long.parseLong(numReviews.replace(",", "").trim());
        try {
            this.lastUpdated = DATE_FORMAT.parse(lastUpdated);
        } catch (ParseException e) {
            e.printStackTrace();
            this.lastUpdated = null;
        }
        this.daysSinceLastUpdate = Math.toIntExact((new Date().getTime() - this.lastUpdated.getTime()) / (24 * 60 * 60 * 1000));
        this.score = this.numReviews / (this.daysSinceLastUpdate * 1.0f);
    }

    @Override
    public String toString() {
        return String.join("\t", appName, String.valueOf(numReviews),
                DATE_FORMAT.format(lastUpdated), String.valueOf(daysSinceLastUpdate),
                String.valueOf(score)) + System.lineSeparator();
    }
}
