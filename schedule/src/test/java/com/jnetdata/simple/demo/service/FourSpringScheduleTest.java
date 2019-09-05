package com.jnetdata.simple.demo.service;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 注解方式(并行)
 * Spring scheduled-tasks默认是串行执行，时常发生task任务太多，
 * 而导致执行任务排队等待，此时就需要配置并行执行
 */
@ContextConfiguration({
        "classpath:spring/springcontext-task4.xml",
})
@RunWith(SpringJUnit4ClassRunner.class)
public class FourSpringScheduleTest {

    /**
     * 配置文件
     */
    @Test
    @SneakyThrows
    public void test1(){
        System.out.println(Thread.currentThread().getName()+">>>>>>");
        while(true) {
            Thread.sleep(50000L);
        }
    }


}
