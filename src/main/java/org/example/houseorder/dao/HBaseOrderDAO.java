package org.example.houseorder.dao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.example.houseorder.model.HouseOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HBaseOrderDAO implements AutoCloseable {

    private static final TableName TABLE = TableName.valueOf("house_order", "orders");

    private static final byte[] CF_INFO = Bytes.toBytes("info");
    private static final byte[] CF_DETAIL = Bytes.toBytes("detail");

    private final Connection connection;

    public HBaseOrderDAO() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        conf.setInt("hbase.client.retries.number", 3);
        conf.setInt("hbase.client.connection.timeout", 10000);
        conf.setInt("hbase.rpc.timeout", 10000);
        conf.setInt("hbase.client.operation.timeout", 15000);
        conf.setInt("hbase.client.scanner.timeout.period", 30000);
        conf.setInt("zookeeper.session.timeout", 60000);
        conf.setInt("zookeeper.connection.timeout", 15000);
        conf.setInt("hbase.client.scanner.caching", 100);
        System.out.println("[HBase] ZK地址: " + conf.get("hbase.zookeeper.quorum"));
        System.out.println("[HBase] ZK端口: " + conf.get("hbase.zookeeper.property.clientPort"));
        this.connection = ConnectionFactory.createConnection(conf);
    }

    public List<HouseOrder> findAll() throws Exception {
        List<HouseOrder> list = new ArrayList<HouseOrder>();
        try (Table table = connection.getTable(TABLE);
             ResultScanner scanner = table.getScanner(new Scan())) {
            for (Result r : scanner) {
                HouseOrder order = toOrder(r);
                if (order != null) list.add(order);
            }
        }
        return list;
    }

    private HouseOrder toOrder(Result r) {
        try {
            return new HouseOrder(
                    Bytes.toString(r.getRow()),
                    val(r, CF_INFO, "houseId"),
                    val(r, CF_INFO, "roomType"),
                    Double.parseDouble(val(r, CF_INFO, "area")),
                    val(r, CF_INFO, "tenantName"),
                    val(r, CF_INFO, "contact"),
                    val(r, CF_DETAIL, "checkInDate"),
                    val(r, CF_DETAIL, "checkOutDate"),
                    Integer.parseInt(val(r, CF_DETAIL, "leaseDays")),
                    new BigDecimal(val(r, CF_DETAIL, "rent")),
                    new BigDecimal(val(r, CF_DETAIL, "deposit")),
                    val(r, CF_DETAIL, "paymentMethod"),
                    val(r, CF_DETAIL, "status")
            );
        } catch (Exception e) {
            System.err.println("[HBase] 解析行数据失败 row=" + Bytes.toString(r.getRow()) + ": " + e.getMessage());
            return null;
        }
    }

    public List<HouseOrder> findByStatus(String status) throws Exception {
        List<HouseOrder> list = new ArrayList<HouseOrder>();
        FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        filters.addFilter(new SingleColumnValueFilter(CF_DETAIL, Bytes.toBytes("status"),
                CompareOperator.EQUAL, Bytes.toBytes(status)));
        Scan scan = new Scan();
        scan.setFilter(filters);
        try (Table table = connection.getTable(TABLE);
             ResultScanner scanner = table.getScanner(scan)) {
            for (Result r : scanner) {
                HouseOrder order = toOrder(r);
                if (order != null) list.add(order);
            }
        }
        return list;
    }

    public List<HouseOrder> findByRoomType(String roomType) throws Exception {
        List<HouseOrder> list = new ArrayList<HouseOrder>();
        FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        filters.addFilter(new SingleColumnValueFilter(CF_INFO, Bytes.toBytes("roomType"),
                CompareOperator.EQUAL, Bytes.toBytes(roomType)));
        Scan scan = new Scan();
        scan.setFilter(filters);
        try (Table table = connection.getTable(TABLE);
             ResultScanner scanner = table.getScanner(scan)) {
            for (Result r : scanner) {
                HouseOrder order = toOrder(r);
                if (order != null) list.add(order);
            }
        }
        return list;
    }

    public List<HouseOrder> findByStatusAndRoomType(String status, String roomType) throws Exception {
        List<HouseOrder> list = new ArrayList<HouseOrder>();
        FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        if (status != null && !status.isEmpty()) {
            filters.addFilter(new SingleColumnValueFilter(CF_DETAIL, Bytes.toBytes("status"),
                    CompareOperator.EQUAL, Bytes.toBytes(status)));
        }
        if (roomType != null && !roomType.isEmpty()) {
            filters.addFilter(new SingleColumnValueFilter(CF_INFO, Bytes.toBytes("roomType"),
                    CompareOperator.EQUAL, Bytes.toBytes(roomType)));
        }
        Scan scan = new Scan();
        scan.setFilter(filters);
        try (Table table = connection.getTable(TABLE);
             ResultScanner scanner = table.getScanner(scan)) {
            for (Result r : scanner) {
                HouseOrder order = toOrder(r);
                if (order != null) list.add(order);
            }
        }
        return list;
    }

    public List<HouseOrder> findByKeyword(String keyword) throws Exception {
        List<HouseOrder> all = findAll();
        if (keyword == null || keyword.isEmpty()) return all;
        String kw = keyword.toLowerCase();
        List<HouseOrder> list = new ArrayList<HouseOrder>();
        for (HouseOrder o : all) {
            if (o.getOrderId().toLowerCase().contains(kw)
                    || o.getTenantName().toLowerCase().contains(kw)
                    || o.getHouseId().toLowerCase().contains(kw)
                    || o.getRoomType().toLowerCase().contains(kw)) {
                list.add(o);
            }
        }
        return list;
    }

    private String val(Result r, byte[] family, String col) {
        byte[] v = r.getValue(family, Bytes.toBytes(col));
        return v == null ? "" : Bytes.toString(v);
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
