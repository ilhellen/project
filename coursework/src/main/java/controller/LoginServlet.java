package controller;

import dao.UserDAOImpl;
import jakarta.servlet.http.*;
import model.User;

import java.io.IOException;

public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || password == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email и пароль обязательны");
            return;
        }

        User user = UserDAOImpl.getInstance().getUserByEmail(email.trim());

        if (user != null && user.getPasswordHash().equals(password.trim())) {
            req.getSession().setAttribute("user", user);
            resp.sendRedirect("/flights");
        } else {
            resp.getWriter().write("❌ Неверные данные. <a href='/login.html'>Назад</a>");
        }
    }
}
