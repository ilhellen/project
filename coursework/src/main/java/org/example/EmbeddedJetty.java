package org.example;

import controller.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedJetty {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp"); //  Путь до HTML-файлов
        context.setWelcomeFiles(new String[]{"index.html"});
        context.setParentLoaderPriority(true);

        context.addServlet(new ServletHolder(new FlightServlet()), "/flights");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new RegisterServlet()), "/register");
        context.addServlet(new ServletHolder(new BookingServlet()), "/book");
        context.addServlet(new ServletHolder(new LogoutServlet()), "/logout");

        server.setHandler(context);
        server.start();

        System.out.println("✅ Сервер запущен: http://localhost:8080");
        server.join();
    }
}
