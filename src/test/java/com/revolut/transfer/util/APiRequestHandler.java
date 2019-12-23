package com.revolut.transfer.util;

import spark.utils.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.fail;

public class APiRequestHandler {

    private static final String BASE_URL = "http://localhost:4567/";

    public ApiResponse send(final String method, final String path) {
        return send(method, path, null);
    }

    public ApiResponse send(final String method, final String path, final String jsonBody) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(BASE_URL + path);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(method);
            if (jsonBody != null) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                byte[] outputInBytes = jsonBody.getBytes("UTF-8");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(outputInBytes);
                outputStream.close();
            } else {
                httpURLConnection.setDoInput(true);
            }
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200 || httpURLConnection.getResponseCode() == 201) {
                String body = IOUtils.toString(httpURLConnection.getInputStream());
                return new ApiResponse(httpURLConnection.getResponseCode(), body);
            } else {
                return new ApiResponse(httpURLConnection.getResponseCode());
            }
        } catch (IOException ex) {
            fail("Error in sending request: " + ex.getMessage());
            return null;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }
}
