package com.ebvmonitoring.application.views.settings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Config {


    public static void Main() {
        try {
            Auslesen();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Scheduler --> wird jede 15 Minuten abgefragt
        //ruft jede 15 Minuten die Methode Auslesen auf
        Timer t = new Timer();

        t.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    Auslesen();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 0, 900000);
    }

    public static void Auslesen() throws IOException {

        String zeile = "";

        //Einlesen der Config-Datei
        BufferedReader b = new BufferedReader(new FileReader("schnittstellen.cfg"));

        //Überprüfung, ob die Zeile eh nicht leer ist
        while ((zeile = b.readLine()) != null) {
            //wenn die Zeile nicht leer ist und sie auch nicht mit # startet, dann wird überprüft
            if (zeile.length() != 0 && !zeile.startsWith("#")) {
                //teilen (URL) wird auf der 2.Stelle gesplitet, beim Delimiter :
                String[] teilen = zeile.split(":", 2);
                //teilen_params (Parameter) wird auf der 2. Stelle gesplittet, beim Delimiter ;
                String[] teilen_params = zeile.split(";", 2);
                //nochmales splitten von teilen, da sonst die ganze Zeile für die URL mitgenommen wird
                String url = teilen[1].split(";")[0];
                //ob die Zeile mit REST anfängt, wenn ja, greift man auf die REST-Klasse zu
                if (teilen[0].equals("REST") && teilen_params[0].contains("REST")) {
                    //System.out.println("REST-Aufruf");
                    REST restObjekt = new REST();
                    //restObjekt.sendPOST(teilen[1], teilen_params[1]);
                    restObjekt.sendPOST(url, teilen_params[1]);

                } else if (teilen[0].equals("SOAP")) {
                    System.out.println("SOAP-Aufruf");
                } else {
                    System.out.println("Schnittstelle ungültig");
                }
            }
        }
    }
}
