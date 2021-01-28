package com.ebvmonitoring.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestServices {

    public static int responseCode;

    public RequestServices(){

    }

    public static void sendPOST() throws IOException {

        try {

            final String POST_PARAMS = "{\n" + "\"affiliation\": \"S_LEASING\",\r\n" + "\"androidVersion\": \"1.1.8\",\r\n" + "\"language\": \"de\",\r\n" + "\"limitDate\": \"02.04.2019T08:30:00\"" + "\n}";

            URL obj = new URL("https://www.eleasing24.at/rest/app/welcome");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(POST_PARAMS.getBytes());
            os.flush();


            responseCode = con.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString());
            } else {
                System.out.println("POST request not worked");
            }

            os.close();
        }catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
