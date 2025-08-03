import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AirlineTests {

    @Test
    public void testDatabaseConnectionAndInitialData() throws SQLException {
        System.out.println("Executing test: Verifying initial flight data...");
        List<Map<String, Object>> flights = DbUtils.executeQuery("SELECT * FROM flights WHERE flight_number = 'AA101'");
        System.out.println("Query result: " + flights);
        Assert.assertEquals(flights.size(), 1, "Should find exactly one flight with number AA101.");
        Map<String, Object> flightAA101 = flights.get(0);
        Assert.assertEquals(flightAA101.get("origin_airport_code"), "JFK", "Origin airport code should be JFK.");
        Assert.assertEquals(flightAA101.get("available_seats"), 180, "Available seats should be 180.");
        System.out.println("Test passed successfully!");
    }

    @Test
    public void testCreateBookingEndToEnd() throws SQLException {
        System.out.println("Executing test: End-to-end booking workflow...");
        int flightIdToBook = 2;
        String testPnr = "QWE456";

        List<Map<String, Object>> flightBefore = DbUtils.executeQuery("SELECT available_seats FROM flights WHERE flight_id = " + flightIdToBook);
        int initialSeats = (int) flightBefore.get(0).get("available_seats");
        System.out.println("Initial available seats: " + initialSeats);

        try {
            // --- FIX IS HERE: Use executeUpdate for INSERT and UPDATE ---
            DbUtils.executeUpdate("INSERT INTO bookings (flight_id, passenger_id, pnr, booking_status, seat_number, class_of_service, total_fare) VALUES (2, 3, '"+testPnr+"', 'CONFIRMED', '18B', 'Business', 1600.00)");
            DbUtils.executeUpdate("UPDATE flights SET available_seats = available_seats - 1 WHERE flight_id = " + flightIdToBook);
            System.out.println("Booking created and seat count updated in DB.");

            List<Map<String, Object>> flightAfter = DbUtils.executeQuery("SELECT available_seats FROM flights WHERE flight_id = " + flightIdToBook);
            int finalSeats = (int) flightAfter.get(0).get("available_seats");
            System.out.println("Final available seats: " + finalSeats);

            Assert.assertEquals(finalSeats, initialSeats - 1, "The number of available seats should have decreased by one.");
            System.out.println("Test passed successfully!");

        } finally {
            // --- FIX IS HERE: Use executeUpdate for UPDATE and DELETE ---
            DbUtils.executeUpdate("UPDATE flights SET available_seats = " + initialSeats + " WHERE flight_id = " + flightIdToBook);
            DbUtils.executeUpdate("DELETE FROM bookings WHERE pnr = '" + testPnr + "'");
            System.out.println("Database state reverted for test independence.");
        }
    }

    @AfterClass
    public void tearDown() throws SQLException {
        DbUtils.closeConnection();
    }
}