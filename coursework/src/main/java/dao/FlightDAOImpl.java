package dao;

import model.CrewMember;
import model.Flight;
import util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAOImpl implements FlightDAO {

    private static FlightDAOImpl instance;

    public FlightDAOImpl() {}

    public static synchronized FlightDAOImpl getInstance() {
        if (instance == null) {
            instance = new FlightDAOImpl();
        }
        return instance;
    }

    public List<Flight> getAllFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT id, flight_number, departure, destination, departure_time, arrival_time FROM flights";

        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Flight flight = new Flight();
                int flightId = rs.getInt("id");
                flight.setId(flightId);
                flight.setFlightNumber(rs.getString("flight_number"));
                flight.setDeparture(rs.getString("departure"));
                flight.setDestination(rs.getString("destination"));
                flight.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
                flight.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());

                // вот здесь просто вызывай метод напрямую:
                flight.setCrew(getCrewForFlight(flightId));

                flights.add(flight);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return flights;
    }


    public List<CrewMember> getCrewForFlight(int flightId) {
        List<CrewMember> crew = new ArrayList<>();
        String sql = "SELECT cm.id, cm.name, cm.role " +
                "FROM crew_members cm " +
                "JOIN flight_crew fc ON cm.id = fc.crew_member_id " +
                "WHERE fc.flight_id = ?";

        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CrewMember member = new CrewMember();
                member.setId(rs.getInt("id"));
                member.setName(rs.getString("name"));
                member.setRole(rs.getString("role"));
                crew.add(member);
            }

            System.out.println("Экипаж для рейса ID=" + flightId + ": " + crew.size() + " человек");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return crew;
    }
    public Flight getFlightById(int id) {
        String sql = "SELECT * FROM flights WHERE id = ?";
        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Flight flight = new Flight();
                flight.setId(rs.getInt("id"));
                flight.setFlightNumber(rs.getString("flight_number"));
                flight.setDeparture(rs.getString("departure"));
                flight.setDestination(rs.getString("destination"));
                flight.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
                flight.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
                flight.setCrew(getCrewForFlight(id));
                return flight;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
