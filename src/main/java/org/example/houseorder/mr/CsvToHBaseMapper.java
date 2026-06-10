package org.example.houseorder.mr;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvToHBaseMapper extends Mapper<LongWritable, Put, ImmutableBytesWritable, Put> {

    private static final byte[] CF_INFO = Bytes.toBytes("info");
    private static final byte[] CF_DETAIL = Bytes.toBytes("detail");

    private static final byte[] Q_HOUSE_ID = Bytes.toBytes("houseId");
    private static final byte[] Q_ROOM_TYPE = Bytes.toBytes("roomType");
    private static final byte[] Q_AREA = Bytes.toBytes("area");
    private static final byte[] Q_TENANT_NAME = Bytes.toBytes("tenantName");
    private static final byte[] Q_CONTACT = Bytes.toBytes("contact");
    private static final byte[] Q_CHECK_IN = Bytes.toBytes("checkInDate");
    private static final byte[] Q_CHECK_OUT = Bytes.toBytes("checkOutDate");
    private static final byte[] Q_LEASE_DAYS = Bytes.toBytes("leaseDays");
    private static final byte[] Q_RENT = Bytes.toBytes("rent");
    private static final byte[] Q_DEPOSIT = Bytes.toBytes("deposit");
    private static final byte[] Q_PAYMENT = Bytes.toBytes("paymentMethod");
    private static final byte[] Q_STATUS = Bytes.toBytes("status");

    @Override
    protected void map(LongWritable key, Put value, Context context) throws IOException, InterruptedException {
        context.write(new ImmutableBytesWritable(value.getRow()), value);
    }

    public static Put parseLine(String line) {
        String[] f = line.split(",", -1);
        if (f.length < 13) return null;

        String orderId = f[0].trim();
        if (orderId.isEmpty() || orderId.equals("订单编号")) return null;

        Put put = new Put(Bytes.toBytes(orderId));
        put.addColumn(CF_INFO, Q_HOUSE_ID, Bytes.toBytes(f[1].trim()));
        put.addColumn(CF_INFO, Q_ROOM_TYPE, Bytes.toBytes(f[2].trim()));
        put.addColumn(CF_INFO, Q_AREA, Bytes.toBytes(f[3].trim()));
        put.addColumn(CF_INFO, Q_TENANT_NAME, Bytes.toBytes(f[4].trim()));
        put.addColumn(CF_INFO, Q_CONTACT, Bytes.toBytes(f[5].trim()));
        put.addColumn(CF_DETAIL, Q_CHECK_IN, Bytes.toBytes(f[6].trim()));
        put.addColumn(CF_DETAIL, Q_CHECK_OUT, Bytes.toBytes(f[7].trim()));
        put.addColumn(CF_DETAIL, Q_LEASE_DAYS, Bytes.toBytes(f[8].trim()));
        put.addColumn(CF_DETAIL, Q_RENT, Bytes.toBytes(f[9].trim()));
        put.addColumn(CF_DETAIL, Q_DEPOSIT, Bytes.toBytes(f[10].trim()));
        put.addColumn(CF_DETAIL, Q_PAYMENT, Bytes.toBytes(f[11].trim()));
        put.addColumn(CF_DETAIL, Q_STATUS, Bytes.toBytes(f[12].trim()));
        return put;
    }
}
