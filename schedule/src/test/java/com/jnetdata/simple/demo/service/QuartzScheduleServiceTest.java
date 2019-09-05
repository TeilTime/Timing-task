package com.jnetdata.simple.demo.service;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Quartz演示
 */
@ContextConfiguration({
        "classpath:spring/springcontext-quartz.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class QuartzScheduleServiceTest {

    @Autowired
    ApplicationContext applicationContext;

    /**
     * 配置文件
     */
    @Test
    @SneakyThrows
    public void taskUsingConfig() {
        System.out.println(Thread.currentThread().getName()+">>>>>>");
        while(true) {
            Thread.sleep(50000L);
        }

    }


    /**
     * quartz, 手工程序产生
     */
    @Test
    @SneakyThrows
    public void quartz_2(){

        // 获取spring定义的调度器
        SchedulerFactoryBean sfb = applicationContext.getBean(SchedulerFactoryBean.class);
        Scheduler scheduler = sfb.getScheduler();
        scheduler.clear();

        // 产生job(用spring bean和方法)
        MethodInvokingJobDetailFactoryBean jf = new MethodInvokingJobDetailFactoryBean();
        jf.setBeanFactory(applicationContext);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        jf.setName(uuid);
        jf.setTargetBeanName("quartzScheduleService");
        jf.setTargetMethod("proxyCall");
        jf.afterPropertiesSet();
        JobDetail jobDetail = jf.getObject();

        // 产生执行规则
        CronTriggerFactoryBean cf = new CronTriggerFactoryBean();
        String uuid1 = UUID.randomUUID().toString().replaceAll("-", "");
        cf.setName(uuid1);
        cf.setCronExpression("0/1 * * * * ? ");
        cf.afterPropertiesSet();
        CronTrigger trigger = cf.getObject();
        System.out.println(trigger);

        // 根据规则调度
        scheduler.scheduleJob(jobDetail, trigger);

        // 测试
        Integer maxRuns = 10;
        int currentIndex = 0;
        while(true) {
            System.out.println("currentIndex="+currentIndex);
            Thread.sleep(2000L);
            if (++currentIndex>maxRuns) {
                scheduler.deleteJob(jobDetail.getKey());
            }
            System.out.println(scheduler.checkExists(jobDetail.getKey()) ? "job existed" : "job has not existed");
        }
    }


}
