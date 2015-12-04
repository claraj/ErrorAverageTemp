package com.clara;

import java.sql.*;
import java.util.LinkedList;

/**
 * Created by we4954cp on 12/4/2015.
 */
public class DB {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";        //Configure the driver needed
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/temp";     //Connection string – where's the database?
    static final String USER = "root";   //TODO replace with your username
    static final String PASSWORD = "itecitec";   //TODO replace with your password

    Connection conn = null;

    //Store all of the Statements and PreparedStatements in a LinkedList, so when they need to be closed,
    //we can iterate over the list to delete them all. Saves calling .close() on each individual object.
    //This is most useful if you know you'll close many Statements together. Otherwise, it's probably
    //better to close a Statement when you are done with it.
    LinkedList<Statement> allStatements = new LinkedList<Statement>();

    //We could also store all of our ResultSet objects in a list too, but since there's only one I'm not going to.
    //Like Statements, it's a good idea to close a ResultSet when you don't need it any more.
    ResultSet allDataResultSet = null;

    final static String MAX_TEMP_COL = "maxtemp";
    final static String MIN_TEMP_COL = "mintemp";

    public void openConnection() {


        try {
            //Instantiate the driver
            Class.forName(JDBC_DRIVER);

        } catch (ClassNotFoundException cnfe) {
            System.out.println("Can't instantiate driver class; check you have drives and classpath configured correctly?");
            cnfe.printStackTrace();
            System.exit(-1);  //No driver? Need to fix before anything else will work. So quit the program
        }

        try {
            conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);

        } catch (SQLException sqle) {
            System.out.println("Can't create connection to database");
            System.out.println(sqle);
            System.exit(-1);
        }

        //Create table to use. Delete it and create it fresh if it already exists

        //This table will store dates, and the min and max temperatures recorded on those dates.

        String createTableSQL = "CREATE TABLE temp (day date, " + MIN_TEMP_COL + " double, " + MAX_TEMP_COL + " double)";
        String deleteTableSQL = "DROP TABLE temp";

        Statement createTableStatement = null;

        try {
            createTableStatement = conn.createStatement();
            allStatements.add(createTableStatement);
            createTableStatement.executeUpdate(createTableSQL);
            System.out.println("Created temp table");
        } catch (SQLException sqle) {

            System.out.println("Temp table appears to exist already, double check by reading the following exception message.);" +
                    "\nTo start the program with known data, attempt to delete and re-create the table.");
            System.out.println(sqle);

            try {

                createTableStatement.executeUpdate(deleteTableSQL);
                createTableStatement.executeUpdate(createTableSQL);

            } catch (SQLException anotherSqle) {
                //Still didn't work. Print error message and quit.
                System.out.println("Still can't create table, error message follows");
                System.out.println(anotherSqle);
                closeConnection();
                System.exit(-1);
            }
        }
    }

    public void addExampleData() {

        String prepStatInsert = "INSERT INTO temp VALUES ( ?, ?, ? )";

        PreparedStatement psInsert;

        try {
            psInsert = conn.prepareStatement(prepStatInsert);
            allStatements.add(psInsert);

            psInsert.setDate(1, Date.valueOf("2014-04-01"));
            psInsert.setDouble(2, 44.2);
            psInsert.setDouble(3, 58.7);
            psInsert.executeUpdate();

            psInsert.setDate(1, Date.valueOf("2014-04-02"));
            psInsert.setDouble(2, 41.6);
            psInsert.setDouble(3, 55.1);
            psInsert.executeUpdate();

            psInsert.setDate(1, Date.valueOf("2014-04-03"));
            psInsert.setDouble(2, 43.9);
            psInsert.setNull(3, Types.DOUBLE);
                //Forgot to record the max temperature for this date so set it to null.
                //There's no data for this date. We can't add this data. So we have no choice to set it to null.
                //Now, look what happens when we fetch this data and use it in the main program?
            psInsert.executeUpdate();

            psInsert.setDate(1, Date.valueOf("2014-04-04"));
            psInsert.setDouble(2, 43.8);
            psInsert.setDouble(3, 47.2);
            psInsert.executeUpdate();

            System.out.println("Added test data to database");

        } catch (SQLException sqle) {
            //Print message and quit. If these statements are causing SQLException, it's most likely that the connection
            //is closed, the SQL has errors, your table doesn't exist... or something else you can fix.
            //So, quit the program - so that you notice that there is an error - and fix the problem.
            System.out.println("Error inserting example data");
            System.out.println(sqle);
            closeConnection();
            System.exit(-1);
        }

    }

    public ResultSet fetchAllData() {

        try {
            String fetchTempsSQL = "SELECT mintemp, maxtemp FROM temp ";
            Statement fetchAllData = conn.createStatement();
            allStatements.add(fetchAllData);
            allDataResultSet = fetchAllData.executeQuery(fetchTempsSQL);
            return allDataResultSet;

        } catch (SQLException sqle) {
            System.out.println("Error fetching all data");
            System.out.println(sqle);
            return null;
        }
    }

    public void closeConnection() {

        try {
            if (allDataResultSet != null) {
                allDataResultSet.close();  //Close result set
                System.out.println("ResultSet closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        //Close all of the statements.
        //Stored a reference to each Statement (which includes PreparedStatements) in allStatements
        //when they were created, so we can loop over all of them and close them all.

        for (Statement s : allStatements) {

            if (s != null) {
                try {
                    s.close();
                    System.out.println("Statement closed");
                } catch (SQLException se) {
                    System.out.println("Error closing statement");
                    se.printStackTrace();
                }
            }
        }

        try {
            if (conn != null) {
                conn.close();  //Close connection to database
                System.out.println("Database connection closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

}
