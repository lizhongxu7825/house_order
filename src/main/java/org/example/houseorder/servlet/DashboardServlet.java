package org.example.houseorder.servlet;

import org.example.houseorder.model.HouseOrder;
import org.example.houseorder.service.DashboardService;
import org.example.houseorder.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/dashboard", "/api/orders"}, loadOnStartup = 1)
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if ("/api/orders".equals(path)) {
            handleOrders(req, resp);
        } else {
            handleDashboard(req, resp);
        }
    }

    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DashboardService svc = (DashboardService) req.getServletContext().getAttribute("dashboardService");
        if (svc == null) {
            resp.setStatus(503);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\":\"HBase 未连接\"}");
            return;
        }
        Map<String, Object> data = svc.getDashboard();
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(JsonUtil.toJson(data));
        out.flush();
    }

    @SuppressWarnings("unchecked")
    private void handleOrders(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<HouseOrder> orders = (List<HouseOrder>) req.getServletContext().getAttribute("orders");
        if (orders == null) orders = new java.util.ArrayList<HouseOrder>();

        int page = parseIntParam(req, "page", 1);
        int size = parseIntParam(req, "pageSize", parseIntParam(req, "size", 10));
        String status = req.getParameter("status");
        String roomType = req.getParameter("roomType");
        String keyword = req.getParameter("keyword");

        DashboardService svc = new DashboardService(orders);
        Map<String, Object> result = svc.getOrdersPage(page, size, status, roomType, keyword);

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(JsonUtil.toJson(result));
        out.flush();
    }

    private int parseIntParam(HttpServletRequest req, String name, int def) {
        try {
            return Integer.parseInt(req.getParameter(name));
        } catch (Exception e) {
            return def;
        }
    }
}
