package com.jnetdata.simple.demo1;

import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Spring任务演示1
 */
@Service
public class FirstSpringTask {

    public void job1(){
        System.out.println(Thread.currentThread().getName()+ ">>>FirstSpringTask::job1 任务进行中。。。"+(new Date()));
    }

    public void job2(){
        System.out.println(Thread.currentThread().getName()+ ">>>FirstSpringTask::job2 任务进行中。。。"+(new Date()));
    }

    public void job3(){
        System.out.println(Thread.currentThread().getName()+ ">>>FirstSpringTask::job3 任务进行中。。。"+(new Date()));
    }

}
