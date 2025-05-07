package controller;

import dao.UserDAOImpl;
import jakarta.servlet.http.*;
import model.User;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8"); // <<< Добавь эту строку
        resp.setContentType("text/html; charset=UTF-8"); // <<< И эту


        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (username == null || email == null || password == null ||
                username.isBlank() || email.isBlank() || password.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Все поля должны быть заполнены");
            return;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(password.trim());

        try {
            boolean success = UserDAOImpl.getInstance().addUser(user);
            if (success) {
                req.getSession().setAttribute("user", user);
                resp.sendRedirect("/flights");
            } else {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Пользователь с таким email уже существует");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при регистрации");
        }
    }
}
