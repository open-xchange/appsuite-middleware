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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.gdpr.dataexport.impl;

import static com.openexchange.gdpr.dataexport.impl.DataExportUtility.stringFor;
import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;
import static com.openexchange.java.Autoboxing.I;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExport;
import com.openexchange.gdpr.dataexport.DataExportArguments;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportConstants;
import com.openexchange.gdpr.dataexport.DataExportDownload;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportJob;
import com.openexchange.gdpr.dataexport.DataExportProvider;
import com.openexchange.gdpr.dataexport.DataExportProviderRegistry;
import com.openexchange.gdpr.dataexport.DataExportResultFile;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStatus;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportTaskInfo;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.gdpr.dataexport.DayOfWeekTimeRanges;
import com.openexchange.gdpr.dataexport.DefaultDataExport;
import com.openexchange.gdpr.dataexport.DefaultDataExportResultFile;
import com.openexchange.gdpr.dataexport.FileLocation;
import com.openexchange.gdpr.dataexport.FileLocations;
import com.openexchange.gdpr.dataexport.HostInfo;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.gdpr.dataexport.TimeOfTheDay;
import com.openexchange.gdpr.dataexport.TimeRange;
import com.openexchange.gdpr.dataexport.impl.notification.DataExportNotificationSender;
import com.openexchange.gdpr.dataexport.impl.notification.Reason;
import com.openexchange.java.ISO8601Utils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * {@link DataExportServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportServiceImpl implements DataExportService {

    /** The logger */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportServiceImpl.class);

    private final DataExportStorageService storageService;
    private final DataExportConfig config;
    private final ServiceLookup services;
    private final ConcurrentMap<Future<Void>, DataExportTaskExecution> executions;
    private final DataExportProviderRegistry providerRegistry;

    private final AtomicReference<ScheduledTimerTask> periodicStartTask;
    private final AtomicReference<ScheduledTimerTask> stopTask;
    private final AtomicReference<ScheduledTimerTask> nextRunTask;

    private final ScheduledTimerTask checkAbortedTimerTask;


    /**
     * Initializes a new {@link DataExportServiceImpl}.
     *
     * @param config The data export configuration
     * @param storageService The storage service for data export
     * @param providerRegistry The provider registry for obtaining appropriate instances of <code>DataExportProvider</code>
     * @param services The service look-up
     * @throws OXException If initialization fails
     */
    public DataExportServiceImpl(DataExportConfig config, DataExportStorageService storageService, DataExportProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super();
        this.config = config;
        this.storageService = storageService;
        this.providerRegistry = providerRegistry;
        this.services = services;
        ConcurrentMap<Future<Void>, DataExportTaskExecution> executions = new ConcurrentHashMap<>(config.getNumberOfConcurrentTasks(), 0.9F, 1);
        this.executions = executions;
        periodicStartTask = new AtomicReference<>(null);
        stopTask = new AtomicReference<>(null);
        nextRunTask = new AtomicReference<>(null);

        TimerService timerService = services.getServiceSafe(TimerService.class);
        Runnable abortedChecker = new Runnable() {

            @Override
            public void run() {
                for (DataExportTaskExecution execution : executions.values()) {
                    Optional<DataExportTask> optionalTask = execution.getCurrentTask();
                    if (optionalTask.isPresent()) {
                        try {
                            DataExportTask task = optionalTask.get();
                            Optional<DataExportStatus> optionalStatus = storageService.getDataExportStatus(task.getUserId(), task.getContextId());
                            if (optionalStatus.isPresent() && optionalStatus.get().isAborted()) {
                                // Task has been aborted
                                execution.stop();
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to retrieve status for data export task.", e);
                        }
                    }
                }

                // Delete expired aborted tasks and completed tasks that exceed max. time-to-live.
                List<DataExportTaskInfo> withPendingNotification = null;
                try {
                    withPendingNotification = storageService.deleteCompletedOrAbortedTasksAndGetTasksWithPendingNotification();
                } catch (Exception e) {
                    LOG.warn("Failed to delete aborted or completed data export tasks", e);
                }

                // Check for tasks with pending notification
                if (withPendingNotification != null && !withPendingNotification.isEmpty()) {
                    for (DataExportTaskInfo taskInfo : withPendingNotification) {
                        Reason reason;
                        switch (taskInfo.getStatus()) {
                            case ABORTED:
                                reason = Reason.ABORTED;
                                break;
                            case DONE:
                                reason = Reason.SUCCESS;
                                break;
                            case FAILED:
                                reason = Reason.FAILED;
                                break;
                            default:
                                reason = null;
                                break;
                        }

                        if (reason != null) {
                            UUID taskId = taskInfo.getTaskId();
                            int userId = taskInfo.getUserId();
                            int contextId = taskInfo.getContextId();

                            HostInfo hostInfo = null;
                            Date expiryDate = null;
                            Date creationDate = null;
                            if (Reason.SUCCESS == reason) {
                                try {
                                    Optional<Date> lastAccessedTimeStamp = storageService.getLastAccessedTimeStamp(userId, contextId);
                                    expiryDate = new Date(lastAccessedTimeStamp.get().getTime() + config.getMaxTimeToLiveMillis());
                                } catch (Exception e) {
                                    expiryDate = new Date(System.currentTimeMillis() + config.getMaxTimeToLiveMillis());
                                    LOG.warn("Failed to query last-accessed time stamp from data export task {} of user {} in context {}. Assuming \"{}\" as expiration date.", stringFor(taskId), I(userId), I(contextId), ISO8601Utils.format(expiryDate), e);
                                }
                                try {
                                    Optional<DataExportTask> optionalTask = storageService.getDataExportTask(userId, contextId);
                                    if (optionalTask.isPresent()) {
                                        DataExportTask task = optionalTask.get();
                                        hostInfo = task.getArguments().getHostInfo();
                                        creationDate = task.getCreationTime();
                                    }
                                } catch (Exception e) {
                                    LOG.warn("Failed loading data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                                }
                            }

                            try {
                                DataExportNotificationSender.sendNotificationAndSetMarker(reason, creationDate == null ? new Date() : creationDate, expiryDate, hostInfo, taskId, userId, contextId, true, services);
                            } catch (Exception e) {
                                LOG.warn("Cannot set notification-sent marker for data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                            }
                        }
                    }
                }
            }
        };
        long delayMillis = config.getCheckForAbortedTasksFrequency();
        checkAbortedTimerTask = timerService.scheduleAtFixedRate(abortedChecker, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public DataExportConfig getConfig() {
        return config;
    }

    @Override
    public List<DataExportTask> getDataExportTasks() throws OXException {
        return storageService.getDataExportTasks();
    }

    @Override
    public Optional<UUID> submitDataExportTaskIfAbsent(DataExportArguments args, Session session) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();

        if (args.getMaxFileSize() > 0 && args.getMaxFileSize() < DataExportConstants.MINIMUM_FILE_SIZE) {
            throw DataExportExceptionCode.INVALID_FILE_SIZE.create(getSize(DataExportConstants.MINIMUM_FILE_SIZE, 2, false, true));
        }

        List<Module> modulesToExport = args.getModules();
        if (modulesToExport == null) {
            throw DataExportExceptionCode.NO_MODULES_SPECIFIED.create();
        }

        int numberOfModulesToExport = modulesToExport.size();
        if (numberOfModulesToExport <= 0) {
            throw DataExportExceptionCode.NO_MODULES_SPECIFIED.create();
        }

        int fileStorageId;
        {
            ConfigViewFactory viewFactory = services.getServiceSafe(ConfigViewFactory.class);
            ConfigView view = viewFactory.getView(userId, contextId);

            fileStorageId = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.gdpr.dataexport.fileStorageId", -1, view);
            if (fileStorageId <= 0) {
                throw DataExportExceptionCode.NO_FILE_STORAGE_SPECIFIED.create();
            }
        }

        List<DataExportWorkItem> workItems = new ArrayList<>(numberOfModulesToExport);
        for (Module module : modulesToExport) {
            String moduleId = module.getId();
            Optional<DataExportProvider> optionalProvider = providerRegistry.getHighestRankedProviderFor(moduleId);
            if (!optionalProvider.isPresent()) {
                throw DataExportExceptionCode.NO_SUCH_PROVIDER.create(moduleId);
            }

            boolean enabled = optionalProvider.get().checkArguments(args, session);
            if (enabled) {
                DataExportWorkItem workItem = new DataExportWorkItem();
                workItem.setId(UUID.randomUUID());
                workItem.setModuleId(moduleId);
                workItems.add(workItem);
            }
        }

        UUID taskId = UUID.randomUUID();

        DataExportTask task = new DataExportTask();
        task.setContextId(contextId);
        task.setUserId(userId);
        task.setFileStorageId(fileStorageId);
        task.setId(taskId);
        task.setWorkItems(workItems);
        task.setArguments(args);

        boolean created = storageService.createIfAbsent(task, userId, contextId);
        if (created) {
            LOG.info("Submitted data export task {} for user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(contextId));
            return Optional.of(taskId);
        }
        return Optional.empty();
    }

    @Override
    public boolean cancelDataExportTask(int userId, int contextId) throws OXException {
        Optional<DataExportTask> optionalTask = storageService.getDataExportTask(userId, contextId);
        if (!optionalTask.isPresent()) {
            return false;
        }

        // Check status
        DataExportTask task = optionalTask.get();
        DataExportStatus status = task.getStatus();
        if (status.isDone() || status.isFailed()) {
            return false;
        }

        if (status.isAborted()) {
            // Already marked as aborted
            return true;
        }

        // Mark as aborted
        if (!storageService.markAborted(task.getId(), userId, contextId)) {
            return false;
        }

        // Check if this node is currently executing the task
        stopQuietly(task);
        LOG.info("Requested cancelation for data export task {} of user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(contextId));
        return true;
    }

    private void stopQuietly(DataExportTask task) {
        Optional<DataExportTask> optionalTask;
        try {
            Map<Future<Void>, DataExportTaskExecution> executions = this.executions;
            for (DataExportTaskExecution execution : executions.values()) {
                optionalTask = execution.getCurrentTask();
                if (optionalTask.isPresent() && equals(task, optionalTask.get())) {
                    execution.stop();
                }
            }
        } catch (Exception e) {
            // Stop attempt failed
            LOG.warn("Failed to locally stop task {} from user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(task.getContextId()));
        }
    }

    @Override
    public List<UUID> cancelDataExportTasks(int contextId) throws OXException {
        List<DataExportTask> tasks = storageService.getDataExportTasks(contextId);
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> taskIds = new ArrayList<>(tasks.size());
        for (DataExportTask task : tasks) {
            DataExportStatus status = task.getStatus();
            if (!status.isDone() && !status.isFailed()) {
                if (status.isAborted()) {
                    // Already marked as aborted
                    taskIds.add(task.getId());
                } else {
                    // Mark as aborted
                    try {
                        if (storageService.markAborted(task.getId(), task.getUserId(), contextId)) {
                            stopQuietly(task);
                            taskIds.add(task.getId());
                            LOG.info("Requested cancelation for data export task {} of user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(contextId));
                        }
                    } catch (Exception e) {
                        LOG.warn("Failed to abort task {} from user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(contextId));
                    }
                }
            }
        }
        return taskIds;
    }

    @Override
    public boolean deleteDataExportTask(int userId, int contextId) throws OXException {
        Optional<DataExportTask> optionalTask = storageService.getDataExportTask(userId, contextId);
        if (!optionalTask.isPresent()) {
            return false;
        }

        // Check status
        DataExportTask task = optionalTask.get();
        DataExportStatus status = task.getStatus();
        if (!status.isDone() && !status.isFailed()) {
            return false;
        }

        boolean deleted = storageService.deleteDataExportTask(userId, contextId);
        if (deleted) {
            LOG.info("Deleted data export task {} (incl. all resources and artifacts) of user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(contextId));
        }
        return deleted;
    }

    @Override
    public Optional<DataExportTask> getDataExportTask(int userId, int contextId) throws OXException {
        return storageService.getDataExportTask(userId, contextId);
    }

    @Override
    public List<DataExportTask> getDataExportTasks(int contextId) throws OXException {
        return storageService.getDataExportTasks(contextId);
    }

    @Override
    public Optional<DataExport> getDataExport(Session session) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();

        Optional<DataExportTask> optionalTask = storageService.getDataExportTask(userId, contextId);
        if (!optionalTask.isPresent()) {
            return Optional.empty();
        }

        // Check task's status
        DataExportTask task = optionalTask.get();
        DataExportStatus status = task.getStatus();
        if (!status.isDone()) {
            return Optional.of(DefaultDataExport.builder().withTask(task).build());
        }

        // Task is done
        Optional<FileLocations> optionalLocations = storageService.getDataExportResultFiles(userId, contextId);
        if (!optionalLocations.isPresent()) {
            return Optional.of(DefaultDataExport.builder().withTask(task).build());
        }

        // Get locations
        FileLocations fileLocations = optionalLocations.get();
        List<FileLocation> locations = fileLocations.getLocations();

        // User
        UserService userService = services.getServiceSafe(UserService.class);
        User user = userService.getUser(userId, contextId);

        int total = locations.size();
        ImmutableList.Builder<DataExportResultFile> files = ImmutableList.builderWithExpectedSize(total);
        for (FileLocation fileLocation : locations) {
            DefaultDataExportResultFile resultFile = DefaultDataExportResultFile.builder()
                .withContentType(DataExportUtility.CONTENT_TYPE)
                .withFileName(DataExportUtility.generateFileNameFor("archive", ".zip", fileLocation.getNumber(), total, task.getCreationTime(), user))
                .withNumber(fileLocation.getNumber())
                .withTaskId(task.getId())
                .build();
            files.add(resultFile);
        }

        return Optional.of(DefaultDataExport.builder().withTask(task).withResultFiles(files.build()).withAvailableUntil(new Date(fileLocations.getLastAccessed() + config.getMaxTimeToLiveMillis())).build());
    }

    @Override
    public DataExportDownload getDataExportDownload(int number, int userId, int contextId) throws OXException {
        Optional<DataExportTask> optionalTask = storageService.getDataExportTask(userId, contextId);
        if (!optionalTask.isPresent()) {
            throw DataExportExceptionCode.NO_SUCH_TASK.create(I(userId), I(contextId));
        }

        // Check task's status
        DataExportTask task = optionalTask.get();
        DataExportStatus status = task.getStatus();
        if (!status.isDone()) {
            if (status.isFailed()) {
                throw DataExportExceptionCode.TASK_FAILED.create(I(userId), I(contextId));
            }
            if (status.isAborted()) {
                throw DataExportExceptionCode.TASK_ABORTED.create(I(userId), I(contextId));
            }
            throw DataExportExceptionCode.TASK_NOT_COMPLETED.create(I(userId), I(contextId));
        }

        // Task is done
        Optional<FileLocations> optionalResultFiles = storageService.getDataExportResultFiles(userId, contextId);
        if (!optionalResultFiles.isPresent()) {
            throw DataExportExceptionCode.TASK_NOT_COMPLETED.create(I(userId), I(contextId));
        }

        FileLocations fileLocations = optionalResultFiles.get();
        List<FileLocation> locations = fileLocations.getLocations();
        FileLocation fileLocation = null;
        for (Iterator<FileLocation> it = locations.iterator(); fileLocation == null && it.hasNext(); ) {
            FileLocation loc = it.next();
            if (loc.getNumber() == number) {
                fileLocation = loc;
            }
        }

        if (fileLocation == null) {
            throw DataExportExceptionCode.NO_SUCH_RESULT_FILE.create(I(number), I(userId), I(contextId));
        }

        // User
        UserService userService = services.getServiceSafe(UserService.class);
        User user = userService.getUser(userId, contextId);

        // File name
        String fileName = DataExportUtility.generateFileNameFor("archive", ".zip", number, locations.size(), task.getCreationTime(), user);

        return new FileStorageDataExportDownload(fileLocation, task, fileName);
    }

    @Override
    public List<Module> getAvailableModules(Session session) throws OXException {
        return providerRegistry.getAvailableModules(session);
    }

    @Override
    public boolean removeDataExport(int userId, int contextId) throws OXException {
        Optional<DataExportTask> optionalTask = storageService.getDataExportTask(userId, contextId);
        if (!optionalTask.isPresent()) {
            return false;
        }

        // Check status
        DataExportTask task = optionalTask.get();
        DataExportStatus status = task.getStatus();
        if (!status.isDone() && !status.isFailed()) {
            // Still in progress...
            return false;
        }

        // Delete it...
        storageService.deleteDataExportTask(userId, contextId);
        return true;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when service has been started.
     *
     * @throws OXException If trigger fails
     */
    public void onStartUp() throws OXException {
        planSchedule();
    }

    /**
     * Invoked when service has been stopped.
     */
    public void onStopped() {
        ScheduledTimerTask checkAbortedTimerTask = this.checkAbortedTimerTask;
        if (null != checkAbortedTimerTask) {
            checkAbortedTimerTask.cancel(false);
        }

        cancelAllTimerTasks(true);
        stopProcessingTasks(false);
    }

    /**
     * Gets the appropriate {@link DayOfWeek} instance for given {@link Calendar#DAY_OF_WEEK} value.
     *
     * @param dayOfTheWeek The value for the day of the week
     * @return The appropriate {@link DayOfWeek} instance
     * @throws IllegalArgumentException If given value for the day of the week is invalid
     *
     * @see Calendar#SUNDAY
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     */
    private static DayOfWeek dayOfWeekFor(int dayOfTheWeek) {
        switch (dayOfTheWeek) {
            case Calendar.SUNDAY:
                return DayOfWeek.SUNDAY;
            case Calendar.MONDAY:
                return DayOfWeek.MONDAY;
            case Calendar.TUESDAY:
                return DayOfWeek.TUESDAY;
            case Calendar.WEDNESDAY:
                return DayOfWeek.WEDNESDAY;
            case Calendar.THURSDAY:
                return DayOfWeek.THURSDAY;
            case Calendar.FRIDAY:
                return DayOfWeek.FRIDAY;
            case Calendar.SATURDAY:
                return DayOfWeek.SATURDAY;
            default:
                throw new IllegalArgumentException("Not a valid value for java.util.Calendar.DAY_OF_WEEK: " + dayOfTheWeek);
        }
    }

    @Override
    public void planSchedule() throws OXException {
        if (!config.isActive()) {
            // Not enabled on this node
            LOG.debug("Denied scheduling data export tasks on this node since deactivated per configuration");
            return;
        }

        try {
            cancelAllTimerTasks(false);

            // With initial settings for the current date and time in the system default time zone
            TimerService timerService = services.getServiceSafe(TimerService.class);
            long currentTimeMillis = System.currentTimeMillis();
            int dayOfTheWeek;
            TimeOfTheDay time;
            {
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(currentTimeMillis);
                dayOfTheWeek = now.get(Calendar.DAY_OF_WEEK);
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);
                int second = now.get(Calendar.SECOND);
                time = new TimeOfTheDay(hour, minute, second);
            }

            // Determine next time frame
            Map<DayOfWeek, DayOfWeekTimeRanges> rangesOfTheWeek = config.getRangesOfTheWeek();
            DayOfWeekTimeRanges dayOfWeekTimeRanges = rangesOfTheWeek.get(dayOfWeekFor(dayOfTheWeek));
            if (dayOfWeekTimeRanges != null) {
                // Today... Check if current time is included in any time range; otherwise determine closest
                TimeRange closest = null;
                for (Iterator<TimeRange> it = dayOfWeekTimeRanges.getRanges().iterator(); closest == null && it.hasNext();) {
                    TimeRange timeRange = it.next();
                    if (timeRange.contains(time)) {
                        // Already in a defined time range from today, hence start immediately
                        Calendar tmp = Calendar.getInstance();
                        tmp.setTimeInMillis(currentTimeMillis);
                        tmp.set(Calendar.HOUR_OF_DAY, timeRange.getEnd().getHour());
                        tmp.set(Calendar.MINUTE, timeRange.getEnd().getMinute());
                        tmp.set(Calendar.SECOND, timeRange.getEnd().getSecond());
                        long stopDelay = tmp.getTimeInMillis() - currentTimeMillis;
                        schedule(0, stopDelay, currentTimeMillis, timerService);
                        return;
                    }

                    if (time.compareTo(timeRange.getStart()) < 0) {
                        closest = timeRange;
                    }
                }

                if (closest != null) {
                    // Schedule for closest time range today
                    Calendar tmp = Calendar.getInstance();
                    tmp.setTimeInMillis(currentTimeMillis);
                    tmp.set(Calendar.HOUR_OF_DAY, closest.getStart().getHour());
                    tmp.set(Calendar.MINUTE, closest.getStart().getMinute());
                    tmp.set(Calendar.SECOND, closest.getStart().getSecond());
                    long startDelay = tmp.getTimeInMillis() - currentTimeMillis;

                    tmp.set(Calendar.HOUR_OF_DAY, closest.getEnd().getHour());
                    tmp.set(Calendar.MINUTE, closest.getEnd().getMinute());
                    tmp.set(Calendar.SECOND, closest.getEnd().getSecond());
                    long stopDelay = tmp.getTimeInMillis() - currentTimeMillis;

                    schedule(startDelay, stopDelay, currentTimeMillis, timerService);
                    return;
                }

                dayOfWeekTimeRanges = null;
            }

            // Find follow-up day's time range
            int dayDiffer = 0;
            while (dayOfWeekTimeRanges == null) {
                dayOfTheWeek = dayOfTheWeek + 1;
                dayDiffer++;
                if (dayOfTheWeek > Calendar.SATURDAY) {
                    dayOfTheWeek = Calendar.SUNDAY;
                }
                dayOfWeekTimeRanges = rangesOfTheWeek.get(dayOfWeekFor(dayOfTheWeek));
            }

            // Schedule for first time range of follow-up day
            TimeRange firstOnFollowUpDay = dayOfWeekTimeRanges.getRanges().get(0);

            Calendar tmp = Calendar.getInstance();
            tmp.setTimeInMillis(currentTimeMillis);
            tmp.add(Calendar.DAY_OF_YEAR, dayDiffer);
            tmp.set(Calendar.HOUR_OF_DAY, firstOnFollowUpDay.getStart().getHour());
            tmp.set(Calendar.MINUTE, firstOnFollowUpDay.getStart().getMinute());
            tmp.set(Calendar.SECOND, firstOnFollowUpDay.getStart().getSecond());
            long startDelay = tmp.getTimeInMillis() - currentTimeMillis;

            tmp.set(Calendar.HOUR_OF_DAY, firstOnFollowUpDay.getEnd().getHour());
            tmp.set(Calendar.MINUTE, firstOnFollowUpDay.getEnd().getMinute());
            tmp.set(Calendar.SECOND, firstOnFollowUpDay.getEnd().getSecond());
            long stopDelay = tmp.getTimeInMillis() - currentTimeMillis;

            schedule(startDelay, stopDelay, currentTimeMillis, timerService);
        } catch (RuntimeException e) {
            throw DataExportExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void schedule(long startDelay, long stopDelay, long currentTimeMillis, TimerService timerService) {
        if (startDelay < 0) {
            throw new IllegalArgumentException("Start delay must not be less than 0 (zero): " + startDelay);
        }
        if (stopDelay < startDelay) {
            throw new IllegalArgumentException("Stop delay (" + startDelay + ") must be greater than start delay (" + stopDelay + ")");
        }
        final long endTimeMillis = currentTimeMillis + stopDelay;
        Runnable startTask = new Runnable() {

            @Override
            public void run() {
                try {
                    if (System.currentTimeMillis() < endTimeMillis) {
                        startProcessingTasks();
                    }
                } catch (Exception e) {
                    LOG.error("Failed to start data export tasks", e);
                }
            }
        };
        this.periodicStartTask.set(timerService.scheduleWithFixedDelay(startTask, startDelay, config.getCheckForTasksFrequency(), TimeUnit.MILLISECONDS));

        Runnable stopTask = new Runnable() {

            @Override
            public void run() {
                try {
                    stopProcessingTasks(true);
                } catch (Exception e) {
                    LOG.error("Failed to stop data export tasks", e);
                }
            }
        };
        this.stopTask.set(timerService.schedule(stopTask, stopDelay, TimeUnit.MILLISECONDS));

        Runnable nextScheduleRun = new Runnable() {

            @Override
            public void run() {
                try {
                    planSchedule();
                } catch (Exception e) {
                    LOG.error("Failed to schedule data export tasks", e);
                }
            }
        };
        this.nextRunTask.set(timerService.schedule(nextScheduleRun, stopDelay + 60000L, TimeUnit.MILLISECONDS));

        LOG.debug("Scheduled execution of data export tasks on this node");
    }

    private void cancelAllTimerTasks(boolean mayInterruptIfRunning) {
        cancelTimerTasks(periodicStartTask, mayInterruptIfRunning, false);
        cancelTimerTasks(stopTask, mayInterruptIfRunning, false);
        cancelTimerTasks(nextRunTask, mayInterruptIfRunning, false);
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (timerService != null) {
            timerService.purge();
        }
    }

    private void cancelTimerTasks(AtomicReference<ScheduledTimerTask> timerTaskReference, boolean mayInterruptIfRunning, boolean purge) {
        ScheduledTimerTask timerTask = timerTaskReference.get();
        if (timerTask != null) {
            timerTaskReference.set(null);
            timerTask.cancel(mayInterruptIfRunning);
        }

        if (purge) {
            TimerService timerService = services.getOptionalService(TimerService.class);
            if (timerService != null) {
                timerService.purge();
            }
        }
    }

    synchronized void startProcessingTasks() throws OXException {
        // Grab thread pool service
        ThreadPoolService threadPool = services.getServiceSafe(ThreadPoolService.class);

        // Obtain and check executions mapping
        final Map<Future<Void>, DataExportTaskExecution> executions = this.executions;
        for (Iterator<Entry<Future<Void>, DataExportTaskExecution>> it = executions.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Future<Void>, DataExportTaskExecution> executionEntry = it.next();
            DataExportTaskExecution execution = executionEntry.getValue();
            if (execution.isInvalid()) {
                it.remove();
                execution.stop();
                executionEntry.getKey().cancel(true);
            }
        }

        // Start new executions as needed
        int count = 1;
        while (executions.size() < config.getNumberOfConcurrentTasks()) {
            Optional<DataExportJob> dataExportJob = storageService.getNextDataExportJob();
            if (!dataExportJob.isPresent()) {
                // No pending/paused/expired tasks available
                LOG.debug("Currently there are no data export tasks to execute");
                return;
            }

            // Get job
            DataExportJob job = dataExportJob.get();

            // Artificial delay
            long nanosToWait = TimeUnit.NANOSECONDS.convert((count++ * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);

            // Check if a diagnostics report is supposed to be compiled for task-associated user
            boolean addDiagnosticsReport = isAddDiagnosticsReport(job);

            // Some variables for clean-up
            DataExportTaskExecution execution = null;
            Future<Void> future = null;
            boolean openedForProcessing = false;
            try {
                // Initialize execution & submit it to thread pool
                execution = new DataExportTaskExecution(job, addDiagnosticsReport, config, storageService, providerRegistry, services);
                future = threadPool.submit(execution);

                // Store execution in map. Then open it for being processed while registering a clean-up task that ensures execution is removed from map when finished
                executions.put(future, execution);
                execution.allowProcessing(new AllowProcessingRunnable(future, executions));
                openedForProcessing = true;
            } finally {
                if (!openedForProcessing) {
                    if (execution != null) {
                        execution.stop();
                    }

                    if (future != null) {
                        executions.remove(future);
                        future.cancel(true);
                    }
                }
            }
        }
    }

    private boolean isAddDiagnosticsReport(DataExportJob job) throws OXException {
        boolean addDiagnosticsReport = false;
        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (viewFactory != null) {
            DataExportTask task = job.getDataExportTask();
            ConfigView view = viewFactory.getView(task.getUserId(), job.getDataExportTask().getContextId());
            addDiagnosticsReport = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.gdpr.dataexport.addDiagnosticsReport", addDiagnosticsReport, view);
        }
        return addDiagnosticsReport;
    }

    synchronized void stopProcessingTasks(boolean cancelStartStopTimerTasks) {
        if (cancelStartStopTimerTasks) {
            cancelTimerTasks(periodicStartTask, false, false);
            cancelTimerTasks(stopTask, false, false);
            TimerService timerService = services.getOptionalService(TimerService.class);
            if (timerService != null) {
                timerService.purge();
            }
        }

        for (Map.Entry<Future<Void>, DataExportTaskExecution> executionEntry : executions.entrySet()) {
            executionEntry.getValue().stop();
            executionEntry.getKey().cancel(true);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static boolean equals(DataExportTask task1, DataExportTask task2) {
        if (task1 == null) {
            return task2 == null ? true : false;
        }
        if (task2 == null) {
            return false;
        }
        if (task1.getContextId() != task2.getContextId()) {
            return false;
        }
        if (task1.getUserId() != task2.getUserId()) {
            return false;
        }
        if (!task1.getId().equals(task2.getId())) {
            return false;
        }
        return true;
    }

    private static final class AllowProcessingRunnable implements Runnable {

        private final Future<Void> futureToRemove;
        private final Map<Future<Void>, DataExportTaskExecution> executions;

        AllowProcessingRunnable(Future<Void> futureToRemove, Map<Future<Void>, DataExportTaskExecution> executions) {
            super();
            this.futureToRemove = futureToRemove;
            this.executions = executions;
        }

        @Override
        public void run() {
            executions.remove(futureToRemove);
        }
    }

}
