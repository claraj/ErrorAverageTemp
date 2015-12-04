package com.clara;

import java.sql.*;

public class Temp {

    public static void main(String[] args) {

        DB db = new DB();
        db.openConnection();
        db.addExampleData();

        System.out.println("\n** Average Weather Database Program **\n");

        //Let's calculate the average minimum and average maximum temperature for all the days.
        //Add up all the maximum temperatures and divide by number of days to get average max temperature.
        //Add up all the minimum temperatures and divide by number of days to get average min temperature.

        double sumMaxTemp = 0, averageMaxTemp;
        double sumMinTemp = 0, averageMinTemp;
        int totalDays = 0;

        ResultSet rs = db.fetchAllData();

        try {

            while (rs.next()) {

                //Get the minimum temperature for this row, and add it to a running total
                double thisDayMin = rs.getDouble(DB.MIN_TEMP_COL);
                sumMinTemp += thisDayMin;

                //Get the minimum temperature for this row, and add it to a running total
                double thisDayMax = rs.getDouble(DB.MAX_TEMP_COL);
                sumMaxTemp += thisDayMax;
                totalDays++; //keep track of how many rows processed.
            }

            averageMaxTemp = sumMaxTemp / totalDays;
            averageMinTemp = sumMinTemp / totalDays;

            System.out.println(String.format("Average maximum temperature = %3.3f \nAverage minimum temperature = %3.3f\n" ,
                    averageMaxTemp, averageMinTemp));

            if (averageMaxTemp < averageMinTemp) {
                System.out.println("WHAT? The average maximum temperature should be AT LEAST as large as the average minimum temperature. What happened?");
            } else {
                System.out.println("All calculations seem to be in order.");
            }

        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("\n** End of program **");

        db.closeConnection();

    }
}