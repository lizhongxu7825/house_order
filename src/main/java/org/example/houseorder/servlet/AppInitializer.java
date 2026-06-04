package org.example.houseorder.servlet;

import org.example.houseorder.dao.HBaseOrderDAO;
import org.example.houseorder.model.HouseOrder;
import org.example.houseorder.service.DashboardService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.ArrayList;
import java.util.List;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        ServletContext ctx = sce.getServletContext();
        System.out.println("[App] 正在连接 HBase 192.168.227.129:2181 ...");

        List<HouseOrder> orders = new ArrayList<HouseOrder>();
        try {
            HBaseOrderDAO dao = new HBaseOrderDAO();
            try {
                orders = dao.findAll();
            } finally {
                dao.close();
            }
            System.out.println("[App] 加载成功: " + orders.size() + " 条");
        } catch (Exception e) {
            System.err.println("[App] HBase 连接失败: " + e.getMessage());
            System.out.println("[App] 将以空数据模式运行");
        }

        ctx.setAttribute("orders", orders);
        ctx.setAttribute("dashboardService", new DashboardService(orders));
        System.out.println("[App] 仪表盘服务已就绪");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[App] 应用关闭");
    }
}
