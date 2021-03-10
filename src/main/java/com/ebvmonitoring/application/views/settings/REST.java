package com.ebvmonitoring.application.views.settings;

import com.ebvmonitoring.application.DBConnection;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class REST {
    public void sendPOST(String teilen, String teilen_params) throws IOException {

        try {

            final String POST_PARAMS = teilen_params;

            URL obj = new URL(teilen);
            //Öffnen der Connection
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            //POST-Methode
            con.setRequestMethod("POST");
            //Properties
            con.setRequestProperty("Content-Type", "application/json");
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            con.setDoOutput(true);
            //ermöglicht Bytes an ein "beliebiges" Ziel zu senden
            OutputStream os = con.getOutputStream();
            os.write(POST_PARAMS.getBytes("utf-8"));
            os.flush();


            System.out.println("URL: "+ teilen);
            //Antwortzeit berechnen in ms
            long start = System.currentTimeMillis();
            //Abruf des HTTP-Status
            int responseCode = con.getResponseCode();
            long end = System.currentTimeMillis();
            System.out.println("Status = " + responseCode);
            long zeit = end - start;
            System.out.println("Antwortzeit = " + zeit + " ms");
            //Zuletzt aufgerufen
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            System.out.println("Aufgerufen: "+dtf.format(LocalDateTime.now()));


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
            //schließt Connection
            os.close();

            //INSERT
            try {
                Connection dbcon = DBConnection.callDB();
                PreparedStatement usedb=dbcon.prepareStatement("USE monitoredebv");
                PreparedStatement posted = dbcon.prepareStatement("INSERT INTO rest (url,status,antwortzeit,aufgerufen) VALUES('"+teilen+"','"+responseCode+"','"+zeit+"','"+dtf.format(LocalDateTime.now())+"')");
                usedb.executeUpdate();
                posted.executeUpdate();
            }catch(Exception e) {
                System.out.println(e);
            }finally {
                System.out.println("Insert Completed.");
            }

        }catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
