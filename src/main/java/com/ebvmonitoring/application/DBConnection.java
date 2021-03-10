package com.ebvmonitoring.application;

import java.sql.*;

public class DBConnection {

    public DBConnection() {
    }

    public static Connection callDB(){

        try{
            //step1 load the driver class
            Class.forName("com.mysql.cj.jdbc.Driver");

            //step2 create  the connection object
            Connection con= DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/?user=root","root","");

            //step3 create the statement object
            Statement stmt=con.createStatement();

            //step4 execute query
            PreparedStatement createdb=con.prepareStatement("CREATE DATABASE IF NOT EXISTS monitoredebv");
            PreparedStatement usedb=con.prepareStatement("USE monitoredebv");
            PreparedStatement create = con.prepareStatement("CREATE TABLE IF NOT EXISTS rest(id int NOT NULL AUTO_INCREMENT, url varchar(255), status int, antwortzeit int, aufgerufen datetime, PRIMARY KEY(id))");
            PreparedStatement createmail = con.prepareStatement("CREATE TABLE IF NOT EXISTS alertemail(id int NOT NULL AUTO_INCREMENT,email varchar(255), PRIMARY KEY(id))");

            createdb.executeUpdate();
            usedb.executeUpdate();
            create.executeUpdate();
            createmail.executeUpdate();

            ResultSet rs=stmt.executeQuery("select url, status, antwortzeit, aufgerufen from REST");


            //step5 close the connection object
            con.close();

            return con;

        }catch(Exception e){ System.out.println(e);}
        return null;
    }
}
