package com.jnetdata.simple.demo.service;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 手工编程调用
 */
@ContextConfiguration({
        "classpath:spring/springcontext-task5.xml",
})
@RunWith(SpringJUnit4ClassRunner.class)
public class FiveSpringTaskTest {

    @Autowired
    TaskScheduler taskScheduler;

    @Test
    @SneakyThrows
    public void task_2() {
        /**
         * Cron Example patterns:
         * <li>"0 0 * * * *" = the top of every hour of every day.</li>
         * <li>"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.</li>
         * <li>"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.</li>
         * <li>"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays</li>
         * <li>"0 0 0 25 12 ?" = every Christmas Day at midnight</li>
         */
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger count = new AtomicInteger();
        ScheduledFuture sf = taskScheduler.schedule(()->{
            System.out.println("task..."+(new Date()));
            latch.countDown();
        }, new org.springframework.scheduling.support.CronTrigger("0/1 * * * * *"));

        latch.await();
        sf.cancel(false);

        Thread.sleep(5000);
        System.out.println("OK");
    }


}
