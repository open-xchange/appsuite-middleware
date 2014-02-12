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

package com.openexchange.quartz.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
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
import org.quartz.service.internal.Services;
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
import com.hazelcast.query.SqlPredicate;
import com.openexchange.quartz.hazelcast.predicates.GroupMatcherPredicate;
import com.openexchange.quartz.hazelcast.predicates.OtherTriggersForJobPredicate;
import com.openexchange.quartz.hazelcast.predicates.SelectTriggersPredicate;
import com.openexchange.quartz.hazelcast.predicates.TriggersForCalendarPredicate;

/**
 * {@link ImprovedHazelcastJobStore}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ImprovedHazelcastJobStore implements JobStore {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ImprovedHazelcastJobStore.class);

    private SchedulerSignaler signaler;

    private long misfireThreshold = 60000l;

    private IMap<TriggerKey, TriggerStateWrapper> triggersByKey;

    private IMap<JobKey, JobDetail> jobsByKey;

    private IMap<String, Boolean> triggerGroups;

    private IMap<String, Boolean> jobGroups;

    private ISet<JobKey> blockedJobs;

    private IMap<String, Calendar> calendarsByName;

    private ILock lock;

    private String instanceId;

    private String instanceName;

    private String nodeIp;

    protected final ConcurrentMap<TriggerKey, Boolean> locallyAcquiredTriggers = new ConcurrentHashMap<TriggerKey, Boolean>();

    protected final ConcurrentMap<TriggerKey, Boolean> locallyExecutingTriggers = new ConcurrentHashMap<TriggerKey, Boolean>();

    private Timer consistencyTimer;


    public ImprovedHazelcastJobStore() {
        super();
    }

    public long getMisfireThreshold() {
        return misfireThreshold;
    }

    public void setMisfireThreshold(long misfireThreshold) {
        if (misfireThreshold < 1) {
            throw new IllegalArgumentException("Misfire threshold must be larger than 0");
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
        triggersByKey = hazelcast.getMap(instanceName + '/' + "quartzTriggersByKey-0");
        jobsByKey = hazelcast.getMap(instanceName + '/' + "quartzJobsByKey-0");
        calendarsByName = hazelcast.getMap(instanceName + '/' + "quartzCalendarsByName-0");
        triggerGroups = hazelcast.getMap(instanceName + '/' + "quartzTriggerGroups-0");
        jobGroups = hazelcast.getMap(instanceName + '/' + "quartzJobGroups-0");
        lock = hazelcast.getLock(instanceName + '/' + "quartzJobStoreLock-0");
        blockedJobs = hazelcast.getSet(instanceName + '/' + "quartzBlockedJobs-0");
        nodeIp = hazelcast.getCluster().getLocalMember().getInetSocketAddress().getAddress().getHostAddress();

//TODO: deactivated due to bug #24647
//        if (triggersByKey.isEmpty()) {
//            triggersByKey.addIndex("trigger.nextFireTime", true);
//            triggersByKey.addIndex("trigger.misfireInstruction", false);
//            triggersByKey.addIndex("trigger.jobKey", false);
//            triggersByKey.addIndex("trigger.calendarName", false);
//        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initialized HazelcastJobStore.");
        }
    }

    @Override
    public void schedulerStarted() throws SchedulerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Scheduler was started. Starting consistency task...");
        }

        try {
            if (consistencyTimer != null) {
                consistencyTimer.cancel();
            }

            consistencyTimer = new Timer(true);
            consistencyTimer.schedule(new ConsistencyTask(this, locallyAcquiredTriggers, locallyExecutingTriggers), new Date(System.currentTimeMillis() + 60000L * 5), 60000L * 5);
        } catch (IllegalStateException e) {
            LOG.warn("Could not schedule consistency task", e);
        }
    }

    @Override
    public void schedulerPaused() {
        LOG.debug("Scheduler was paused. Cancelling consistency task...");

        if (consistencyTimer != null) {
            consistencyTimer.cancel();
            consistencyTimer = null;
        }
    }

    @Override
    public void schedulerResumed() {
        LOG.debug("Scheduler was resumed. Starting consistency task...");

        try {
            if (consistencyTimer != null) {
                consistencyTimer.cancel();
            }

            consistencyTimer = new Timer(true);
            consistencyTimer.schedule(new ConsistencyTask(this, locallyAcquiredTriggers, locallyExecutingTriggers), new Date(System.currentTimeMillis() + 60000L * 5), 60000L * 5);
        } catch (IllegalStateException e) {
            LOG.warn("Could not schedule consistency task", e);
        }
    }

    @Override
    public void shutdown() {
        LOG.debug("Scheduler was stopped. Cancelling consistency task...");

        if (consistencyTimer != null) {
            consistencyTimer.cancel();
            consistencyTimer = null;
        }
    }

    @Override
    public boolean supportsPersistence() {
        return false;
    }

    @Override
    public long getEstimatedTimeToReleaseAndAcquireTrigger() {
        return 2000;
    }

    @Override
    public boolean isClustered() {
        return true;
    }

    @Override
    public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
        if (replaceExisting) {
            jobsByKey.set(newJob.getKey(), newJob, 0, TimeUnit.SECONDS);
            jobGroups.putIfAbsent(newJob.getKey().getGroup(), false);
        } else {
            if (jobsByKey.putIfAbsent(newJob.getKey(), newJob) == null) {
                jobGroups.putIfAbsent(newJob.getKey().getGroup(), false);
            } else {
                throw new ObjectAlreadyExistsException(newJob);
            }
        }
    }

    @Override
    public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
        if (!replaceExisting && triggersByKey.containsKey(newTrigger.getKey())) {
            throw new ObjectAlreadyExistsException(newTrigger);
        }

        Boolean isPaused = triggerGroups.get(newTrigger.getKey().getGroup());
        if (isPaused == null) {
            isPaused = false;
        }

        JobKey jobKey = newTrigger.getJobKey();
        Boolean isJobPaused = jobGroups.get(jobKey.getGroup());
        if (isJobPaused == null) {
            isJobPaused = false;
        }

        TriggerStateWrapper stateWrapper;
        if (isPaused || isJobPaused) {
            if (blockedJobs.contains(jobKey)) {
                stateWrapper = new TriggerStateWrapper(newTrigger, TriggerStateWrapper.STATE_PAUSED_BLOCKED);
            } else {
                stateWrapper = new TriggerStateWrapper(newTrigger, TriggerStateWrapper.STATE_PAUSED);
            }
        } else {
            if (blockedJobs.contains(jobKey)) {
                stateWrapper = new TriggerStateWrapper(newTrigger, TriggerStateWrapper.STATE_BLOCKED);
            } else {
                stateWrapper = new TriggerStateWrapper(newTrigger);
            }
        }

        triggersByKey.set(newTrigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
        triggerGroups.putIfAbsent(newTrigger.getKey().getGroup(), isPaused);
    }

    @Override
    public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws ObjectAlreadyExistsException, JobPersistenceException {
        storeJob(newJob, false);
        storeTrigger(newTrigger, false);
    }

    @Override
    public void storeJobsAndTriggers(Map<JobDetail, List<Trigger>> triggersAndJobs, boolean replace) throws ObjectAlreadyExistsException, JobPersistenceException {
        if (!replace) {
            for (JobDetail jobDetail : triggersAndJobs.keySet()) {
                if (jobsByKey.containsKey(jobDetail.getKey())) {
                    throw new ObjectAlreadyExistsException(jobDetail);
                }

                List<Trigger> triggers = triggersAndJobs.get(jobDetail);
                for (Trigger trigger : triggers) {
                    if (triggersByKey.containsKey(trigger.getKey())) {
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
    }

    @Override
    public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
        JobDetail removed = jobsByKey.remove(jobKey);
        if (removed != null) {
            Set<TriggerKey> triggersToRemove = triggersByKey.keySet(new OtherTriggersForJobPredicate(null, jobKey));
            for (TriggerKey key : triggersToRemove) {
                triggersByKey.remove(key);
                GroupMatcher<TriggerKey> triggerGroupEquals = GroupMatcher.groupEquals(key.getGroup());
                Set<TriggerKey> triggerKeysForGroup = triggersByKey.keySet(new GroupMatcherPredicate<TriggerKey, TriggerStateWrapper>(triggerGroupEquals));
                if (triggerKeysForGroup.isEmpty()) {
                    triggerGroups.remove(key.getGroup());
                }
            }

            GroupMatcher<JobKey> jobGroupEquals = GroupMatcher.groupEquals(jobKey.getGroup());
            Set<JobKey> jobKeysForGroup = jobsByKey.keySet(new GroupMatcherPredicate<JobKey, JobDetail>(jobGroupEquals));
            if (jobKeysForGroup.isEmpty()) {
                jobGroups.remove(jobKey.getGroup());
            }
        }

        return true;
    }

    @Override
    public boolean removeJobs(List<JobKey> jobKeys) throws JobPersistenceException {
        boolean removedAll = true;
        for (JobKey jobKey : jobKeys) {
            boolean removed = removeJob(jobKey);
            removedAll = removedAll && removed;
        }

        return removedAll;
    }

    @Override
    public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
        return jobsByKey.get(jobKey);
    }

    @Override
    public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        TriggerStateWrapper stateWrapper = triggersByKey.remove(triggerKey);
        if (stateWrapper != null) {
            JobKey jobKey = stateWrapper.getTrigger().getJobKey();
            JobDetail jobDetail = jobsByKey.get(jobKey);
            if (jobDetail != null && !jobDetail.isDurable()) {
                Set<TriggerKey> otherTriggerKeys = triggersByKey.keySet(new OtherTriggersForJobPredicate(triggerKey, jobKey));
                if (otherTriggerKeys.isEmpty() && removeJob(jobKey)) {
                    signaler.notifySchedulerListenersJobDeleted(jobKey);
                }
            }

            GroupMatcher<TriggerKey> triggerGroupEquals = GroupMatcher.groupEquals(triggerKey.getGroup());
            Set<TriggerKey> triggerKeysForGroup = triggersByKey.keySet(new GroupMatcherPredicate<TriggerKey, TriggerStateWrapper>(triggerGroupEquals));
            if (triggerKeysForGroup.isEmpty()) {
                triggerGroups.remove(triggerKey.getGroup());
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean removeTriggers(List<TriggerKey> triggerKeys) throws JobPersistenceException {
        boolean removedAll = true;
        for (TriggerKey triggerKey : triggerKeys) {
            boolean removed = removeTrigger(triggerKey);
            removedAll = removedAll && removed;
        }

        return removedAll;
    }

    @Override
    public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
        boolean retval = false;
        TriggerStateWrapper stateWrapper = triggersByKey.get(triggerKey);
        if (stateWrapper != null) {
            if (!stateWrapper.getTrigger().getJobKey().equals(newTrigger.getJobKey())) {
                throw new JobPersistenceException("New trigger is not related to the same job as the old trigger.");
            }

            retval = true;
        }

        try {
            storeTrigger(newTrigger, false);
            return retval;
        } catch(JobPersistenceException e) {
            storeTrigger((OperableTrigger)stateWrapper.getTrigger(), false); // put previous trigger back...
            throw e;
        }
    }

    @Override
    public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        TriggerStateWrapper stateWrapper = triggersByKey.get(triggerKey);
        if (stateWrapper == null) {
            return null;
        }

        return (OperableTrigger) stateWrapper.getTrigger();
    }

    @Override
    public boolean checkExists(JobKey jobKey) throws JobPersistenceException {
        return jobsByKey.containsKey(jobKey);
    }

    @Override
    public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
        return triggersByKey.containsKey(triggerKey);
    }

    @Override
    public void clearAllSchedulingData() throws JobPersistenceException {
        triggersByKey.clear();
        jobsByKey.clear();
        calendarsByName.clear();
        triggerGroups.clear();
        jobGroups.clear();
    }

    @Override
    public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers) throws ObjectAlreadyExistsException, JobPersistenceException {
        boolean exists = calendarsByName.containsKey(name);
        if (exists && !replaceExisting) {
            throw new ObjectAlreadyExistsException("Calendar with name '" + name + "' already exists.");
        }

        calendarsByName.set(name, calendar, 0, TimeUnit.SECONDS);
        if (exists && updateTriggers) {
            Collection<TriggerStateWrapper> triggersToUpdate = triggersByKey.values(new TriggersForCalendarPredicate(name));
            for (TriggerStateWrapper stateWrapper : triggersToUpdate) {
                OperableTrigger trigger = (OperableTrigger) stateWrapper.getTrigger();
                trigger.updateWithNewCalendar(calendar, getMisfireThreshold());
                triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public boolean removeCalendar(String calName) throws JobPersistenceException {
        Set<TriggerKey> triggerKeys = triggersByKey.keySet(new TriggersForCalendarPredicate(calName));
        if (triggerKeys.size() > 0) {
            throw new JobPersistenceException("Calender cannot be removed, it is referenced by a Trigger!");
        }

        return calendarsByName.remove(calName) != null;
    }

    @Override
    public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
        return calendarsByName.get(calName);
    }

    @Override
    public int getNumberOfJobs() throws JobPersistenceException {
        return jobsByKey.size();
    }

    @Override
    public int getNumberOfTriggers() throws JobPersistenceException {
        return triggersByKey.size();
    }

    @Override
    public int getNumberOfCalendars() throws JobPersistenceException {
        return calendarsByName.size();
    }

    @Override
    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        return jobsByKey.keySet(new GroupMatcherPredicate<JobKey, JobDetail>(matcher));
    }

    @Override
    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        return triggersByKey.keySet(new GroupMatcherPredicate<TriggerKey, TriggerStateWrapper>(matcher));
    }

    @Override
    public List<String> getJobGroupNames() throws JobPersistenceException {
        return new ArrayList<String>(jobGroups.keySet());
    }

    @Override
    public List<String> getTriggerGroupNames() throws JobPersistenceException {
        return new ArrayList<String>(triggerGroups.keySet());
    }

    @Override
    public List<String> getCalendarNames() throws JobPersistenceException {
        return new ArrayList<String>(calendarsByName.keySet());
    }

    @Override
    public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
        Collection<TriggerStateWrapper> triggers = triggersByKey.values(new OtherTriggersForJobPredicate(null, jobKey));
        List<OperableTrigger> operableTriggers = new ArrayList<OperableTrigger>();
        for (TriggerStateWrapper stateWrapper : triggers) {
            operableTriggers.add((OperableTrigger) stateWrapper.getTrigger());
        }

        return operableTriggers;
    }

    @Override
    public TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
        TriggerStateWrapper trigger = triggersByKey.get(triggerKey);
        if (trigger == null) {
            return TriggerState.NONE;
        }

        if (trigger.getState() == TriggerStateWrapper.STATE_COMPLETE) {
            return TriggerState.COMPLETE;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_PAUSED) {
            return TriggerState.PAUSED;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_PAUSED_BLOCKED) {
            return TriggerState.PAUSED;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_ERROR) {
            return TriggerState.ERROR;
        } else if (trigger.getState() == TriggerStateWrapper.STATE_BLOCKED) {
            return TriggerState.BLOCKED;
        }

        return TriggerState.NORMAL;
    }

    @Override
    public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        TriggerStateWrapper trigger = triggersByKey.get(triggerKey);
        if (trigger == null || trigger.getState() == TriggerStateWrapper.STATE_COMPLETE) {
            return;
        }

        if (trigger.getState() == TriggerStateWrapper.STATE_BLOCKED) {
            trigger.setState(TriggerStateWrapper.STATE_PAUSED_BLOCKED);
        } else {
            trigger.setState(TriggerStateWrapper.STATE_PAUSED);
        }

        triggersByKey.set(triggerKey, trigger, 0, TimeUnit.SECONDS);
    }

    @Override
    public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        Collection<TriggerStateWrapper> triggers = triggersByKey.values(new GroupMatcherPredicate<TriggerKey, TriggerStateWrapper>(matcher));
        Set<String> groupsToPause = new HashSet<String>();
        for (TriggerStateWrapper stateWrapper : triggers) {
            if (stateWrapper.getState() == TriggerStateWrapper.STATE_COMPLETE) {
                continue;
            } else if (stateWrapper.getState() == TriggerStateWrapper.STATE_BLOCKED) {
                stateWrapper.setState(TriggerStateWrapper.STATE_PAUSED_BLOCKED);
            } else {
                stateWrapper.setState(TriggerStateWrapper.STATE_PAUSED);
            }

            triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
            groupsToPause.add(stateWrapper.getTrigger().getKey().getGroup());
        }

        for (String group : groupsToPause) {
            triggerGroups.set(group, true, 0, TimeUnit.SECONDS);
        }

        return groupsToPause;
    }

    @Override
    public void pauseJob(JobKey jobKey) throws JobPersistenceException {
        Collection<TriggerStateWrapper> triggersToPause = triggersByKey.values(new OtherTriggersForJobPredicate(null, jobKey));
        for (TriggerStateWrapper stateWrapper : triggersToPause) {
            if (stateWrapper.getState() == TriggerStateWrapper.STATE_COMPLETE) {
                continue;
            } else if (stateWrapper.getState() == TriggerStateWrapper.STATE_BLOCKED) {
                stateWrapper.setState(TriggerStateWrapper.STATE_PAUSED_BLOCKED);
            } else {
                stateWrapper.setState(TriggerStateWrapper.STATE_PAUSED);
            }

            triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public Collection<String> pauseJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        Set<JobKey> jobsToPause = jobsByKey.keySet(new GroupMatcherPredicate<JobKey, JobDetail>(matcher));
        Set<String> groupsToPause = new HashSet<String>();
        for (JobKey jobKey : jobsToPause) {
            pauseJob(jobKey);
            groupsToPause.add(jobKey.getGroup());
        }

        for (String group : groupsToPause) {
            jobGroups.set(group, true, 0, TimeUnit.SECONDS);
        }

        return groupsToPause;
    }

    @Override
    public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
        TriggerStateWrapper stateWrapper = triggersByKey.get(triggerKey);
        if (stateWrapper == null || (stateWrapper.getState() != TriggerStateWrapper.STATE_PAUSED && stateWrapper.getState() != TriggerStateWrapper.STATE_PAUSED_BLOCKED)) {
            return;
        }

        if (blockedJobs.contains(stateWrapper.getTrigger().getJobKey())) {
            stateWrapper.setState(TriggerStateWrapper.STATE_BLOCKED);
        } else {
            stateWrapper.setState(TriggerStateWrapper.STATE_WAITING);
        }
        if (!applyMisfire(stateWrapper)) {
            triggersByKey.set(triggerKey, stateWrapper, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
        Collection<TriggerStateWrapper> triggers = triggersByKey.values(new GroupMatcherPredicate<TriggerKey, TriggerStateWrapper>(matcher));
        Set<String> groupsToResume = new HashSet<String>();
        for (TriggerStateWrapper stateWrapper : triggers) {
            if (stateWrapper.getState() == TriggerStateWrapper.STATE_PAUSED) {
                stateWrapper.setState(TriggerStateWrapper.STATE_WAITING);
            } else if (stateWrapper.getState() == TriggerStateWrapper.STATE_PAUSED_BLOCKED) {
                stateWrapper.setState(TriggerStateWrapper.STATE_BLOCKED);
            } else {
                continue;
            }

            triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
            groupsToResume.add(stateWrapper.getTrigger().getKey().getGroup());
        }

        for (String group : groupsToResume) {
            triggerGroups.set(group, false, 0, TimeUnit.SECONDS);
        }

        return groupsToResume;
    }

    @Override
    public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
        return triggerGroups.keySet(new SqlPredicate("this = true"));
    }

    @Override
    public void resumeJob(JobKey jobKey) throws JobPersistenceException {
        Collection<TriggerStateWrapper> triggersToResume = triggersByKey.values(new OtherTriggersForJobPredicate(null, jobKey));
        for (TriggerStateWrapper stateWrapper : triggersToResume) {
            if (stateWrapper.getState() == TriggerStateWrapper.STATE_PAUSED) {
                stateWrapper.setState(TriggerStateWrapper.STATE_WAITING);
            } else if (stateWrapper.getState() == TriggerStateWrapper.STATE_PAUSED_BLOCKED) {
                stateWrapper.setState(TriggerStateWrapper.STATE_BLOCKED);
            } else {
                continue;
            }

            triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
        Set<JobKey> jobsToResume = jobsByKey.keySet(new GroupMatcherPredicate<JobKey, JobDetail>(matcher));
        Set<String> groupsToResume = new HashSet<String>();
        for (JobKey jobKey : jobsToResume) {
            resumeJob(jobKey);
            groupsToResume.add(jobKey.getGroup());
        }

        for (String group : groupsToResume) {
            jobGroups.set(group, false, 0, TimeUnit.SECONDS);
        }

        return groupsToResume;
    }

    @Override
    public void pauseAll() throws JobPersistenceException {
        Set<String> groups = triggerGroups.keySet();
        for (String group : groups) {
            pauseTriggers(GroupMatcher.triggerGroupEquals(group));
        }
    }

    @Override
    public void resumeAll() throws JobPersistenceException {
        Set<String> groups = triggerGroups.keySet();
        for (String group : groups) {
            resumeTriggers(GroupMatcher.triggerGroupEquals(group));
        }
    }

    @Override
    public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        StringBuilder logBuilder = null;

        final boolean traceEnabled = LOG.isTraceEnabled();
        if (traceEnabled) {
            logBuilder = new StringBuilder();
            logBuilder.append("Acquiring triggers at ");
            logBuilder.append(startTime);
            logBuilder.append("\n");
        }

        List<OperableTrigger> returnList = new ArrayList<OperableTrigger>();
        lock.lock();
        if (logBuilder != null) {
            logBuilder.append("    Getting lock took ");
            long now = System.currentTimeMillis();
            logBuilder.append(now - lastTime);
            lastTime = now;
            logBuilder.append("ms.\n");
        }

        long firstAcquiredTriggerFireTime = 0L;
        try {
            Collection<TriggerStateWrapper> filteredTriggers = triggersByKey.values(new SelectTriggersPredicate(noLaterThan, timeWindow));
            if (logBuilder != null) {
                logBuilder.append("    Filtering ");
                logBuilder.append(filteredTriggers.size());
                logBuilder.append(" triggers took ");
                long now = System.currentTimeMillis();
                logBuilder.append(now - lastTime);
                lastTime = now;
                logBuilder.append("ms.\n");
            }

            if (filteredTriggers == null || filteredTriggers.isEmpty()) {
                return returnList;
            }

            ArrayList<TriggerStateWrapper> triggers = new ArrayList<TriggerStateWrapper>(filteredTriggers);
            Collections.sort(triggers, new TriggerWrapperTimeComparator());
            if (logBuilder != null) {
                logBuilder.append("    Sorting triggers took ");
                long now = System.currentTimeMillis();
                logBuilder.append(now - lastTime);
                lastTime = now;
                logBuilder.append("ms.\n");
            }
            Set<JobKey> excluded = new HashSet<JobKey>();
            for (TriggerStateWrapper stateWrapper : triggers) {
                if (stateWrapper.getTrigger().getNextFireTime() == null || stateWrapper.getState() == TriggerStateWrapper.STATE_COMPLETE) {
                    if (traceEnabled) {
                        LOG.trace("Removing trigger {}", stateWrapper.getTrigger().getKey().getName());
                    }

                    removeTrigger(stateWrapper.getTrigger().getKey());
                    continue;
                }

                if (firstAcquiredTriggerFireTime > 0
                    && stateWrapper.getTrigger().getNextFireTime().getTime() > (firstAcquiredTriggerFireTime + timeWindow)) {
                    break;
                }

                if (applyMisfire(stateWrapper)) {
                    continue;
                }

                JobKey jobKey = stateWrapper.getTrigger().getJobKey();
                JobDetail jobDetail = jobsByKey.get(jobKey);
                if (jobDetail == null) {
                    continue;
                }

                if (jobDetail.isConcurrentExectionDisallowed()) {
                    if (excluded.contains(jobKey)) {
                        continue;
                    }

                    excluded.add(jobKey);
                }

                stateWrapper.setState(TriggerStateWrapper.STATE_ACQUIRED);
                stateWrapper.setOwner(nodeIp);
                OperableTrigger trigger = (OperableTrigger) stateWrapper.getTrigger();
                trigger.setFireInstanceId(getFiredTriggerRecordId());
                triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
                locallyAcquiredTriggers.put(stateWrapper.getTrigger().getKey(), true);
                if (firstAcquiredTriggerFireTime == 0) {
                    firstAcquiredTriggerFireTime = trigger.getNextFireTime().getTime();
                }

                returnList.add(trigger);
                if (returnList.size() == maxCount) {
                    break;
                }
            }

            if (logBuilder != null) {
                logBuilder.append("    Processing triggers took ");
                long now = System.currentTimeMillis();
                logBuilder.append(now - lastTime);
                lastTime = now;
                logBuilder.append("ms.\n");
            }
            return returnList;
        } finally {
            if (logBuilder == null) {
                lock.unlock();
            } else {
                lastTime = System.currentTimeMillis();
                lock.unlock();
                long now = System.currentTimeMillis();
                logBuilder.append("    Releasing lock took ");
                logBuilder.append(now - lastTime);
                logBuilder.append("ms.\n    Overall duration: ");
                logBuilder.append(now - startTime);
                logBuilder.append("ms (");
                logBuilder.append(now);
                logBuilder.append(").\n    Returning ");
                logBuilder.append(returnList.size());
                logBuilder.append(" triggers.");
                for (OperableTrigger trigger : returnList) {
                    logBuilder.append("\n        Trigger: ").append(trigger.getKey().getName());
                }

                LOG.trace(logBuilder.toString());
            }
        }
    }

    private static final AtomicLong ftrCtr = new AtomicLong(System.currentTimeMillis());

    protected String getFiredTriggerRecordId() {
        return String.valueOf(ftrCtr.incrementAndGet());
    }

    @Override
    public void releaseAcquiredTrigger(OperableTrigger trigger) throws JobPersistenceException {
        lock.lock();

        final boolean traceEnabled = LOG.isTraceEnabled();
        if (traceEnabled) {
            LOG.trace("Got lock. {}", System.nanoTime());
        }

        try {
            TriggerStateWrapper stateWrapper = triggersByKey.remove(trigger.getKey());
            if (stateWrapper == null || stateWrapper.getState() != TriggerStateWrapper.STATE_ACQUIRED) {
                return;
            }

            stateWrapper.setState(TriggerStateWrapper.STATE_WAITING);
            stateWrapper.resetOwner();
            triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
            locallyAcquiredTriggers.remove(trigger.getKey());
        } finally {
            lock.unlock();

            if (traceEnabled) {
                StringBuilder sb = new StringBuilder("Releasing lock. ");
                sb.append(System.nanoTime()).append(". ");
                sb.append("\n    Trigger: ").append(trigger.getKey().getName());

                LOG.trace(sb.toString());
            }
        }
    }

    @Override
    public List<TriggerFiredResult> triggersFired(List<OperableTrigger> firedTriggers) throws JobPersistenceException {
        List<TriggerFiredResult> results = new ArrayList<TriggerFiredResult>();
        lock.lock();

        final boolean traceEnabled = LOG.isTraceEnabled();
        if (traceEnabled) {
            LOG.trace("Got lock. {}", System.nanoTime());
        }

        try {
            for (OperableTrigger trigger : firedTriggers) {
                TriggerStateWrapper stateWrapper = triggersByKey.get(trigger.getKey());
                if (stateWrapper == null || stateWrapper.getState() != TriggerStateWrapper.STATE_ACQUIRED) {
                    continue;
                }

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
                TriggerStateWrapper firedWrapper = new TriggerStateWrapper(trigger, TriggerStateWrapper.STATE_EXECUTING);
                firedWrapper.setOwner(nodeIp);
                triggersByKey.set(trigger.getKey(), firedWrapper, 0, TimeUnit.SECONDS);
                locallyAcquiredTriggers.remove(trigger.getKey());
                locallyExecutingTriggers.put(trigger.getKey(), true);

                TriggerFiredResult result;
                JobKey jobKey = trigger.getJobKey();
                if (jobKey == null) {
                    result = new TriggerFiredResult(new JobPersistenceException("Job could not be found."));
                    results.add(result);
                    continue;
                }

                JobDetail job = jobsByKey.get(jobKey);
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

                    if (job.isConcurrentExectionDisallowed()) {
                        Collection<TriggerStateWrapper> otherTriggers = triggersByKey.values(new OtherTriggersForJobPredicate(stateWrapper.getTrigger().getKey(), jobKey));
                        for (TriggerStateWrapper triggerToBlock : otherTriggers) {
                            if (triggerToBlock.getState() == TriggerStateWrapper.STATE_WAITING) {
                                triggerToBlock.setState(TriggerStateWrapper.STATE_BLOCKED);
                            } else if (triggerToBlock.getState() == TriggerStateWrapper.STATE_PAUSED) {
                                triggerToBlock.setState(TriggerStateWrapper.STATE_PAUSED_BLOCKED);
                            }

                            triggersByKey.set(triggerToBlock.getTrigger().getKey(), triggerToBlock, 0, TimeUnit.SECONDS);
                        }

                        blockedJobs.add(jobKey);
                    }
                }

                results.add(result);
            }

            return results;
        } finally {
            lock.unlock();
            if (traceEnabled) {
                StringBuilder sb = new StringBuilder("Releasing lock. ");
                sb.append(System.nanoTime()).append(". ");
                for (OperableTrigger trigger : firedTriggers) {
                    sb.append("\n    Trigger: ").append(trigger.getKey().getName());
                }

                LOG.trace(sb.toString());
            }
        }
    }

    @Override
    public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail, CompletedExecutionInstruction triggerInstCode) throws JobPersistenceException {
        lock.lock();

        final boolean traceEnabled = LOG.isTraceEnabled();
        if (traceEnabled) {
            LOG.trace("Got lock. {}", System.nanoTime());
        }

        try {
            JobKey jobKey = jobDetail.getKey();
            Collection<TriggerStateWrapper> otherTriggers = null;
            if (jobDetail.isConcurrentExectionDisallowed() || jobDetail.isPersistJobDataAfterExecution()) {
                if (jobsByKey.containsKey(jobKey)) {
                    if (jobDetail.isPersistJobDataAfterExecution()) {
                        JobDataMap newData = jobDetail.getJobDataMap();
                        if (newData != null) {
                            newData.clearDirtyFlag();
                        }

                        ((JobDetailImpl) jobDetail).setJobDataMap(newData);
                        jobsByKey.put(jobKey, jobDetail);
                    }

                    if (jobDetail.isConcurrentExectionDisallowed()) {
                        blockedJobs.remove(jobKey);
                        otherTriggers = triggersByKey.values(new OtherTriggersForJobPredicate(
                            trigger.getKey(),
                            jobKey));
                        for (TriggerStateWrapper triggerToUnblock : otherTriggers) {
                            if (triggerToUnblock.getState() == TriggerStateWrapper.STATE_BLOCKED) {
                                triggerToUnblock.setState(TriggerStateWrapper.STATE_WAITING);
                            } else if (triggerToUnblock.getState() == TriggerStateWrapper.STATE_PAUSED_BLOCKED) {
                                triggerToUnblock.setState(TriggerStateWrapper.STATE_PAUSED);
                            }

                            triggersByKey.set(triggerToUnblock.getTrigger().getKey(), triggerToUnblock, 0, TimeUnit.SECONDS);
                        }

                        signaler.signalSchedulingChange(0L);
                    }
                } else {
                    blockedJobs.remove(jobKey);
                }
            }

            locallyExecutingTriggers.remove(trigger.getKey());
            TriggerStateWrapper stateWrapper = triggersByKey.get(trigger.getKey());
            if (stateWrapper != null) {
                stateWrapper.resetOwner();
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
                    stateWrapper.setState(TriggerStateWrapper.STATE_COMPLETE);
                    triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
                    signaler.signalSchedulingChange(0L);
                } else if(triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_ERROR) {
                    stateWrapper.setState(TriggerStateWrapper.STATE_ERROR);
                    triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR) {
                    stateWrapper.setState(TriggerStateWrapper.STATE_ERROR);
                    triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
                    if (otherTriggers == null) {
                        otherTriggers = triggersByKey.values(new OtherTriggersForJobPredicate(
                            trigger.getKey(),
                            jobKey));
                    }

                    for (TriggerStateWrapper triggerToChange : otherTriggers) {
                        triggerToChange.setState(TriggerStateWrapper.STATE_ERROR);
                        triggersByKey.set(triggerToChange.getTrigger().getKey(), triggerToChange, 0, TimeUnit.SECONDS);
                    }

                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
                    stateWrapper.setState(TriggerStateWrapper.STATE_COMPLETE);
                    triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
                    if (otherTriggers == null) {
                        otherTriggers = triggersByKey.values(new OtherTriggersForJobPredicate(
                            trigger.getKey(),
                            jobKey));
                    }

                    for (TriggerStateWrapper triggerToChange : otherTriggers) {
                        triggerToChange.setState(TriggerStateWrapper.STATE_COMPLETE);
                        triggersByKey.set(triggerToChange.getTrigger().getKey(), triggerToChange, 0, TimeUnit.SECONDS);
                    }

                    signaler.signalSchedulingChange(0L);
                } else {
                    stateWrapper.setState(TriggerStateWrapper.STATE_WAITING);
                    triggersByKey.set(trigger.getKey(), stateWrapper, 0, TimeUnit.SECONDS);
                }
            }
        } finally {
            lock.unlock();
            if (traceEnabled) {
                StringBuilder sb = new StringBuilder("Releasing lock. ");
                sb.append(System.nanoTime()).append(". ");
                sb.append("\n    Trigger: ").append(trigger.getKey().getName());

                LOG.trace(sb.toString());
            }
        }
    }

    @Override
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public void setThreadPoolSize(int poolSize) {
        //
    }

    public IMap<TriggerKey, TriggerStateWrapper> getTriggerMap() {
        return triggersByKey;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public ILock getClusterLock() {
        return lock;
    }

    public SchedulerSignaler getSignaler() {
        return signaler;
    }

    public ISet<JobKey> getBlockedJobs() {
        return blockedJobs;
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
            calendar = calendarsByName.get(calendarName);
        }

        signaler.notifyTriggerListenersMisfired(stateWrapper.getTrigger());
        ((OperableTrigger) stateWrapper.getTrigger()).updateAfterMisfire(calendar);
        if (stateWrapper.getTrigger().getNextFireTime() == null) {
            stateWrapper.setState(TriggerStateWrapper.STATE_COMPLETE);
            triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
            signaler.notifySchedulerListenersFinalized(stateWrapper.getTrigger());
        } else if (nextFireTime.equals(stateWrapper.getTrigger().getNextFireTime())) {
            return false;
        } else {
            triggersByKey.set(stateWrapper.getTrigger().getKey(), stateWrapper, 0, TimeUnit.SECONDS);
        }

        LOG.trace("Applied misfire on trigger: {}", stateWrapper.getTrigger().getKey().getName());
        return true;
    }

    protected HazelcastInstance getHazelcast() throws JobPersistenceException {
        HazelcastInstance hazelcast = Services.optService(HazelcastInstance.class);
        if (hazelcast == null) {
            throw new JobPersistenceException("Hazelcast Service is not available.");
        }

        return hazelcast;
    }

}
