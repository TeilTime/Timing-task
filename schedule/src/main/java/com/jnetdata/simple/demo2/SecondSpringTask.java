package com.jnetdata.simple.demo2;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Spring任务演示1
 */
@Service
public class SecondSpringTask {

    @Scheduled(cron = "0/10 * * * * ?")
    public void job1(){
        System.out.println(Thread.currentThread().getName()+ ">>>SecondSpringTask::job1 任务进行中。。。"+(new Date()));
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 2000)
    public void job2(){
        System.out.println(Thread.currentThread().getName()+ ">>>SecondSpringTask::job2 任务进行中。。。"+(new Date()));
    }

    @Schedules({
            @Scheduled(cron = "0/10 * * * * ?"),
            @Scheduled(initialDelay = 2000, fixedDelay = 2000)
    })
    public void job3(){
        System.out.println(Thread.currentThread().getName()+ ">>>SecondSpringTask::job3 任务进行中。。。"+(new Date()));
    }

}
