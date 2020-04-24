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

    private static final byte[] HEADER = (
            String.join("\t",
                    "APP_NAME", "NUM_REVIEWS", "LAST_UPDATED", "DAYS_SINCE_LAST_UPDATE", "SCORE") +
                    System.lineSeparator()
    ).getBytes();

    private static final String APP_DIV_SELECTOR = "div.b8cIId.ReQCgd.Q9MA7b";
    private static final String APP_NAME_SELECTOR = "div.WsMG1c.nnK0zc";
    private static final String APP_LINK_SELECTOR = "a";
    private static final String APP_NUM_REVIEWS_SELECTOR = "span.AYi5wd.TBRnV span";
    private static final String APP_UPDATED_DIV_SELECTOR = "div.hAyfc";
    private static final String APP_UPDATED_SELECTOR = "span.htlgb span.htlgb";

    public static void main(String[] args) throws IOException {
        // clean up any existing file
        Files.deleteIfExists(APP_DETAILS_OUTPUT_PATH);
        Files.createFile(APP_DETAILS_OUTPUT_PATH);
        Files.write(APP_DETAILS_OUTPUT_PATH, HEADER, StandardOpenOption.APPEND);

        // parse app name and its link from top apps page
        Map<String, String> topAppsNameVsLink = parseTopAppsNameVsLink();

        for (Map.Entry<String, String> appNameVsLink : topAppsNameVsLink.entrySet()) {
            // parse app details for each app
            AppDetails appDetails = parseAppDetails(appNameVsLink.getKey(), appNameVsLink.getValue());
            // write app details for each app as tsv (check toString)
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

        Elements appDivs = doc.select(APP_DIV_SELECTOR);
        Map<String, String> appNameVsLink = new HashMap<>();
        for (Element appDiv : appDivs) {
            String appName = appDiv.selectFirst(APP_NAME_SELECTOR).attr("title");
            String appLink = appDiv.selectFirst(APP_LINK_SELECTOR).attr("href");
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

        String numReviews = doc.selectFirst(APP_NUM_REVIEWS_SELECTOR).text();
        Optional<Element> updatedDiv = doc.select(APP_UPDATED_DIV_SELECTOR).stream()
                .filter(elem -> elem.text().contains("Updated"))
                .findFirst();
        assert updatedDiv.isPresent();
        String lastUpdated = updatedDiv.get().select(APP_UPDATED_SELECTOR).text();
        return new AppDetails(appName, numReviews, lastUpdated);
    }
}
