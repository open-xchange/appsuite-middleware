/* 
 * Copyright 2005 - 2009 Terracotta, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package org.quartz.examples.example9;


import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * @author wkratzer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Job1Listener implements JobListener {

    @Override
    public String getName() {
        return "job1_to_job2";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext inContext) {
        System.out.println("Job1Listener says: Job Is about to be executed.");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext inContext) {
        System.out.println("Job1Listener says: Job Execution was vetoed.");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext inContext,
            JobExecutionException inException) {
        System.out.println("Job1Listener says: Job was executed.");
        
        // Simple job #2
        JobDetail job2 = newJob(SimpleJob2.class)
            .withIdentity("job2")
            .build();
        
        Trigger trigger = newTrigger() 
            .withIdentity("job2Trigger")
            .startNow()
            .build();
        
        try {
            // schedule the job to run!
            inContext.getScheduler().scheduleJob(job2, trigger);
        } catch (SchedulerException e) {
            System.err.println("Unable to schedule job2!");
            e.printStackTrace();
        }
        
    }

}
