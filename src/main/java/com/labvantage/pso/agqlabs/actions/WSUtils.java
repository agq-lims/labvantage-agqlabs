package com.labvantage.pso.agqlabs.actions;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import sapphire.action.BaseAction;
import sapphire.util.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WSUtils extends BaseAction {

    public WSUtils() {
    }

    public static String getToken(String urlEndpoint, String clientId, String scope, String method, String clientSecret, String username, String password) throws Exception {
        Logger.logInfo("WSUtils", "WSUtils start...");
        String sAccessToken = "";
        Logger.logInfo("WSUtils URL", urlEndpoint);
        Logger.logInfo("WSUtils clientId", clientId);
        Logger.logInfo("WSUtils scope", scope);
        Logger.logInfo("WSUtils method", method);
        Logger.logInfo("WSUtils clientSecret", clientSecret);
        Logger.logInfo("WSUtils username", username);
        Logger.logInfo("WSUtils password", password);
        String typeString = "grant_type=password&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + scope + "&userName=" + username + "&password=" + password;
        Logger.logInfo("WSUtils typeString", typeString);

        try {
            URL urlToken = new URL(urlEndpoint);
            HttpsURLConnection connToken = (HttpsURLConnection)urlToken.openConnection();
            connToken.setRequestMethod(method);
            connToken.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connToken.setRequestProperty("charset", "utf-8");
            connToken.setDoOutput(true);
            byte[] postData = typeString.getBytes(StandardCharsets.UTF_8);
            OutputStream os = connToken.getOutputStream();
            os.write(postData);
            os.flush();
            os.close();
            StringBuilder sbHTTPResponse = new StringBuilder();
            InputStreamReader ins = new InputStreamReader(connToken.getInputStream());
            BufferedReader in = new BufferedReader(ins);

            String sResponseLine;
            while((sResponseLine = in.readLine()) != null) {
                sbHTTPResponse.append(sResponseLine);
            }

            in.close();
            Logger.logDebug("getToken Method ", (new StringBuffer("HTTP Response : ")).append(sbHTTPResponse.toString()));
            JSONObject jsonHTTPResponse = new JSONObject(sbHTTPResponse.toString());
            sAccessToken = (String)jsonHTTPResponse.get("access_token");
            return sAccessToken;
        } catch (Exception var18) {
            Logger.logError("Exception! " + var18.getMessage());
            Logger.logError(var18.getMessage());
            throw new Exception("Exception! " + var18.getMessage());
        }
    }

    public static Object getAnswer(
            String url,
            String method,
            String body,
            String authorization,
            String cookie
    ) throws Exception {

        Logger.logInfo("sEndPointURL", url);
        Logger.logInfo("Method", method);
        Logger.logInfo("Cookie", cookie);

        URL urlAnswer = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlAnswer.openConnection();

        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);

        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("charset", "utf-8");

        conn.setRequestProperty(
                "User-Agent",
                "PostmanRuntime/7.32.3"
        );

        conn.setRequestProperty(
                "Connection",
                "close"
        );

        if (authorization != null && !authorization.isEmpty()) {
            conn.setRequestProperty("Authorization", authorization);
        }

        if (cookie != null && !cookie.isEmpty()) {
            conn.setRequestProperty("Cookie", cookie);
        }

        // ⚠️ SOLO enviar body si NO es GET
        if (!"GET".equalsIgnoreCase(method) && body != null && !body.isEmpty()) {
            byte[] postData = body.getBytes(StandardCharsets.UTF_8);

            Logger.logInfo("Request Body Bytes", String.valueOf(postData.length));

            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setFixedLengthStreamingMode(postData.length);

            conn.setRequestProperty(
                    "Content-Length",
                    String.valueOf(postData.length)
            );

            OutputStream os = conn.getOutputStream();
            os.write(postData);
            os.flush();
            os.close();

        }

        int responseCode = conn.getResponseCode();
        Logger.logInfo("HTTP Response Code", String.valueOf(responseCode));


        if (responseCode != HttpURLConnection.HTTP_OK) {
            BufferedReader err = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
            );

            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = err.readLine()) != null) {
                errorResponse.append(line);
            }

            throw new IOException(
                    "HTTP Error " + responseCode + " - " + errorResponse
            );

        }

        // ✅ HTTP 200 → leer respuesta
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        );

        StringBuilder sb = new StringBuilder();
        String ch;
        while ((ch = reader.readLine()) != null) {
            sb.append(ch);
        }

        String response = sb.toString();

        return new JSONObject(response);
    }



    public static Object getNewAnswer(
            String url,
            String method,
            String body,
            String authorization,
            String cookie
    ) throws Exception {

        Logger.logInfo("sEndPointURL", url);
        Logger.logInfo("Method", method);
        Logger.logInfo("Cookie", cookie);

        RequestConfig config = RequestConfig.custom()
                .setExpectContinueEnabled(false)
                .build();


        CloseableHttpClient client =
                HttpClients.custom()
                        .setDefaultRequestConfig(config)
                        .disableAutomaticRetries()
                        .build();

        try {

            if ("POST".equalsIgnoreCase(method)) {

                HttpPost post = new HttpPost(url);

                // Headers
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Accept", "*/*");
                post.setHeader("charset", "utf-8");

                // Simula Postman
                post.setHeader(
                        "User-Agent",
                        "PostmanRuntime/7.54.0"
                );

                post.setHeader(
                        "Accept-Encoding",
                        "gzip, deflate, br"
                );

                post.setHeader("Connection", "keep-alive");

                post.setHeader(
                        "Postman-Token",
                        UUID.randomUUID().toString()
                );

                if (authorization != null &&
                        !authorization.isEmpty()) {

                    post.setHeader(
                            "Authorization",
                            authorization
                    );
                }

                if (cookie != null &&
                        !cookie.isEmpty()) {

                    post.setHeader(
                            "Cookie",
                            cookie
                    );
                }

                // Body
                if (body != null && !body.isEmpty()) {

                    Logger.logInfo(
                            "Request Body Size",
                            String.valueOf(body.length())
                    );

                    StringEntity entity =
                            new StringEntity(
                                    body,
                                    ContentType.APPLICATION_JSON
                            );

                    post.setEntity(entity);
                }


                HttpResponse response =
                        client.execute(post);

                int responseCode =
                        response.getStatusLine().getStatusCode();

                HttpEntity responseEntity =
                        response.getEntity();

                String responseString =
                        responseEntity != null
                                ? EntityUtils.toString(responseEntity, "UTF-8")
                                : "";


                // ERROR HTTP
                if (responseCode < 200 || responseCode >= 300) {

                    throw new Exception(
                            "HTTP Error "
                                    + responseCode
                                    + " - "
                                    + responseString
                    );
                }

                // JSON vacío
                if (responseString == null ||
                        responseString.trim().isEmpty()) {

                    return new JSONObject();
                }

                return new JSONObject(responseString);
            }

            throw new Exception(
                    "Método HTTP no soportado: " + method
            );

        } catch (Exception e) {

            Logger.logError(
                    "Error consumiendo servicio: "
                            + e.getMessage()
            );

            throw e;

        } finally {

            try {
                client.close();
            } catch (Exception e) {
                Logger.logError(
                        "Error cerrando HttpClient: "
                                + e.getMessage()
                );
            }
        }
    }

}
