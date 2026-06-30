package com.campuslf.service;

public class ActivityLogServiceTest {
    public static void main(String[] args) {

        ActivityLogService service =
                new ActivityLogService();

        boolean result =
                service.logAction(
                        1,
                        "Backend Service Test"
                );

        System.out.println(result);
    }
}
