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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.gdpr.dataexport.impl.DataExportUtility.deleteQuietly;
import static com.openexchange.gdpr.dataexport.impl.DataExportUtility.stringFor;
import static com.openexchange.gdpr.dataexport.impl.notification.DataExportNotificationSender.sendNotificationAndSetMarker;
import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportAbortedException;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportProvider;
import com.openexchange.gdpr.dataexport.DataExportProviderRegistry;
import com.openexchange.gdpr.dataexport.DataExportDiagnosticsReport;
import com.openexchange.gdpr.dataexport.DataExportSavepoint;
import com.openexchange.gdpr.dataexport.DataExportStatus;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportJob;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.gdpr.dataexport.ExportResult;
import com.openexchange.gdpr.dataexport.Message;
import com.openexchange.gdpr.dataexport.PauseResult;
import com.openexchange.gdpr.dataexport.impl.notification.Reason;
import com.openexchange.gdpr.dataexport.impl.utils.ChunkedZippedOutputStream;
import com.openexchange.java.ISO8601Utils;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link DataExportTaskExecution} - Continues executing data export tasks as long as
 * {@link DataExportStorageService#getNextDataExportJob() getNextDataExportTask()} returns a task to work on.
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
 * Note: Don't forget to invoke {@link #allowProcessing(Runnable)}
 * </div>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportTaskExecution extends AbstractTask<Void> {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportTaskExecution.class);

    private final DataExportProviderRegistry providerRegistry;
    private final DataExportStorageService storageService;
    private final long expirationTimeMillis;
    private final long maxProcessingTimeMillis;
    private final long maxTimeToLiveMillis;
    private final AtomicReference<JobAndReport> currentJob;
    private final AtomicReference<ProviderInfo> currentProviderInfo;
    private final ProcessingThreadReference processingThread;
    private final AtomicReference<ScheduledTimerTask> currentToucher;
    private final AtomicReference<ScheduledTimerTask> currentStopper;
    private final Lock stopLock;
    private final ServiceLookup services;
    private final AtomicReference<Runnable> cleanUpTask;
    private final CountDownLatch latch;
    private final AtomicLong startTime;
    private final boolean addDiagnosticsReport;

    /**
     * Initializes a new {@link DataExportTaskExecution}.
     *
     * @param initialJob The initial job or <code>null</code>
     * @param addDiagnosticsReport code>true</code> to add diagnostics report; otherwise <code>false</code>
     * @param config The configuration
     * @param storageService The storage service to use
     * @param providerRegistry The listing of available data export providers
     * @param services The services
     */
    public DataExportTaskExecution(DataExportJob initialJob, boolean addDiagnosticsReport, DataExportConfig config, DataExportStorageService storageService, DataExportProviderRegistry providerRegistry, ServiceLookup services) {
        super();
        this.addDiagnosticsReport = addDiagnosticsReport;
        this.expirationTimeMillis = config.getExpirationTimeMillis();
        this.maxProcessingTimeMillis = config.getMaxProcessingTimeMillis();
        this.maxTimeToLiveMillis = config.getMaxTimeToLiveMillis();
        currentJob = new AtomicReference<>(new JobAndReport(initialJob, null));
        currentToucher = new AtomicReference<>(null);
        currentStopper = new AtomicReference<>(null);
        stopLock = new ReentrantLock();
        this.storageService = storageService;
        this.providerRegistry = providerRegistry;
        this.services = services;
        currentProviderInfo = new AtomicReference<>(null);
        processingThread = new ProcessingThreadReference();
        cleanUpTask = new AtomicReference<>(null);
        latch = new CountDownLatch(1);
        startTime = new AtomicLong(0L);
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix(DataExportTaskExecution.class.getSimpleName());
    }

    /**
     * Gets the task that is currently executed.
     *
     * @return The optional task
     */
    public Optional<DataExportTask> getCurrentTask() {
        JobAndReport jobAndReport = currentJob.get();
        return jobAndReport == null ? Optional.empty() : Optional.of(jobAndReport.job.getDataExportTask());
    }

    /**
     * Allows processing for this execution.
     */
    public void allowProcessing(Runnable cleanUpTask) {
        this.cleanUpTask.set(cleanUpTask);
        latch.countDown();
    }

    /**
     * Gets the processing time in milliseconds.
     *
     * @return The processing time in milliseconds or <code>-1</code> if not yet started
     */
    public long getProcessingTimeMillis() {
        long startTimeMillis = startTime.get();
        return startTimeMillis <= 0 ? -1L : System.currentTimeMillis() - startTimeMillis;
    }

    /**
     * Checks whether this execution is considered as being valid.
     * <p>
     * Valid means, either processing is ahead or it is currently processed.
     *
     * @return <code>true</code> if processed; otherwise <code>false</code>
     */
    public boolean isValid() {
        return processingThread.isPendingOrRunning();
    }

    /**
     * Checks whether this execution is considered as being invalid.
     * <p>
     * Invalid means, processing has started, but there is no more a processing thread associated with it.
     *
     * @return <code>true</code> if <b>not</b> processed; otherwise <code>false</code>
     */
    public boolean isInvalid() {
        return !isValid();
    }

    @Override
    public Void call() throws OXException {
        this.processingThread.setCurrentThread(Thread.currentThread());
        try {
            process();
        } finally {
            this.processingThread.unsetCurrentThread();
        }
        return null;
    }

    /**
     * Processes this data export execution that is querying and handling pending/paused/expired data export tasks.
     *
     * @throws OXException If processing fails
     */
    private void process() throws OXException {
        try {
            // Check if thread is interrupted
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted prior to processing any task");
            }

            // Await until processing is allowed; see allowProcessing()
            latch.await();
            startTime.set(System.currentTimeMillis());

            Optional<DataExportJob> optionalJob;
            {
                JobAndReport initialJob = currentJob.getAndSet(null);
                optionalJob = initialJob == null ? storageService.getNextDataExportJob() : Optional.of(initialJob.job);
            }

            if (!optionalJob.isPresent()) {
                // No further pending/paused/expired jobs available
                return;
            }

            do {
                // Get the job to process
                DataExportJob job = optionalJob.get();

                // Check if thread is interrupted
                if (Thread.interrupted()) {
                    StringBuilder msg = new StringBuilder("Interrupted while processing task \"");
                    msg.append(UUIDs.getUnformattedString(job.getDataExportTask().getId())).append('"');
                    throw new InterruptedException(msg.toString());
                }

                // Handle current job
                handleJob(job);

                // Acquire next job... after some artificial delay
                long nanosToWait = TimeUnit.NANOSECONDS.convert(1000 + ((long) (Math.random() * 5000)), TimeUnit.MILLISECONDS);
                LockSupport.parkNanos(nanosToWait);
                optionalJob = storageService.getNextDataExportJob();
            } while (optionalJob.isPresent());

            // No further pending/paused/expired jobs available
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            LOG.info("Data export execution interrupted", e);
        } catch (Throwable t) {
            LOG.warn("Data export execution failed", t);
            throw t;
        } finally {
            Runnable cleanUpTask = this.cleanUpTask.get();
            if (cleanUpTask != null) {
                try {
                    cleanUpTask.run();
                } catch (Exception e) {
                    LOG.warn("Failed to perform clean-up for data export execution", e);
                }
            }
        }
    }

    /**
     * Handles specified data export task job.
     *
     * @param job The job to handle
     */
    private void handleJob(DataExportJob job) {
        DataExportTask task = job.getDataExportTask();
        int userId = task.getUserId();
        int contextId = task.getContextId();
        UUID taskId = task.getId();
        Optional<DataExportDiagnosticsReport> optionalReport = addDiagnosticsReport ? Optional.of(new DataExportDiagnosticsReport()) : Optional.empty();

        boolean error = true;
        currentJob.set(new JobAndReport(job, optionalReport));
        try {
            // Check task status (from loaded instance and live)
            if ((task.getStatus().isAborted() || storageService.getDataExportStatus(userId, contextId).orElse(null) == DataExportStatus.ABORTED)) {
                // Task has been aborted. Delete it...
                storageService.deleteDataExportTask(userId, contextId);

                // Trigger notification for user that data export has been aborted
                sendNotificationAndSetMarker(Reason.ABORTED, task.getCreationTime(), null, null, taskId, userId, contextId, false, services);
                return;
            }

            // User's locale
            Locale locale = getUserLocale(userId, contextId);

            // Determine the file storage to use
            FileStorage fileStorage = DataExportUtility.getFileStorageFor(task);

            // Fetch first work item
            Optional<DataExportWorkItem> optionalItem = job.getNextDataExportWorkItem();

            // Initialize toucher and stopper (if max. processing time is given)
            initTimerTasks(taskId, userId, contextId);

            LOG.info("Starting data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId));

            // Process it (if present) and fetch remaining ones subsequently
            DataExportStatus currentStatus = DataExportStatus.RUNNING;
            boolean keepGoing = true;
            if (optionalItem.isPresent()) {
                boolean stillRunningAndHasNext = false;
                do {
                    try {
                        keepGoing = handleWorkItem(optionalItem.get(), task, locale, fileStorage, optionalReport);
                    } catch (DataExportAbortedException e) {
                        // Aborted...
                        keepGoing = false;
                        currentStatus = DataExportStatus.ABORTED;
                    }

                    if (keepGoing) {
                        currentStatus = storageService.getDataExportStatus(userId, contextId).orElse(null);
                        stillRunningAndHasNext = ((currentStatus == DataExportStatus.RUNNING) && (optionalItem = job.getNextDataExportWorkItem()).isPresent());
                    }
                } while (keepGoing && stillRunningAndHasNext);
            }

            // Check status
            if (currentStatus == null || currentStatus == DataExportStatus.ABORTED) {
                // Task has been aborted. Delete it...
                storageService.deleteDataExportTask(userId, contextId);

                // Trigger notification for user that data export has been aborted
                sendNotificationAndSetMarker(Reason.ABORTED, task.getCreationTime(), null, null, taskId, userId, contextId, false, services);
                LOG.info("Data export task {} of user {} in context {} aborted", stringFor(taskId), I(userId), I(contextId));
            } else if (currentStatus == DataExportStatus.RUNNING) {
                if (keepGoing) {
                    // Consider as completed. Grab up-to-date version from storage
                    DataExportTask reloaded = storageService.getDataExportTask(userId, contextId).orElse(null);
                    if (reloaded != null) {
                        // Finished because no more work items left and therefore task is completed. Generate resulting archive(s)
                        ChunkedZippedOutputStream zipOut = new ChunkedZippedOutputStream(reloaded, fileStorage, storageService, services);
                        try {
                            generateResultFilesAndMarkAsDone(reloaded, fileStorage, zipOut, optionalReport, locale);
                        } finally {
                            zipOut.close();
                        }

                        // Drop work items' files to free space
                        try {
                            storageService.dropIntermediateFiles(taskId, contextId);
                        } catch (Exception e) {
                            LOG.warn("Failed to drop intermediate files from data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                        }

                        // Determine expiration date
                        Date expiryDate;
                        try {
                            Optional<Date> lastAccessedTimeStamp = storageService.getLastAccessedTimeStamp(userId, contextId);
                            expiryDate = new Date(lastAccessedTimeStamp.get().getTime() + maxTimeToLiveMillis);
                        } catch (Exception e) {
                            expiryDate = new Date(System.currentTimeMillis() + maxTimeToLiveMillis);
                            LOG.warn("Failed to query last-accessed time stamp from data export task {} of user {} in context {}. Assuming \"{}\" as expiration date.", stringFor(taskId), I(userId), I(contextId), ISO8601Utils.format(expiryDate), e);
                        }

                        // Trigger notification for user that data export is available
                        sendNotificationAndSetMarker(Reason.SUCCESS, task.getCreationTime(), expiryDate, task.getArguments().getHostInfo(), taskId, userId, contextId, true, services);
                        LOG.info("Data export task {} of user {} in context {} completed", stringFor(taskId), I(userId), I(contextId));
                    }
                } else {
                    // Processing work items has been stopped

                    // TODO: Anything to do here?
                }
            }

            error = false;
        } catch (AlreadyLoggedError e) {
            // Re-throw error
            throw e.error;
        } catch (Error e) {
            LOG.warn("Data export task {} of user {} in context {} failed fatally", stringFor(taskId), I(userId), I(contextId), e);
            // Re-throw error
            throw e;
        } catch (AlreadyLoggedException t) {
            // Just caught here to avoid duplicate logging. Nothing to do...
        } catch (Throwable t) {
            LOG.warn("Data export task {} of user {} in context {} failed", stringFor(taskId), I(userId), I(contextId), t);
        } finally {
            cancelTimerTasks(taskId, userId, contextId, true);
            currentJob.set(null);
            if (error) {
                try {
                    storageService.markFailed(taskId, userId, contextId);
                } catch (Exception e) {
                    LOG.warn("Cannot set failed marker for data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                }

                // Trigger notification for user that data export failed
                try {
                    sendNotificationAndSetMarker(Reason.FAILED, task.getCreationTime(), null, null, taskId, userId, contextId, true, services);
                } catch (Exception e) {
                    LOG.warn("Cannot set notification-sent marker for data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                }
            }
        }
    }

    private boolean handleWorkItem(DataExportWorkItem item, DataExportTask task, Locale locale, FileStorage fileStorage, Optional<DataExportDiagnosticsReport> optionalReport) throws OXException, DataExportAbortedException {
        int userId = task.getUserId();
        int contextId = task.getContextId();
        UUID taskId = task.getId();
        String moduleId = item.getModuleId();
        boolean keepGoing = true;

        // Determine fitting provider with highest ranking
        Optional<DataExportProvider> optionalProvider = providerRegistry.getHighestRankedProviderFor(moduleId);
        if (!optionalProvider.isPresent()) {
            // No such provider
            OXException oxe = DataExportExceptionCode.NO_SUCH_PROVIDER.create(moduleId);
            storageService.markWorkItemFailed(Optional.ofNullable(toJson(oxe)), taskId, moduleId, userId, contextId);
            throw oxe;
        }
        DataExportProvider providerToUse = optionalProvider.get();

        // Get provider's path prefix
        String pathPrefix = providerToUse.getPathPrefix(locale);

        // Create the data sink
        String optPrevFileStorageLocation = item.getFileStorageLocation();
        DataExportSinkImpl sink = new DataExportSinkImpl(task, moduleId, pathPrefix, Optional.ofNullable(optPrevFileStorageLocation), fileStorage, storageService, optionalReport, services);

        // Grab latest save-point (if any)
        Optional<JSONObject> optionalSavepoint;
        try {
            DataExportSavepoint savePoint = storageService.getSavePoint(taskId, moduleId, userId, contextId);
            optionalSavepoint = savePoint.getSavepoint();
            if (optionalReport.isPresent() && savePoint.getReport().isPresent()) {
                optionalReport.get().addAll(savePoint.getReport().get());
            }
        } catch (Exception e) {
            storageService.markWorkItemFailed(Optional.ofNullable(toJson(e)), taskId, moduleId, userId, contextId);
            return keepGoing;
        }

        // Generate a unique processing identifier for current provider's export
        UUID processingId = UUID.randomUUID();

        // Trigger provider export
        Throwable failure = null;
        currentProviderInfo.set(new ProviderInfo(processingId, providerToUse, sink, fileStorage));
        try {
            // Initiate/continue data export
            ExportResult exportResult;
            try {
                LOG.info("{} \"{}\" work item for data export task {} of user {} in context {}", optionalSavepoint.isPresent() ? "Resuming" : "Starting", moduleId, stringFor(taskId), I(userId), I(contextId));
                exportResult = providerToUse.export(processingId, sink, optionalSavepoint, task, locale);
            } catch (InterruptedException e) {
                // Export interrupted
                Thread.currentThread().interrupt();
                LOG.warn("Interrupted \"{}\" work item for data export task {} of user {} in context {}", moduleId, stringFor(taskId), I(userId), I(contextId), e);
                exportResult = ExportResult.interrupted();
            } finally {
                LogProperties.removeLogProperties();
            }

            if (exportResult.isCompleted()) {
                // Export completed successfully
                LOG.info("Completed \"{}\" work item for data export task {} of user {} in context {}", moduleId, stringFor(taskId), I(userId), I(contextId));

                // Await file storage location
                String fileStorageLocation = sink.finish().get();

                boolean err = true;
                try {
                    // Mark work item as done
                    if (sink.wasExportCalled() || optPrevFileStorageLocation != null) {
                        storageService.markWorkItemDone(fileStorageLocation, taskId, moduleId, userId, contextId);
                    } else {
                        // Sink has not been initialized. Consequently, nothing was written to it
                        if (fileStorageLocation != null) {
                            fileStorage.deleteFile(fileStorageLocation);
                        }
                        storageService.markWorkItemDone(null, taskId, moduleId, userId, contextId);
                        if (optionalReport.isPresent()) {
                            optionalReport.get().add(Message.builder().appendToMessage("Data export provider \"").appendToMessage(moduleId).appendToMessage("\" exported no data.").withModuleId(moduleId).withTimeStamp(new Date()).build());
                        }
                    }
                    err = false;
                } finally {
                    if (err) {
                        deleteQuietly(fileStorageLocation, fileStorage);
                    }
                }
            } else {
                keepGoing = false;
                if (exportResult.isInterrupted()) {
                    // Manually paused; necessary actions take place in "stop(boolean)" method
                    LOG.info("Interrupted \"{}\" work item for data export task {} of user {} in context {}", moduleId, stringFor(taskId), I(userId), I(contextId));
                } else if (exportResult.isIncomplete()) {
                    // Incomplete result (paused)
                    Optional<Exception> incompleteReason = exportResult.getIncompleteReason();
                    if (incompleteReason.isPresent()) {
                        if (LOG.isDebugEnabled()) {
                            Exception pauseReason = incompleteReason.get();
                            LOG.info("Pausing \"{}\" work item for data export task {} of user {} in context {} due to (temporary) exception", moduleId, stringFor(taskId), I(userId), I(contextId), pauseReason);
                        } else {
                            LOG.info("Pausing \"{}\" work item for data export task {} of user {} in context {} due to (temporary) exception ``{}\u00b4\u00b4", moduleId, stringFor(taskId), I(userId), I(contextId), incompleteReason.get().getMessage());
                        }
                    } else {
                        LOG.info("Pausing \"{}\" work item for data export task {} of user {} in context {}", moduleId, stringFor(taskId), I(userId), I(contextId));
                    }
                    DataExportSavepoint savePoint = DataExportSavepoint.builder().withSavepoint(exportResult.getSavePoint().orElse(null)).withReport(optionalReport.orElse(null)).build();
                    storageService.setSavePoint(taskId, moduleId, savePoint, userId, contextId);
                    storageService.markPaused(taskId, userId, contextId);
                    Optional<String> fileStorageLocation = sink.finish();
                    if (fileStorageLocation.isPresent()) {
                        boolean err = true;
                        try {
                            storageService.markWorkItemPaused(fileStorageLocation.get(), taskId, moduleId, userId, contextId);
                            err = false;
                        } finally {
                            if (err) {
                                deleteQuietly(fileStorageLocation.get(), fileStorage);
                            }
                        }
                    }
                } else if (exportResult.isAborted()) {
                    // Manually aborted
                    LOG.info("Aborted \"{}\" work item for data export task {} of user {} in context {}", moduleId, stringFor(taskId), I(userId), I(contextId));
                    throw new DataExportAbortedException("\"" + moduleId + "\" data export task " + stringFor(taskId) + " of user " + userId + " in context " + contextId + " aborted");
                } else {
                    throw new IllegalArgumentException("Unknown export value: " + exportResult.getValue());
                }
            }
        } catch (DataExportAbortedException e) {
            // Just re-throw
            throw e;
        } catch (Exception e) {
            failure = e;
            LOG.warn("\"{}\" work item for data export task {} of user {} in context {} failed", moduleId, stringFor(taskId), I(userId), I(contextId), failure);
        } catch (Error e) {
            failure = e;
            LOG.warn("\"{}\" work item for data export task {} of user {} in context {} failed fatally", moduleId, stringFor(taskId), I(userId), I(contextId), failure);
            // Re-throw error
            throw new AlreadyLoggedError(e);
        } catch (Throwable t) {
            failure = t;
            LOG.warn("\"{}\" work item for data export task {} of user {} in context {} failed", moduleId, stringFor(taskId), I(userId), I(contextId), failure);
        } finally {
            currentProviderInfo.set(null);
            if (failure != null) {
                // An error occurred
                sink.revoke();
                storageService.markWorkItemFailed(Optional.ofNullable(toJson(failure)), taskId, moduleId, userId, contextId);
            }
        }
        return keepGoing;
    }

    private void generateResultFilesAndMarkAsDone(DataExportTask task, FileStorage fileStorage, ChunkedZippedOutputStream zipOut, Optional<DataExportDiagnosticsReport> optionalReport, Locale locale) throws AlreadyLoggedError, OXException {
        boolean error = true;
        try {
            // Add diagnostics report
            if (optionalReport.isPresent() && !optionalReport.get().isEmpty()) {
                zipOut.addDiagnostics(optionalReport.get(), locale);
            }

            // Add work items' artifacts
            for (DataExportWorkItem wo : task.getWorkItems()) {
                String fileStorageLocation = wo.getFileStorageLocation();
                if (fileStorageLocation != null) {
                    InputStream in = null;
                    ZipArchiveInputStream zipIn = null;
                    try {
                        in = fileStorage.getFile(fileStorageLocation);
                        zipIn = new ZipArchiveInputStream(in, "UTF-8");
                        zipOut.addEntriesFrom(zipIn);
                    } finally {
                        Streams.close(zipIn, in);
                    }
                }
            }

            // Mark task as done
            storageService.markDone(task.getId(), task.getUserId(), task.getContextId());

            error = false;
        } catch (Error e) {
            LOG.warn("Data export task {} of user {} in context {} failed fatally", stringFor(task.getId()), I(task.getUserId()), I(task.getContextId()), e);
            // Re-throw error
            throw new AlreadyLoggedError(e);
        } catch (Throwable t) {
            LOG.warn("Failed generating result files for data export task {} of user {} in context {}", stringFor(task.getId()), I(task.getUserId()), I(task.getContextId()), t);
            throw new AlreadyLoggedException(t);
        } finally {
            if (error) {
                zipOut.cleanUp();
            }
        }
    }

    /**
     * Generates the JSON representation for given failure.
     *
     * @param failure The failure to convert to JSON
     * @return The JSON representation
     */
    private static JSONObject toJson(Throwable failure) {
        try {
            OXException oxe = failure instanceof OXException ? ((OXException) failure) : DataExportExceptionCode.UNEXPECTED_ERROR.create(failure, failure.getMessage());
            JSONObject jException = new JSONObject(10);
            ResponseWriter.addException(jException, oxe);
            jException.remove(ResponseFields.ERROR_STACK);
            return jException;
        } catch (JSONException e) {
            // Conversion to JSON failed
            LOG.info("Failed to convert exceotion to JSON", e);
        }
        return null;
    }

    /**
     * Stops this execution.
     */
    public void stop() {
        stop(false);
    }

    /**
     * Stops this execution.
     *
     * @param internallyInvoked Whether this method has been invoked internally or externally
     */
    void stop(boolean internallyInvoked) {
        // Acquire stop lock
        stopLock.lock();
        try {
            // Grab currently executed job (if any)
            JobAndReport currentJob = this.currentJob.get();
            if (currentJob != null) {
                DataExportTask task = currentJob.job.getDataExportTask();
                int userId = task.getUserId();
                int contextId = task.getContextId();
                UUID taskId = task.getId();

                // Grab the provider that is currently exporting
                boolean paused = false;
                {
                    ProviderInfo providerInfo = this.currentProviderInfo.get();
                    if (providerInfo != null) {
                        // Try to pause provider
                        DataExportProvider provider = providerInfo.provider;
                        String moduleId = provider.getId();
                        DataExportSinkImpl sink = providerInfo.sink;
                        try {
                            PauseResult pauseResult = provider.pause(providerInfo.processingId, sink, task);
                            if (pauseResult.isPaused()) {
                                paused = true;
                                DataExportSavepoint savePoint = DataExportSavepoint.builder().withSavepoint(pauseResult.getSavePoint().orElse(null)).withReport(currentJob.optionalReport.orElse(null)).build();
                                storageService.setSavePoint(taskId, moduleId, savePoint, userId, contextId);
                                storageService.markPaused(taskId, userId, contextId);
                                Optional<String> fileStorageLocation = sink.finish();
                                if (fileStorageLocation.isPresent()) {
                                    boolean err = true;
                                    try {
                                        storageService.markWorkItemPaused(fileStorageLocation.get(), taskId, moduleId, userId, contextId);
                                        err = false;
                                    } finally {
                                        if (err) {
                                            deleteQuietly(fileStorageLocation.get(), providerInfo.fileStorage);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.info("Failed to pause provider {}", moduleId, e);
                        }
                    }
                }

                if (paused) {
                    cancelTimerTasks(taskId, userId, contextId, !internallyInvoked);
                }
            }
        } finally {
            stopLock.unlock();
        }
    }

    /**
     * Initializes timer tasks for stopping and touching.
     *
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    private void initTimerTasks(UUID taskId, int userId, int contextId) throws OXException {
        DataExportStorageService storageService = this.storageService;
        TimerService timerService = services.getServiceSafe(TimerService.class);
        Runnable toucher = new Runnable() {

            @Override
            public void run() {
                try {
                    Optional<DataExportStatus> optionalStatus = storageService.getDataExportStatus(userId, contextId);
                    if (optionalStatus.isPresent() && !optionalStatus.get().isTerminated()) {
                        storageService.touch(userId, contextId);
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to touch time stamp of data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                }
            }
        };
        long delayMillis = expirationTimeMillis >> 1;
        currentToucher.set(timerService.scheduleWithFixedDelay(toucher, delayMillis, delayMillis));

        // Max. processing time
        if (maxProcessingTimeMillis > 0) {
            // Create one-shot timer task for stopping
            Runnable stopper = new Runnable() {

                @Override
                public void run() {
                    try {
                        stop(true);
                    } catch (Exception e) {
                        LOG.warn("Failed to stop execution of data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                    }
                }
            };
            this.currentStopper.set(timerService.schedule(stopper, maxProcessingTimeMillis));
        }
    }

    /**
     * Cancels timer tasks for stopping and touching.
     *
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param cancelStopperTask Whether stopper task needs to be canceled
     */
    private void cancelTimerTasks(UUID taskId, int userId, int contextId, boolean cancelStopperTask) {
        // Drop stopper timer task (as we are obviously stopping here)
        boolean somethingStopped = false;
        if (cancelStopperTask) {
            ScheduledTimerTask stopperTask = currentStopper.getAndSet(null);
            if (stopperTask != null) {
                try {
                    stopperTask.cancel();
                    somethingStopped = true;
                } catch (Exception e) {
                    LOG.warn("Failed to cancel one-time stopper task for data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
                }
            }
        }

        // Drop toucher timer task
        ScheduledTimerTask timerTask = currentToucher.getAndSet(null);
        if (timerTask != null) {
            try {
                timerTask.cancel();
                somethingStopped = true;
            } catch (Exception e) {
                LOG.warn("Failed to cancel periodic toucher for data export task {} of user {} in context {}", stringFor(taskId), I(userId), I(contextId), e);
            }
        }

        if (somethingStopped) {
            purgeCanceledTimerTasks();
        }
    }

    private void purgeCanceledTimerTasks() {
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (timerService != null) {
            timerService.purge();
        }
    }

    private Locale getUserLocale(int userId, int contextId) throws OXException {
        UserService userService = services.getServiceSafe(UserService.class);
        return userService.getUser(userId, contextId).getLocale();
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private static class ProviderInfo {

        final DataExportProvider provider;
        final UUID processingId;
        final DataExportSinkImpl sink;
        final FileStorage fileStorage;

        ProviderInfo(UUID processingId, DataExportProvider provider, DataExportSinkImpl sink, FileStorage fileStorage) {
            super();
            this.processingId = processingId;
            this.provider = provider;
            this.sink = sink;
            this.fileStorage = fileStorage;
        }
    }

    private static class JobAndReport {

        final DataExportJob job;
        final Optional<DataExportDiagnosticsReport> optionalReport;

        JobAndReport(DataExportJob job, Optional<DataExportDiagnosticsReport> optionalReport) {
            super();
            this.job = job;
            this.optionalReport = optionalReport;
        }
    }

    private static class AlreadyLoggedError extends Error {

        private static final long serialVersionUID = 6838393800639076591L;

        final Error error;

        /**
         * Initializes a new {@link AlreadyLoggedError}.
         *
         * @param cause The causing error
         */
        AlreadyLoggedError(Error cause) {
            super();
            this.error = cause;
        }
    }

    private static class AlreadyLoggedException extends RuntimeException {

        private static final long serialVersionUID = 7438393800639076591L;

        /**
         * Initializes a new {@link AlreadyLoggedException}.
         *
         * @param reason The reason
         */
        AlreadyLoggedException(Throwable reason) {
            super(reason);
        }
    }

    private static class ProcessingThreadReference {

        private boolean started;
        private Thread currentThread;

        ProcessingThreadReference() {
            super();
            started = false;
        }

        synchronized boolean isPendingOrRunning() {
            return !started || currentThread != null;
        }

        synchronized void setCurrentThread(Thread currentThread) {
            if (currentThread == null) {
                return;
            }
            this.currentThread = currentThread;
            started = true;
        }

        synchronized void unsetCurrentThread() {
            this.currentThread = null;
        }
    }

}