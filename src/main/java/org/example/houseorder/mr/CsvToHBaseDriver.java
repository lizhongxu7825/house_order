package org.example.houseorder.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class CsvToHBaseDriver extends Configured implements Tool {

    private static final String TABLE_NAME = "house_order";

    public static class CsvMapper extends Mapper<LongWritable, org.apache.hadoop.io.Text, ImmutableBytesWritable, Put> {

        @Override
        protected void map(LongWritable key, org.apache.hadoop.io.Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            Put put = CsvToHBaseMapper.parseLine(line);
            if (put != null) {
                context.write(new ImmutableBytesWritable(put.getRow()), put);
            }
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: CsvToHBaseDriver <csv-file-path>");
            return 1;
        }

        Configuration conf = getConf();
        conf.set("hbase.zookeeper.quorum", "192.168.227.129");
        conf.set("hbase.zookeeper.property.clientPort", "2181");

        Connection connection = ConnectionFactory.createConnection(conf);
        TableName tableName = TableName.valueOf(TABLE_NAME);

        if (!connection.getAdmin().tableExists(tableName)) {
            org.apache.hadoop.hbase.HTableDescriptor desc = new org.apache.hadoop.hbase.HTableDescriptor(tableName);
            desc.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor("info"));
            desc.addFamily(new org.apache.hadoop.hbase.HColumnDescriptor("detail"));
            connection.getAdmin().createTable(desc);
            System.out.println("[MR] created table " + TABLE_NAME);
        }
        connection.close();

        Job job = Job.getInstance(conf, "CSV to HBase Import");
        job.setJarByClass(CsvToHBaseDriver.class);
        job.setMapperClass(CsvMapper.class);
        job.setNumReduceTasks(0);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, TABLE_NAME);

        FileInputFormat.addInputPath(job, new Path(args[0]));

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(HBaseConfiguration.create(), new CsvToHBaseDriver(), args);
        System.exit(exitCode);
    }
}
