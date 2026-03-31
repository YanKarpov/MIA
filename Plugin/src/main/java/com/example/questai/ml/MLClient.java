package com.example.questai.ml;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.lang.reflect.Type;
import java.util.Scanner;

public class MLClient {

    private static final String ML_URL = "http://ml-service:8000/rank";
    private final Gson gson = new Gson();

    public List<RankResponse> rank(RankRequest request) {

        try {
            URL url = new URL(ML_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = gson.toJson(request);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            Type listType = new TypeToken<List<RankResponse>>() {}.getType();

            return gson.fromJson(response, listType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}