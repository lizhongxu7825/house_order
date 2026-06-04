package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.houseorder.dao.HBaseOrderDAO;
import org.example.houseorder.model.HouseOrder;
import org.example.houseorder.service.DashboardService;
import org.example.houseorder.util.JsonUtil;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Launcher {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        List<HouseOrder> orders = new ArrayList<HouseOrder>();
        try {
            System.out.println("[HBase] 正在连接 192.168.227.129:2181（请耐心等待）...");
            ExecutorService exec = Executors.newSingleThreadExecutor();
            Future<List<HouseOrder>> future = exec.submit(new Callable<List<HouseOrder>>() {
                public List<HouseOrder> call() throws Exception {
                    HBaseOrderDAO dao = new HBaseOrderDAO();
                    try {
                        return dao.findAll();
                    } finally {
                        dao.close();
                    }
                }
            });
            try {
                orders = future.get(60, TimeUnit.SECONDS);
                System.out.println("[HBase] 加载成功: " + orders.size() + " 条");
            } catch (Exception e) {
                System.err.println("[HBase] 连接超时或失败: " + e.getMessage());
                e.printStackTrace();
                System.out.println("[HBase] 将以空数据模式启动，页面图表将为空");
                future.cancel(true);
            } finally {
                exec.shutdownNow();
            }
        } catch (Exception e) {
            System.err.println("[HBase] 异常: " + e.getMessage());
            System.out.println("[HBase] 请确保虚拟机已启动 HBase，然后重启本程序");
        }

        DashboardService svc = new DashboardService(orders);

        String html = new String(Files.readAllBytes(Paths.get("src/main/webapp/index.html")), StandardCharsets.UTF_8);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            try {
                if ("/api/dashboard".equals(path)) {
                    json(exchange, JsonUtil.toJson(svc.getDashboard()));
                } else if ("/api/orders".equals(path)) {
                    Map<String, String> p = parseQuery(exchange.getRequestURI().getQuery());
                    int page = Integer.parseInt(p.getOrDefault("page", "1"));
                    int size = Integer.parseInt(p.getOrDefault("pageSize", "20"));
                    json(exchange, JsonUtil.toJson(svc.getOrdersPage(page, size,
                            p.getOrDefault("keyword", ""), p.getOrDefault("status", ""), p.getOrDefault("roomType", ""))));
                } else {
                    send(exchange, 200, "text/html;charset=UTF-8", html);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    send(exchange, 500, "text/plain", e.getMessage());
                } catch (Exception ignored) {}
            }
        });
        server.start();
        System.out.println("=== 房屋租赁管理系统 ===");
        System.out.println("订单数: " + orders.size());
        System.out.println("http://localhost:8080/");
    }

    private static void json(HttpExchange ex, String body) throws Exception {
        send(ex, 200, "application/json;charset=UTF-8", body);
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws Exception {
        ex.getResponseHeaders().set("Content-Type", type);
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new java.util.HashMap<String, String>();
        if (q == null || q.isEmpty()) return m;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try { m.put(java.net.URLDecoder.decode(kv[0], "UTF-8"), java.net.URLDecoder.decode(kv[1], "UTF-8")); }
                catch (Exception ignored) {}
            }
        }
        return m;
    }
}
/*1*/