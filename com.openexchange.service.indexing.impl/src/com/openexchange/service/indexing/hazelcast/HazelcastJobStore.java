/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.service.indexing.hazelcast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.PredicateBuilder;
import com.openexchange.service.indexing.impl.Services;


/**
 * {@link HazelcastJobStore}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastJobStore implements JobStore {
    
    private SchedulerSignaler signaler;
    
    private long misfireThreshold = 60000l;
    
    private ISet<JobKey> jobKeys;
    
    private IMap<TriggerKey, TriggerStateWrapper> triggersByKey;
    
    private IMap<String, ISet<TriggerKey>> triggersByGroup;
    
    private IMap<JobKey, ISet<TriggerKey>> triggersByJobKey;
    
    private IMap<String, IMap<JobKey, JobDetail>> jobsByGroup;
    
    private IMap<String, Calendar> calendarsByName;
    
    private ISet<String> pausedTriggerGroups;
    
    private ISet<String> pausedJobGroups;
    
    private ILock lock;
    
    
    public HazelcastJobStore() {
        super();        
    }
    
    public long getMisfireThreshold() {
        return misfireThreshold;
    }

    public void setMisfireThreshold(long misfireThreshold) {
        if (misfireThreshold < 1) {
            throw new IllegalArgumentException("Misfirethreashold must be larger than 0");
        }
        this.misfireThreshold = misfireThreshold;
    }

    @Override
    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
        HazelcastInstance hazelcast;
        try {
            hazelcast = getHazelcast();
        } catch (JobPersistenceException e) {
            throw new SchedulerConfigException(e.getMessage());
        }
        
        this.signaler = signaler;
        jobsByGroup = hazelcast.getMap("quartzJobsByGroup");
        triggersByGroup = hazelcast.getMap("quartzTriggersByGroup");
        jobKeys = hazelcast.getSet("quartzJobKeys");
        triggersByKey = hazelcast.getMap("quartzTriggersByKey");
        triggersByJobKey = hazelcast.getMap("quartzTriggersByJobKey");
        calendarsByName = hazelcast.getMap("quartzCalendarsByName");
        pausedTriggerGroups = hazelcast.getSet("quartzPausedTriggerGroups");
        pausedJobGroups = hazelcast.getSet("quartzPausedJobGroups");
        lock = hazelcast.getLock("quartzJobStoreLock");
        
        triggersByKey.addIndex("trigger.nextFireTime", true);
        triggersByKey.addIndex("trigger.misfireInstruction", false);
    }

    @Override
    public void schedulerStarted() throws SchedulerException {

    }

    @Override
    public void schedulerPaused() {
        
    }

    @Override
    public void schedulerResumed() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean supportsPersistence() {
        return false;
    }

    @Override
    public long getEstimatedTimeToReleaseAndAcquireTrigger() {
        return 20;
    }

    @Override
    public boolean isClustered() {
        return true;
    }
    
    @Override
    public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
        HazelcastInstance hazelcast = getHazelcast();
        lock.lock();
        try {
            JobKey key = newJob.getKey();
            String group = key.getGroup();
            IMap<JobKey, JobDetail> jobsByKey = jobsByGroup.get(group);
            if (jobsByKey == null) {
                jobsByKey = hazelcast.getMap("quartzJobsByKey/" + group);
                jobsByKey.put(key, newJob);
                jobKeys.add(key);
                jobsByGroup.put(group, jobsByKey);
            } else {
                if (jobsByKey.containsKey(key)) {
                    if (replaceExisting) {
                        jobsByKey.put(key, newJob);
                        jobKeys.add(key);
                    } else {
                        throw new ObjectAlreadyExistsException(newJob);
                    }
                } else {
                    jobsByKey.put(key, newJob);
                    jobKeys.add(key);
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
        HazelcastInstance hazelcast = getHazelcast();
        lock.lock();
        try {            
            TriggerKey key = newTrigger.getKey();
            String group = key.getGroup();
            ISet<TriggerKey> triggersByKey = triggersByGroup.get(group);
            if (triggersByKey == null) {
                triggersByKey = hazelcast.getSet("quartzTriggerKeys/" + group);
                triggersByGroup.put(group, triggersByKey);
                storeTrigger(triggersByKey, newTrigger);
            } else {
                if (triggersByKey.contains(key)) {
                    if (replaceExisting) {
                        storeTrigger(triggersByKey, newTrigger);
                    } else {
                        throw new ObjectAlreadyExistsException(newTrigger);
                    }
                } else {
                    storeTrigger(triggersByKey, newTrigger);
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void storeTrigger(ISet<TriggerKey> triggersByKey, OperableTrigger newTrigger) throws JobPersistenceException {
        TriggerKey triggerKey = newTrigger.getKey();
        String group = triggerKey.getGroup();
        JobKey jobKey = newTrigger.getJobKey();
        triggersByKey.add(triggerKey);
        TriggerStateWrapper stateWrapper;
        if (pausedTriggerGroups.contains(group) || pausedJobGroups.contains(jobKey.getGroup())) {
            stateWrapper = new TriggerStateWrapper(newTrigger, TriggerStateWrapper.STATE_PAUSED);
        } else {
            stateWrapper = new TriggerStateWrapper(newTrigger);
        }
        
        this.triggersByKey.put(triggerKey, stateWrapper);
        ISet<TriggerKey> triggers = triggersByJobKey.get(jobKey);
        if (triggers == null) {
            HazelcastInstance hazelcast = getHazelcast();
            triggers = hazelcast.getSet("quartzTriggersByJobKey/" + jobKey.toString());
            triggersByJobKey.put(jobKey, triggers);
        }
        
        triggers.add(triggerKey);
    }
    
    @Override
    public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws ObjectAlreadyExistsException, JobPersistenceException {
        lock.lock();
        try {
            storeJob(newJob, false);
            storeTrigger(newTrigger, false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void storeJobsAndTriggers(Map<JobDetail, List<Trigger>> triggersAndJobs, boolean replace) throws ObjectAlreadyExistsException, JobPersistenceException {
        lock.lock();
        try {
            if (!replace) {
                for (JobDetail jobDetail : triggersAndJobs.keySet()) {
                    if (jobKeys.contains(jobDetail.getKey())) {
                        throw new ObjectAlreadyExistsException(jobDetail);
                    }
                    
                    List<Trigger> triggers = triggersAndJobs.get(jobDetail);
                    for (Trigger trigger : triggers) {
                        if (triggersByKey.keySet().contains(trigger.getKey())) {
                            throw new ObjectAlreadyExistsException(trigger);
                        }
                    }
                }
            }
            
            for (JobDetail jobDetail : triggersAndJobs.keySet()) {
                storeJob(jobDetail, true);
                List<Trigger> triggers = triggersAndJobs.get(jobDetail);
                for (Trigger trigger : triggers) {
                    storeTrigger((OperableTrigger) trigger, true);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
        lock.lock();
        try {
            String group = jobKey.getGroup();
            if (!jobKeys.remove(jobKey)) {
                return false;
            }
            
            IMap<JobKey, JobDetail> jobsByKey = jobsByGroup.get(group);
            if (jobsByKey == null) {
                return false;
            }

            jobsByKey.remove(jobKey);
            if (jobsByKey.isEmpty()) {
                jobsByGroup.remove(group);
            }
            
            ISet<TriggerKey> triggers = triggersByJobKey.get(jobKey);
            removeTriggers(new ArrayList<TriggerKey>(triggers));
        } finally {
            lock.unlock();
        }

        return true;
    }

    @Override
    public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
        lock.lock();
        try {
            boolean removedAll = true;
            for (JobKey jobKey : jobKeys) {
                removedAll = removedAll && removeJob(jobKey);
            }
    
            return removedAll;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
        lock.lock();
        try {
            String group = jobKey.getGroup();
            IMap<JobKey, JobDetail> jobsByKey = jobsByGroup.get(group);
            if (jobsByKey == null) {
                return null;
            }            
            
            return jobsByKey.get(jobKey);
        } finally {
            lock.unlock();
        }
    }    

    @Override
    public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        lock.lock();
        try {
            TriggerStateWrapper removedTrigger = triggersByKey.remove(triggerKey);
            if (removedTrigger == null) {
                return false;
            }
            
            String group = triggerKey.getGroup();
            ISet<TriggerKey> triggers = triggersByGroup.get(group);
            triggers.remove(triggerKey);
            if (triggers.isEmpty()) {
                triggersByGroup.remove(group);
                if (pausedTriggerGroups.contains(group)) {
                    pausedTriggerGroups.remove(group);
                }
            }
            
            JobKey jobKey = removedTrigger.getTrigger().getJobKey();
            ISet<TriggerKey> triggerKeysForJob = triggersByJobKey.get(jobKey);
            triggerKeysForJob.remove(triggerKey);
            boolean removeJob = false;
            if (triggerKeysForJob.isEmpty()) {
                IMap<JobKey, JobDetail> jobsByKey = jobsByGroup.get(jobKey.getGroup());
                if (jobsByKey != null) {
                    JobDetail jobDetail = jobsByKey.get(jobKey);
                    if (jobDetail != null && !jobDetail.isDurable()) {
                        removeJob = true;
                    }
                }
            }
            
            if (removeJob && removeJob(jobKey)) {                
                signaler.notifySchedulerListenersJobDeleted(jobKey);
            }
            
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
        lock.lock();
        boolean removedAll = true;
        try {
            for (TriggerKey triggerKey : triggerKeys) {
                removedAll = removedAll && removeTrigger(triggerKey);
            }            
        } finally {
            lock.unlock();
        }

        return removedAll;
    }

    @Override
    public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
        lock.lock();
        try {
            boolean found = removeTrigger(triggerKey);
            storeTrigger(newTrigger, true);
            
            return found;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        lock.lock();
        try {            
            TriggerStateWrapper stateWrapper = triggersByKey.get(triggerKey);
            if (stateWrapper == null) {
                return null;
            }
            
            return (OperableTrigger) stateWrapper.getTrigger();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean checkExists(JobKey jobKey) throws JobPersistenceException {
        lock.lock();
        try {
            return jobKeys.contains(jobKey);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
        lock.lock();
        try {
            return triggersByKey.keySet().contains(triggerKey);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearAllSchedulingData() throws JobPersistenceException {
        lock.lock();
        try {
            jobKeys.clear();
            triggersByKey.clear();
            triggersByJobKey.clear();
            for (IMap<JobKey, JobDetail> inner : jobsByGroup.values()) {
                inner.clear();
            }
            
            for (ISet<TriggerKey> inner : triggersByGroup.values()) {
                inner.clear();
            }
            jobsByGroup.clear();
            triggersByGroup.clear();
            calendarsByName.clear();
            pausedTriggerGroups.clear();
            pausedJobGroups.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers) throws ObjectAlreadyExistsException, JobPersistenceException {
        lock.lock();
        try {
            boolean exists = calendarsByName.containsKey(name);
            if (exists && !replaceExisting) {
                throw new ObjectAlreadyExistsException("Calendar with name '" + name + "' already exists.");
            }
            
            calendarsByName.put(name, calendar);
            Set<TriggerKey> toUpdate = new HashSet<TriggerKey>();
            if (exists && updateTriggers) {
                for (TriggerStateWrapper stateWrapper : triggersByKey.values()) {
                    if (name.equals(stateWrapper.getTrigger().getCalendarName())) {
                        toUpdate.add(stateWrapper.getTrigger().getKey());
                    }
                }
                
                for (TriggerKey triggerKey : toUpdate) {
                    TriggerStateWrapper stateWrapper = triggersByKey.remove(triggerKey);
                    if (stateWrapper != null) {
                        OperableTrigger trigger = (OperableTrigger) stateWrapper.getTrigger();
                        trigger.updateWithNewCalendar(calendar, getMisfireThreshold());
                        triggersByKey.put(triggerKey, stateWrapper);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeCalendar(String calName) throws JobPersistenceException {
        lock.lock();
        try {
            for (String group : triggersByGroup.keySet()) {
                ISet<TriggerKey> triggerKeys = triggersByGroup.get(group);
                for (TriggerKey key : triggerKeys) {
                    Trigger trigger = triggersByKey.get(key).getTrigger();
                    if (trigger.getCalendarName().equals(calName)) {
                        throw new JobPersistenceException("Calender cannot be removed if it referenced by a Trigger!");
                    }
                }
            }         
            
            return calendarsByName.remove(calName) != null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
        lock.lock();
        try {
            return calendarsByName.get(calName);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getNumberOfJobs() throws JobPersistenceException {
        lock.lock();
        try {
            return jobKeys.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getNumberOfTriggers() throws JobPersistenceException {
        lock.lock();
        try {
            return triggersByKey.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getNumberOfCalendars() throws JobPersistenceException {
        lock.lock();
        try {
            return calendarsByName.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        Set<JobKey> resultKeys = new HashSet<JobKey>();
        lock.lock();
        try {
            for (JobKey jobKey : jobKeys) {
                if (matcher.isMatch(jobKey)) {
                    resultKeys.add(jobKey);
                }
            }
            
            return resultKeys;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        Set<TriggerKey> resultKeys = new HashSet<TriggerKey>();
        lock.lock();
        try {
            for (TriggerKey triggerKey : triggersByKey.keySet()) {
                if (matcher.isMatch(triggerKey)) {
                    resultKeys.add(triggerKey);
                }
            }
            
            return resultKeys;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> getJobGroupNames() throws JobPersistenceException {
        List<String> groupNames = new ArrayList<String>();
        lock.lock();
        try {
            groupNames.addAll(jobsByGroup.keySet());            
            return groupNames;
        } finally {
            lock.unlock();
        } 
    }

    @Override
    public List<String> getTriggerGroupNames() throws JobPersistenceException {
        List<String> groupNames = new ArrayList<String>();
        lock.lock();
        try {
            groupNames.addAll(triggersByGroup.keySet());            
            return groupNames;
        } finally {
            lock.unlock();
        } 
    }

    @Override
    public List<String> getCalendarNames() throws JobPersistenceException {
        List<String> calendarNames = new ArrayList<String>();
        lock.lock();
        try {
            calendarNames.addAll(calendarsByName.keySet());            
            return calendarNames;
        } finally {
            lock.unlock();
        } 
    }

    @Override
    public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
        List<OperableTrigger> resultTriggers = new ArrayList<OperableTrigger>();
        lock.lock();
        try {
            ISet<TriggerKey> triggerKeys = triggersByJobKey.get(jobKey);
            for (TriggerKey triggerKey : triggerKeys) {
                OperableTrigger trigger = (OperableTrigger) triggersByKey.get(triggerKey).getTrigger();
                resultTriggers.add(trigger);
            }
            
            return resultTriggers;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
        lock.lock();
        TriggerStateWrapper trigger;
        try {
            trigger = triggersByKey.get(triggerKey);
        } finally {
            lock.unlock();
        }
        
        if (trigger == null) {
            return TriggerState.NONE;
        }
        
        if (trigger.getState() == TriggerStateWrapper.STATE_NORMAL) {
            return TriggerState.NORMAL;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_PAUSED) {
            return TriggerState.PAUSED;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_COMPLETE) {
            return TriggerState.COMPLETE;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_ERROR) {
            return TriggerState.ERROR;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_BLOCKED) {
            return TriggerState.BLOCKED;
        }
        
        return TriggerState.NORMAL;
    }

    @Override
    public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        lock.lock();
        try {
            TriggerStateWrapper trigger = triggersByKey.get(triggerKey);
            if (trigger == null || trigger.getState() == TriggerStateWrapper.STATE_COMPLETE) {
                return;
            }
                        
            trigger = triggersByKey.remove(triggerKey);
            trigger.setState(TriggerStateWrapper.STATE_PAUSED);
            triggersByKey.put(triggerKey, trigger);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        List<String> groupsToPause = new ArrayList<String>();
        lock.lock();
        try {
            for (TriggerKey triggerKey : triggersByKey.keySet()) {
                if (matcher.isMatch(triggerKey)) {
                    groupsToPause.add(triggerKey.getGroup());
                    pausedTriggerGroups.add(triggerKey.getGroup());
                }
            }
            
            Set<TriggerKey> triggerKeys = getTriggerKeys(matcher);
            for (TriggerKey triggerKey : triggerKeys) {
                pauseTrigger(triggerKey);
            }
            
            return groupsToPause;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void pauseJob(JobKey jobKey) throws JobPersistenceException {
        lock.lock();
        try {
            ISet<TriggerKey> triggerKeys = triggersByJobKey.get(jobKey);
            if (triggerKeys == null) {
                return;
            }
            
            for (TriggerKey triggerKey : triggerKeys) {
                pauseTrigger(triggerKey);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<String> pauseJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        List<String> groupsToPause = new ArrayList<String>();
        lock.lock();
        try {
            for (JobKey jobKey : jobKeys) {
                if (matcher.isMatch(jobKey)) {
                    groupsToPause.add(jobKey.getGroup());
                    pausedJobGroups.add(jobKey.getGroup());
                }
            }
            
            Set<JobKey> jobKeys = getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                pauseJob(jobKey);
            }
            
            return groupsToPause;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        lock.lock();
        try {
            TriggerStateWrapper stateWrapper = triggersByKey.get(triggerKey);
            if (stateWrapper == null || stateWrapper.getState() != TriggerStateWrapper.STATE_PAUSED) {
                return;
            }
            
            applyMisfire(stateWrapper);
            stateWrapper = triggersByKey.remove(triggerKey);
            stateWrapper.setState(TriggerStateWrapper.STATE_NORMAL);
            triggersByKey.put(triggerKey, stateWrapper);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        List<String> groupsToResume = new ArrayList<String>();
        lock.lock();
        try {
            for (TriggerKey triggerKey : triggersByKey.keySet()) {
                if (matcher.isMatch(triggerKey)) {
                    groupsToResume.add(triggerKey.getGroup());
                    pausedTriggerGroups.remove(triggerKey.getGroup());
                }
            }
            
            Set<TriggerKey> triggerKeys = getTriggerKeys(matcher);
            for (TriggerKey triggerKey : triggerKeys) {
                resumeTrigger(triggerKey);
            }
            
            return groupsToResume;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
        Set<String> toReturn = new HashSet<String>();
        lock.lock();
        try {
            toReturn.addAll(pausedTriggerGroups);
            return toReturn;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resumeJob(JobKey jobKey) throws JobPersistenceException {
        lock.lock();
        try {
            ISet<TriggerKey> triggerKeys = triggersByJobKey.get(jobKey);
            if (triggerKeys == null) {
                return;
            }
            
            for (TriggerKey triggerKey : triggerKeys) {
                resumeTrigger(triggerKey);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        List<String> groupsToResume = new ArrayList<String>();
        lock.lock();
        try {
            for (JobKey jobKey : jobKeys) {
                if (matcher.isMatch(jobKey)) {
                    groupsToResume.add(jobKey.getGroup());
                    pausedJobGroups.remove(jobKey.getGroup());
                }
            }
            
            Set<JobKey> jobKeys = getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                resumeJob(jobKey);
            }
            
            return groupsToResume;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void pauseAll() throws JobPersistenceException {
        lock.lock();
        try {
            Set<String> groups = triggersByGroup.keySet();
            for (String group : groups) {
                pauseTriggers(GroupMatcher.triggerGroupEquals(group));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resumeAll() throws JobPersistenceException {
        lock.lock();
        try {
            Set<String> groups = triggersByGroup.keySet();
            for (String group : groups) {
                resumeTriggers(GroupMatcher.triggerGroupEquals(group));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
        List<OperableTrigger> returnList = new ArrayList<OperableTrigger>();
        lock.lock();
        long now = System.currentTimeMillis();
        long firstAcquiredTriggerFireTime = 0L;
        try {
            EntryObject e = new PredicateBuilder().getEntryObject();
            PredicateBuilder query = e.get("trigger.nextFireTime").isNotNull()
                .and(e.get("trigger.nextFireTime").lessEqual(new Date(noLaterThan + timeWindow))
                .and(e.get("trigger.misfireInstruction").equal(Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
                .or(e.get("trigger.misfireInstruction").notEqual(Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
                .and(e.get("trigger.nextFireTime").greaterEqual(new Date(now - getMisfireThreshold()))))));
            
            Collection<TriggerStateWrapper> filteredTriggers = triggersByKey.values(query);
            ArrayList<TriggerStateWrapper> triggers = new ArrayList<TriggerStateWrapper>(filteredTriggers);
            Collections.sort(triggers, new TriggerWrapperTimeComparator());

            Set<JobKey> excluded = new HashSet<JobKey>();
            for (TriggerStateWrapper stateWrapper : triggers) {
                if (cannotAcquire(stateWrapper)) {
                    continue;
                }
                
                if(firstAcquiredTriggerFireTime > 0 && stateWrapper.getTrigger().getNextFireTime().getTime() > (firstAcquiredTriggerFireTime + timeWindow)) {                    
                    break;
                }
                
                if (applyMisfire(stateWrapper)) {
                    continue;
                }
                
                JobKey jobKey = stateWrapper.getTrigger().getJobKey();
                String group = jobKey.getGroup();
                IMap<JobKey, JobDetail> jobDetails = jobsByGroup.get(group);
                if (jobDetails == null) {
                    continue;
                }

                JobDetail jobDetail = jobDetails.get(jobKey);
                if (jobDetail == null) {
                    continue;
                }
                
                if (jobDetail.isConcurrentExectionDisallowed()) {
                    if (excluded.contains(jobKey)) {
                        continue;
                    }
                    
                    excluded.add(jobKey);                    
                }                                
                
                triggersByKey.remove(stateWrapper.getTrigger().getKey());
                stateWrapper.setState(TriggerStateWrapper.STATE_ACQUIRED);
                OperableTrigger trigger = (OperableTrigger) stateWrapper.getTrigger();
                trigger.setFireInstanceId(getFiredTriggerRecordId());
                triggersByKey.put(stateWrapper.getTrigger().getKey(), stateWrapper);
                if (firstAcquiredTriggerFireTime == 0) {
                    firstAcquiredTriggerFireTime = trigger.getNextFireTime().getTime();
                }
                
                returnList.add(trigger);
                if (returnList.size() == maxCount) {
                    break;
                }
            }            
            
            return returnList;
        } finally {
            lock.unlock();
        }
    }
    
    private static final AtomicLong ftrCtr = new AtomicLong(System.currentTimeMillis());

    protected String getFiredTriggerRecordId() {
        return String.valueOf(ftrCtr.incrementAndGet());
    }
    
    private boolean cannotAcquire(TriggerStateWrapper stateWrapper) {
        return stateWrapper.getState() == TriggerStateWrapper.STATE_BLOCKED 
                || stateWrapper.getState() == TriggerStateWrapper.STATE_COMPLETE
                || stateWrapper.getState() == TriggerStateWrapper.STATE_PAUSED
                || stateWrapper.getState() == TriggerStateWrapper.STATE_ACQUIRED;
    }

    @Override
    public void releaseAcquiredTrigger(OperableTrigger trigger) throws JobPersistenceException {
        lock.lock();
        try {
            TriggerStateWrapper stateWrapper = triggersByKey.remove(trigger.getKey());
            if (stateWrapper != null) {
                stateWrapper.setState(stateWrapper.getOldState());
                triggersByKey.put(trigger.getKey(), stateWrapper);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<TriggerFiredResult> triggersFired(List<OperableTrigger> firedTriggers) throws JobPersistenceException {
        List<TriggerFiredResult> results = new ArrayList<TriggerFiredResult>();
        lock.lock();
        try {
            for (OperableTrigger trigger : firedTriggers) {
                if (triggersByKey.containsKey(trigger.getKey())) {
                    Calendar calendar = null;
                    String calendarName = trigger.getCalendarName();
                    if (calendarName != null) {
                        calendar = calendarsByName.get(calendarName);
                        if (calendar == null) {
                            continue;
                        }
                    }
                    
                    Date prevFireTime = trigger.getPreviousFireTime();
                    trigger.triggered(calendar);
                    replaceTrigger(trigger.getKey(), trigger);
                    JobKey jobKey = trigger.getJobKey();
                    JobDetail job = retrieveJob(jobKey);
                    
                    TriggerFiredResult result;
                    if (job == null) {
                        result = new TriggerFiredResult(new JobPersistenceException("Job could not be found."));
                    } else {
                        TriggerFiredBundle firedBundle = new TriggerFiredBundle(
                            job, 
                            trigger, 
                            calendar, 
                            false, new Date(), 
                            trigger.getPreviousFireTime(), 
                            prevFireTime,
                            trigger.getNextFireTime());
                        
                        result = new TriggerFiredResult(firedBundle);
                    }
                    
                    results.add(result);
                    
                    if (job.isConcurrentExectionDisallowed()) {
                        ISet<TriggerKey> otherTriggers = triggersByJobKey.get(jobKey);
                        for (TriggerKey keyToBlock : otherTriggers) {
                            if (keyToBlock.equals(trigger.getKey())) {
                                continue;
                            }
                            
                            TriggerStateWrapper triggerToBlock = triggersByKey.remove(keyToBlock);
                            triggerToBlock.setState(TriggerStateWrapper.STATE_BLOCKED);
                            triggersByKey.put(keyToBlock, triggerToBlock);
                        }
                    }
                }
            }
            
            return results;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail, CompletedExecutionInstruction triggerInstCode) throws JobPersistenceException {
        lock.lock();
        try {
            JobKey jobKey = jobDetail.getKey();
            String group = jobKey.getGroup();
            if (jobKeys.contains(jobKey)) {
                if (jobDetail.isPersistJobDataAfterExecution()) {
                    IMap<JobKey, JobDetail> jobsByKey = jobsByGroup.get(group);
                    if (jobsByKey != null && jobsByKey.remove(jobKey) != null) {
                        JobDataMap newData = jobDetail.getJobDataMap();
                        if (newData != null) {
                            newData.clearDirtyFlag();
                        }
                        
                        ((JobDetailImpl)jobDetail).setJobDataMap(newData);
                        jobsByKey.put(jobKey, jobDetail);
                    }
                }
                
                if (jobDetail.isConcurrentExectionDisallowed()) {
                    ISet<TriggerKey> otherTriggers = triggersByJobKey.get(jobKey);
                    for (TriggerKey keyToUnblock : otherTriggers) {
                        if (keyToUnblock.equals(trigger.getKey())) {
                            continue;
                        }                        
                        
                        TriggerStateWrapper triggerToUnblock = triggersByKey.remove(keyToUnblock);
                        triggerToUnblock.setState(triggerToUnblock.getOldState());
                        triggersByKey.put(keyToUnblock, triggerToUnblock);
                    }
                    
                    signaler.signalSchedulingChange(0L);
                }
            }
            
            TriggerStateWrapper stateWrapper = triggersByKey.get(trigger.getKey());
            if (stateWrapper != null) {
                if (triggerInstCode == CompletedExecutionInstruction.DELETE_TRIGGER) {
                    if (trigger.getNextFireTime() == null) {
                        if (stateWrapper.getTrigger().getNextFireTime() == null) {
                            removeTrigger(trigger.getKey());
                        }
                    } else {
                        removeTrigger(trigger.getKey());
                        signaler.signalSchedulingChange(0L);
                    }
                } else if (triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_COMPLETE) {
                    triggersByKey.remove(trigger.getKey());
                    stateWrapper.setState(TriggerStateWrapper.STATE_COMPLETE);
                    triggersByKey.put(trigger.getKey(), stateWrapper);
                    signaler.signalSchedulingChange(0L);
                } else if(triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_ERROR) {
                    triggersByKey.remove(trigger.getKey());
                    stateWrapper.setState(TriggerStateWrapper.STATE_ERROR);
                    triggersByKey.put(trigger.getKey(), stateWrapper);
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR) {
                    ISet<TriggerKey> otherTriggers = triggersByJobKey.get(jobKey);
                    for (TriggerKey triggerKeyToChange : otherTriggers) {
                        TriggerStateWrapper triggerToChange = triggersByKey.remove(triggerKeyToChange);
                        triggerToChange.setState(TriggerStateWrapper.STATE_ERROR);
                        triggersByKey.put(triggerKeyToChange, triggerToChange);
                    }

                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
                    ISet<TriggerKey> otherTriggers = triggersByJobKey.get(jobKey);
                    for (TriggerKey triggerKeyToChange : otherTriggers) {
                        TriggerStateWrapper triggerToChange = triggersByKey.remove(triggerKeyToChange);
                        triggerToChange.setState(TriggerStateWrapper.STATE_COMPLETE);
                        triggersByKey.put(triggerKeyToChange, triggerToChange);
                    }
                    
                    signaler.signalSchedulingChange(0L);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setInstanceId(String schedInstId) {
        //
    }

    @Override
    public void setInstanceName(String schedName) {
        //
    }

    @Override
    public void setThreadPoolSize(int poolSize) {
        //
    }
    
    private boolean applyMisfire(TriggerStateWrapper stateWrapper) throws JobPersistenceException {
        long misfireTime = System.currentTimeMillis();
        if (getMisfireThreshold() > 0) {
            misfireTime -= getMisfireThreshold();
        }
        
        Date nextFireTime = stateWrapper.getTrigger().getNextFireTime();
        if (nextFireTime == null 
            || nextFireTime.getTime() > misfireTime 
            || stateWrapper.getTrigger().getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) { 
            return false; 
        }

        Calendar calendar = null;
        String calendarName = stateWrapper.getTrigger().getCalendarName();
        if (calendarName != null) {
            calendar = retrieveCalendar(calendarName);
        }

        signaler.notifyTriggerListenersMisfired((OperableTrigger) stateWrapper.getTrigger());
        ((OperableTrigger) stateWrapper.getTrigger()).updateAfterMisfire(calendar);

        if (stateWrapper.getTrigger().getNextFireTime() == null) {
            stateWrapper.setState(TriggerStateWrapper.STATE_COMPLETE);
            triggersByKey.remove(stateWrapper.getTrigger().getKey());
            triggersByKey.put(stateWrapper.getTrigger().getKey(), stateWrapper);
            
            signaler.notifySchedulerListenersFinalized(stateWrapper.getTrigger());
        } else if (nextFireTime.equals(stateWrapper.getTrigger().getNextFireTime())) {
            return false;
        }

        return true;
    }
    
    protected HazelcastInstance getHazelcast() throws JobPersistenceException {
        HazelcastInstance hazelcast = Services.optService(HazelcastInstance.class);
        if (hazelcast == null) {
            throw new JobPersistenceException("Hazelcast Service is not available.");
        }
        
        return hazelcast;            
    }
    
    static final class TriggerStateWrapper implements Serializable {
        
        private static final long serialVersionUID = -7286840785328204285L;
        
        public static final int STATE_NONE = 0;
        
        public static final int STATE_NORMAL = 1;
        
        public static final int STATE_PAUSED = 2;
        
        public static final int STATE_COMPLETE = 3;
        
        public static final int STATE_ERROR = 4;
        
        public static final int STATE_BLOCKED = 5;
        
        public static final int STATE_ACQUIRED = 6;
        
        private final Trigger trigger;
        
        private int state;
        
        private int oldState;
        

        public TriggerStateWrapper(Trigger trigger) {
            super();
            this.trigger = trigger;
            this.state = STATE_NORMAL;
            oldState = STATE_NONE;
        }
        
        public TriggerStateWrapper(Trigger trigger, int state) {
            super();
            this.trigger = trigger;
            this.state = state;
            oldState = STATE_NONE;
        }

        public Trigger getTrigger() {
            return trigger;
        }
        
        public int getState() {
            return state;
        }
        
        public int getOldState() {
            return oldState;
        }
        
        public void setState(int state) {
            oldState = this.state;
            this.state = state;
        }
    }
    
    /**
     * A Comparator that compares trigger's next fire times, or in other words,
     * sorts them according to earliest next fire time.  If the fire times are
     * the same, then the triggers are sorted according to priority (highest
     * value first), if the priorities are the same, then they are sorted
     * by key.
     */
    class TriggerWrapperTimeComparator implements Comparator<TriggerStateWrapper>, Serializable {
      
        private static final long serialVersionUID = -3904243490805975570L;

        public int compare(TriggerStateWrapper trig1, TriggerStateWrapper trig2) {

            Date t1 = trig1.getTrigger().getNextFireTime();
            Date t2 = trig2.getTrigger().getNextFireTime();

            if (t1 != null || t2 != null) {
                if (t1 == null) {
                    return 1;
                }

                if (t2 == null) {
                    return -1;
                }

                if(t1.before(t2)) {
                    return -1;
                }

                if(t1.after(t2)) {
                    return 1;
                }
            }

            int comp = trig2.getTrigger().getPriority() - trig1.getTrigger().getPriority();
            if (comp != 0) {
                return comp;
            }
            
            return trig1.getTrigger().getKey().compareTo(trig2.getTrigger().getKey());
        }
    }

}
