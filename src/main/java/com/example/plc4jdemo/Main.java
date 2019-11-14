package com.example.plc4jdemo;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;

public class Main {

    public static void main(String[] args) {
        LocalDateTime startTotal = now();
        try (PlcConnection plcConnection = new PlcDriverManager().getConnection("s7://172.30.74.65/0/0")) {
            System.out.println("connected");
            System.out.println(plcConnection);
            CompletableFuture<Void> ping = plcConnection.ping();
            ping.get(500, TimeUnit.MILLISECONDS);
            System.out.println("ping exception = " + ping.isCompletedExceptionally());
            System.out.println();

            // non optimized DB
            for (int i = 0; i < 10000; i++) {
                PlcReadRequest.Builder builder = plcConnection.readRequestBuilder();
                builder.addItem("Int", "%DB101.DBW78:INT");
                builder.addItem("DInt", "%DB101.DBD80:DINT");
                builder.addItem("SinusReal", "%DB101.DBD118:REAL");
                PlcReadRequest readRequest = builder.build();
                LocalDateTime start = now();
                PlcReadResponse plcReadResponse = readRequest.execute().get();
                if (i % 1000 == 0) {
                    System.out.println("request i=" + i + " took " + Duration.between(start, now()));
                    for (String rfn : plcReadResponse.getFieldNames()) {
                        Object o = plcReadResponse.getObject(rfn);
                        System.out.println(rfn + " response: " + plcReadResponse.getResponseCode(rfn) + "; value: " + o + "; data type: " + o.getClass().getCanonicalName());
                    }
                    System.out.println();
                }
                Thread.sleep(10);
            }
            System.out.println("all requests took " + Duration.between(startTotal, now()));

            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("disconnected");
    }

}
