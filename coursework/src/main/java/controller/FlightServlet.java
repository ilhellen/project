package controller;

import dao.FlightDAOImpl;
import model.Flight;
import model.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FlightServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");

        String bookingMessage = (String) req.getSession().getAttribute("bookingMessage");
        if (bookingMessage != null) {
            req.getSession().removeAttribute("bookingMessage");
        }

        List<Flight> flights = FlightDAOImpl.getInstance().getAllFlights();
        String query = req.getParameter("search");

        if (query != null && !query.trim().isEmpty()) {
            String normalizedQuery = query.trim().toLowerCase();
            flights = flights.stream()
                    .filter(f -> f.getFlightNumber() != null && f.getFlightNumber().toLowerCase().contains(normalizedQuery))
                    .collect(Collectors.toList());
        }

        LocalDateTime now = LocalDateTime.now();

        List<Flight> activeFlights = flights.stream()
                .filter(flight -> flight.getArrivalTime().isAfter(now))
                .collect(Collectors.toList());

        List<Flight> archivedFlights = flights.stream()
                .filter(flight -> flight.getArrivalTime().isBefore(now))
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Список рейсов</title>");

        html.append("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
        html.append("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>");

        html.append("<style>")
                .append(":root { --bg-color: #f8f9fa; --text-color: #000; --header-bg: #e9ecef; }")
                .append("body.dark-theme { --bg-color: #121212; --text-color: #f1f1f1; --header-bg: #1f1f1f; }")
                .append("body { font-family: sans-serif; background: var(--bg-color); color: var(--text-color); padding: 20px; transition: 0.3s; }")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                .append("th, td { border: 1px solid #ccc; padding: 10px; text-align: center; }")
                .append("th { background-color: var(--header-bg); }")
                .append("input[type='text'] { padding: 6px; width: 200px; }")
                .append("button { padding: 6px 12px; margin: 5px; cursor: pointer; }")
                .append("a.logout { float: right; text-decoration: none; background: #dc3545; color: white; padding: 6px 12px; border-radius: 5px; }")
                .append("</style>");

        html.append("<script>")
                .append("let mapModal = null;")
                .append("function toggleTheme() { document.body.classList.toggle('dark-theme'); }")
                .append("function showCrew(crewInfo) { alert('Экипаж: ' + crewInfo); }")
                .append("function closeMapModal() {")
                .append("document.getElementById('mapModal').style.display = 'none';")
                .append("if (window.mapInstance) { window.mapInstance.remove(); window.mapInstance = null; }")
                .append("}")
                .append("function showMapModal(departure, destination) {")
                .append("document.getElementById('mapModal').style.display = 'flex';")
                .append("if (window.mapInstance) { window.mapInstance.remove(); }")
                .append("window.mapInstance = L.map('map').setView([55, 37], 5);")
                .append("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap contributors' }).addTo(window.mapInstance);")
                .append("Promise.all([fetchCoords(departure), fetchCoords(destination)]).then(([depCoords, destCoords]) => {")
                .append("if (depCoords && destCoords) {")
                .append("L.marker(depCoords).addTo(window.mapInstance).bindPopup('Откуда: ' + departure).openPopup();")
                .append("L.marker(destCoords).addTo(window.mapInstance).bindPopup('Куда: ' + destination);")
                .append("L.polyline([depCoords, destCoords], { color: 'blue' }).addTo(window.mapInstance);")
                .append("window.mapInstance.fitBounds([depCoords, destCoords]);")
                .append("} else { alert('Не удалось определить координаты'); }")
                .append("});")
                .append("}")
                .append("function fetchCoords(city) {")
                .append("return fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(city)}`)")
                .append(".then(res => res.json()).then(data => data.length ? [parseFloat(data[0].lat), parseFloat(data[0].lon)] : null).catch(() => null);")
                .append("}")
                .append("</script>");

        html.append("</head><body>");

        html.append("""
            <div id='mapModal' style='
                display:none; position:fixed; top:0; left:0; width:100%; height:100%;
                background: rgba(0,0,0,0.8); z-index:1000; justify-content:center; align-items:center;'>
                <div style='width:80%; height:80%; position:relative;'>
                    <button onclick='closeMapModal()' style='position:absolute; top:10px; right:10px; z-index:1001;'>✖</button>
                    <div id='map' style='width:100%; height:100%; z-index:1000;'></div>
                </div>
            </div>
        """);

        html.append("<header style='background: #fff; padding: 10px 20px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px;'>")
                .append("<h2 style='margin: 0;'>Список рейсов</h2>")
                .append("<a href='/logout' style='background: #dc3545; color: white; padding: 8px 16px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Выйти</a>")
                .append("</header>");

        html.append("<button onclick='toggleTheme()'>🌓 Сменить тему</button>");

        if (bookingMessage != null) {
            html.append("<div style='background: #d4edda; color: #155724; border: 1px solid #c3e6cb; padding: 10px; margin-bottom: 10px;'>")
                    .append(bookingMessage)
                    .append("</div>");
        }

        html.append("<form method='get'>")
                .append("<input type='text' name='search' placeholder='Поиск по номеру рейса' value='")
                .append(query != null ? query : "")
                .append("'>")
                .append("<button type='submit'>Найти</button>")
                .append("</form>");

        if (!activeFlights.isEmpty()) {
            html.append("<h3>Доступные рейсы для бронирования</h3>");
            html.append("<table><tr>")
                    .append("<th>№ Рейса</th><th>Откуда</th><th>Куда</th>")
                    .append("<th>Вылет</th><th>Прибытие</th><th>Статус</th><th>Действия</th>")
                    .append("</tr>");

            for (Flight flight : activeFlights) {
                Duration duration = Duration.between(flight.getDepartureTime(), flight.getArrivalTime());
                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                String crewInfo = flight.getCrew().stream()
                        .map(c -> c.getName() + " (" + c.getRole() + ")")
                        .collect(Collectors.joining(", "));

                html.append("<tr>")
                        .append("<td>").append(flight.getFlightNumber()).append("</td>")
                        .append("<td>").append(flight.getDeparture()).append("</td>")
                        .append("<td>").append(flight.getDestination()).append("</td>")
                        .append("<td>").append(flight.getDepartureTime().format(formatter)).append("</td>")
                        .append("<td>").append(flight.getArrivalTime().format(formatter)).append("</td>")
                        .append("<td>").append(flight.getStatus() != null ? flight.getStatus() : "-").append("</td>")
                        .append("<td>")
                        .append("<button onclick=\"showCrew('").append(crewInfo).append("')\">Подробнее</button>")
                        .append("<button onclick=\"showMapModal('").append(flight.getDeparture()).append("','").append(flight.getDestination()).append("')\">🗺 Маршрут</button>")
                        .append("<form method='post' action='/book' style='display:inline;'>")
                        .append("<input type='hidden' name='flightId' value='").append(flight.getId()).append("'>")
                        .append("<button type='submit'>✈ Забронировать</button>")
                        .append("</form>")
                        .append("</td>")
                        .append("</tr>");
            }

            html.append("</table>");
        } else {
            html.append("<div style='background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 10px; margin-top: 20px;'>Нет активных рейсов для бронирования.</div>");
        }

        if (!archivedFlights.isEmpty()) {
            html.append("<h3>Архив рейсов</h3>");
            html.append("<table><tr>")
                    .append("<th>№ Рейса</th><th>Откуда</th><th>Куда</th>")
                    .append("<th>Вылет</th><th>Прибытие</th><th>Статус</th>")
                    .append("</tr>");

            for (Flight flight : archivedFlights) {
                html.append("<tr>")
                        .append("<td>").append(flight.getFlightNumber()).append("</td>")
                        .append("<td>").append(flight.getDeparture()).append("</td>")
                        .append("<td>").append(flight.getDestination()).append("</td>")
                        .append("<td>").append(flight.getDepartureTime().format(formatter)).append("</td>")
                        .append("<td>").append(flight.getArrivalTime().format(formatter)).append("</td>")
                        .append("<td>").append(flight.getStatus() != null ? flight.getStatus() : "-").append("</td>")
                        .append("</tr>");
            }

            html.append("</table>");
        }

        html.append("</body></html>");
        resp.getWriter().write(html.toString());
    }
}
