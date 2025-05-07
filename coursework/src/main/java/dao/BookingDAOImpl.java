package dao;

import model.Booking;
import util.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl {

    private static BookingDAOImpl instance;

    private BookingDAOImpl() {}

    public static BookingDAOImpl getInstance() {
        if (instance == null) {
            instance = new BookingDAOImpl();
        }
        return instance;
    }

    public void addBooking(Booking booking) {
        try (Connection conn = ConnectionPool.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bookings (user_id, flight_id) VALUES (?, ?)"
            );
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getFlightId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAlreadyBooked(int userId, int flightId) {
        try (Connection conn = ConnectionPool.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND flight_id = ?"
            );
            ps.setInt(1, userId);
            ps.setInt(2, flightId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Booking> getBookingsForUser(int userId) {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = ConnectionPool.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM bookings WHERE user_id = ?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking(rs.getInt("user_id"), rs.getInt("flight_id"));
                list.add(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
