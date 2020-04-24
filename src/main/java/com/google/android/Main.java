package com.google.android;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Main {

    private static final String GOOGLE_PLAY_URL = "https://play.google.com";
    private static final String TOP_CHARTS_URL = GOOGLE_PLAY_URL + "/store/apps/top";
    private static final Path APP_DETAILS_OUTPUT_PATH =
            Paths.get(System.getProperty("user.home") + "/playstore-top-app-details.tsv");
    private static final String HEADER = String.join("\t",
            "APP_NAME", "NUM_REVIEWS", "LAST_UPDATED", "DAYS_SINCE_LAST_UPDATE", "SCORE") +
            System.lineSeparator();

    public static void main(String[] args) throws IOException {
        // clean up any existing file
        Files.deleteIfExists(APP_DETAILS_OUTPUT_PATH);
        Files.createFile(APP_DETAILS_OUTPUT_PATH);
        Files.write(APP_DETAILS_OUTPUT_PATH, HEADER.getBytes(), StandardOpenOption.APPEND);
        // parse app name and its link from top apps page
        Map<String, String> topAppsNameVsLink = parseTopAppsNameVsLink();
        for (Map.Entry<String, String> appNameVsLink : topAppsNameVsLink.entrySet()) {
            // parse app details for each app
            AppDetails appDetails = parseAppDetails(appNameVsLink.getKey(), appNameVsLink.getValue());
            Files.write(APP_DETAILS_OUTPUT_PATH, appDetails.toString().getBytes(), StandardOpenOption.APPEND);
        }
    }


    private static Map<String, String> parseTopAppsNameVsLink() {
        Document doc = null;
        try {
            doc = Jsoup.connect(TOP_CHARTS_URL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;

        Elements appDivs = doc.select("div.b8cIId.ReQCgd.Q9MA7b");
        Map<String, String> appNameVsLink = new HashMap<>();
        for (Element appDiv : appDivs) {
            String appName = appDiv.selectFirst("div.WsMG1c.nnK0zc").attr("title");
            String appLink = appDiv.selectFirst("a").attr("href");
            appNameVsLink.put(appName, appLink);
        }
        return appNameVsLink;
    }

    private static AppDetails parseAppDetails(String appName, String appLink) {
        Document doc = null;
        try {
            doc = Jsoup.connect(GOOGLE_PLAY_URL + appLink).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert doc != null;

        String numReviews = doc.selectFirst("span.AYi5wd.TBRnV span").text();
        Optional<Element> updatedDiv = doc.select("div.hAyfc").stream()
                .filter(elem -> elem.text().contains("Updated"))
                .findFirst();

        String lastUpdated = null;
        if (updatedDiv.isPresent()) {
            lastUpdated = updatedDiv.get().select("span.htlgb span.htlgb").text();
        }
        assert lastUpdated != null;
        return new AppDetails(appName, numReviews, lastUpdated);
    }

}
