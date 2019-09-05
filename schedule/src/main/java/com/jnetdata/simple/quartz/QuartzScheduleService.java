package com.jnetdata.simple.quartz;

import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Quartz任务演示
 */
@Service
public class QuartzScheduleService {

    public void hello() {
        System.out.println(Thread.currentThread().getName()+ ">>>QuartzScheduleService::hello() at " + (new Date()));
    }

    public void proxyCall() {
        System.out.println(Thread.currentThread().getName()+ ">>>QuartzScheduleService()::proxyCall");
    }

}
