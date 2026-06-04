package org.example.houseorder.service;

import org.example.houseorder.model.HouseOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {

    private final List<HouseOrder> orders;

    public DashboardService(List<HouseOrder> orders) {
        this.orders = orders;
    }

    public List<HouseOrder> getOrders() { return orders; }

    public Map<String, Object> getDashboard() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("summary", buildSummary());
        m.put("roomPopularity", roomPopularity());
        m.put("paymentShare", paymentShare());
        m.put("rentStats", rentStats());
        m.put("monthlyTrend", monthlyTrend());
        m.put("leasePreference", leasePreference());
        return m;
    }

    public Map<String, Object> getOrdersPage(int page, int size, String keyword, String status, String roomType) {
        List<HouseOrder> filtered = filter(keyword, status, roomType);
        int total = filtered.size();
        int totalPages = Math.max(1, (total + size - 1) / size);
        page = Math.max(1, Math.min(page, totalPages));
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<HouseOrder> pageData = from < total ? filtered.subList(from, to) : new ArrayList<HouseOrder>();

        List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
        for (HouseOrder o : pageData) {
            Map<String, Object> rec = new LinkedHashMap<String, Object>();
            rec.put("orderId", o.getOrderId());
            rec.put("houseId", o.getHouseId());
            rec.put("roomType", o.getRoomType());
            rec.put("area", o.getArea());
            rec.put("tenantName", o.getTenantName());
            rec.put("contact", o.getContact());
            rec.put("checkInDate", o.getCheckInDate());
            rec.put("checkOutDate", o.getCheckOutDate());
            rec.put("leaseDays", o.getLeaseDays());
            rec.put("rent", o.getRent());
            rec.put("deposit", o.getDeposit());
            rec.put("paymentMethod", o.getPaymentMethod());
            rec.put("status", o.getStatus());
            records.add(rec);
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("page", page);
        result.put("pageSize", size);
        result.put("totalRecords", total);
        result.put("totalPages", totalPages);
        result.put("records", records);
        return result;
    }

    private List<HouseOrder> filter(String keyword, String status, String roomType) {
        List<HouseOrder> result = new ArrayList<HouseOrder>();
        for (HouseOrder o : orders) {
            if (status != null && !status.isEmpty() && !status.equals(o.getStatus())) continue;
            if (roomType != null && !roomType.isEmpty() && !roomType.equals(o.getRoomType())) continue;
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.toLowerCase();
                if (!o.getOrderId().toLowerCase().contains(kw)
                        && !o.getTenantName().toLowerCase().contains(kw)
                        && !o.getHouseId().toLowerCase().contains(kw)
                        && !o.getRoomType().toLowerCase().contains(kw)) continue;
            }
            result.add(o);
        }
        return result;
    }

    private Map<String, Object> buildSummary() {
        long total = orders.size();
        long completed = 0, canceled = 0;
        for (HouseOrder o : orders) {
            if ("完成".equals(o.getStatus())) completed++;
            else if ("取消".equals(o.getStatus())) canceled++;
        }
        double rate = total > 0 ? BigDecimal.valueOf(completed * 100.0 / total).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0;
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("totalOrders", total);
        m.put("completedOrders", completed);
        m.put("canceledOrders", canceled);
        m.put("completionRate", rate);
        return m;
    }

    private Map<String, Long> roomPopularity() {
        Map<String, Long> m = new LinkedHashMap<String, Long>();
        for (HouseOrder o : orders) {
            m.merge(o.getRoomType(), 1L, Long::sum);
        }
        return m;
    }

    private Map<String, Double> paymentShare() {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (HouseOrder o : orders) {
            String pm = o.getPaymentMethod();
            if (pm == null || pm.isEmpty()) pm = "未知";
            counts.merge(pm, 1, Integer::sum);
        }
        Map<String, Double> m = new LinkedHashMap<String, Double>();
        int total = orders.size();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            m.put(e.getKey(), BigDecimal.valueOf(e.getValue() * 100.0 / total).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
        return m;
    }

    private Map<String, Map<String, BigDecimal>> rentStats() {
        Map<String, List<BigDecimal>> groups = new LinkedHashMap<String, List<BigDecimal>>();
        for (HouseOrder o : orders) {
            groups.computeIfAbsent(o.getRoomType(), k -> new ArrayList<BigDecimal>()).add(o.getRent());
        }
        Map<String, Map<String, BigDecimal>> m = new LinkedHashMap<String, Map<String, BigDecimal>>();
        for (Map.Entry<String, List<BigDecimal>> e : groups.entrySet()) {
            BigDecimal min = e.getValue().stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = e.getValue().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal avg = e.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(e.getValue().size()), 2, RoundingMode.HALF_UP);
            Map<String, BigDecimal> stats = new LinkedHashMap<String, BigDecimal>();
            stats.put("min", min);
            stats.put("max", max);
            stats.put("avg", avg);
            m.put(e.getKey(), stats);
        }
        return m;
    }

    private Map<String, Long> monthlyTrend() {
        Map<String, Long> m = new LinkedHashMap<String, Long>();
        for (HouseOrder o : orders) {
            if ("完成".equals(o.getStatus()) && o.getCheckInDate().length() >= 7) {
                String month = o.getCheckInDate().substring(0, 7);
                m.merge(month, 1L, Long::sum);
            }
        }
        return m;
    }

    private Map<String, Object> leasePreference() {
        long shortTerm = 0, longTerm = 0;
        for (HouseOrder o : orders) {
            if (o.getLeaseDays() < 90) shortTerm++;
            else longTerm++;
        }
        long total = shortTerm + longTerm;
        Map<String, Object> counts = new LinkedHashMap<String, Object>();
        counts.put("shortTerm", shortTerm);
        counts.put("longTerm", longTerm);
        counts.put("shortTermRate", total > 0 ? BigDecimal.valueOf(shortTerm * 100.0 / total).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0);
        counts.put("longTermRate", total > 0 ? BigDecimal.valueOf(longTerm * 100.0 / total).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0);
        return counts;
    }
}
