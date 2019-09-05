package com.jnetdata.simple.demo3;

import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Spring任务演示1
 */
@Service
public class ThirdSpringTask {

    @Scheduled(cron = "0/10 * * * * ?")
    @SneakyThrows
    public void job1(){
        System.out.println(Thread.currentThread().getName()+ ">>>ThirdSpringTask::job1 任务进行中。。。"+(new Date()));
        Thread.sleep(30000);
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 2000)
    public void job2(){
        System.out.println(Thread.currentThread().getName()+ ">>>ThirdSpringTask::job2 任务进行中。。。"+(new Date()));
    }


}
