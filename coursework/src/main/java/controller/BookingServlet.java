package controller;

import dao.BookingDAOImpl;
import dao.FlightDAOImpl;
import model.Booking;
import model.Flight;
import model.User;

import jakarta.servlet.http.*;
import java.io.IOException;

public class BookingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int flightId = Integer.parseInt(req.getParameter("flightId"));
        User user = (User) req.getSession().getAttribute("user");
        req.getSession().setAttribute("bookingMessage", "Успешно забронировано!");
        resp.sendRedirect("/flights");

        if (user == null) {
            resp.sendRedirect("/login.html");
            return;
        }

        BookingDAOImpl bookingDAO = BookingDAOImpl.getInstance();
        if (bookingDAO.isAlreadyBooked(user.getId(), flightId)) {
            req.getSession().setAttribute("bookingMessage", "Вы уже бронировали этот рейс.");
        } else {
            Booking booking = new Booking(user.getId(), flightId);
            bookingDAO.addBooking(booking);
            req.getSession().setAttribute("bookingMessage", "✅ Успешно забронировано.");
        }
        resp.sendRedirect("/flights");

    }
}
