package com.jnetdata.simple.demo.service;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 注解方式
 * Spring scheduled-tasks默认是串行执行
 */
@ContextConfiguration({
        "classpath:spring/springcontext-task3.xml",
})
@RunWith(SpringJUnit4ClassRunner.class)
public class ThirdSpringScheduleTest {

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
