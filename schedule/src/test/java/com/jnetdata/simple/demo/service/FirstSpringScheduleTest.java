package com.jnetdata.simple.demo.service;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * xml配置方式
 */
@ContextConfiguration({
        "classpath:spring/springcontext-task1.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class FirstSpringScheduleTest {

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
