package com.jnetdata.simple.demo1;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FirstSpringTrigger implements Trigger {

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date date = triggerContext.lastActualExecutionTime();
        if (null==date) {
            return new Date();
        }
        return new Date(date.getTime()+10000L);
    }

}
