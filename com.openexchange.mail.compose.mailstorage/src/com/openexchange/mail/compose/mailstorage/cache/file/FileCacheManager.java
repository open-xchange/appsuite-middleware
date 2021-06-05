/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.compose.mailstorage.cache.file;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.MailExceptionCode.getSize;
import static java.util.stream.Collectors.summingLong;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.TmpFileFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.SetableFuture;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.mail.compose.mailstorage.cache.CacheManager;
import com.openexchange.mail.compose.mailstorage.cache.Result;
import com.openexchange.metrics.micrometer.Micrometer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;


/**
 * An {@link CacheManager} that manages locally cached messages as raw MIME files on local disk.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class FileCacheManager implements CacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(FileCacheManager.class);

    private static final String FILE_NAME_PREFIX = "csmsg-";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final File cacheDir;
    private final AtomicReference<Future<ScheduledTimerTask>> timerTaskReference;
    private final ServiceLookup services;
    private final SetableFuture<Boolean> startUpLatch;
    private final AtomicBoolean deprecatedFlag;

    /**
     * Initializes a new {@link FileCacheManager}.
     *
     * @param cacheDir The cache directory
     * @param services The service look-up
     */
    public FileCacheManager(File cacheDir, ServiceLookup services) {
        super();
        this.services = services;
        this.cacheDir = cacheDir;
        timerTaskReference = new AtomicReference<>(null);
        startUpLatch = new SetableFuture<Boolean>();
        deprecatedFlag = new AtomicBoolean(false);
    }

    /**
     * Gets the cache directory.
     *
     * @return The cache directory
     */
    public File getCacheDir() {
        return cacheDir;
    }

    /**
     * Starts-up this cache manager instance.
     *
     * @throws OXException If start-up failed
     */
    public void startUp() throws OXException {
        try {
            initMetrics();
            initCleanUpTask(MailStorageCompositionSpaceConfig.getInstance().getFileCacheMaxIdleSeconds(), services.getServiceSafe(TimerService.class));
            startUpLatch.set(Boolean.TRUE);
            LOG.info("File cache start-up successful. Used directory: {}", cacheDir.getAbsolutePath());
        } catch (OXException e) {
            startUpLatch.setException(e);
            LOG.info("File cache start-up failed. Used directory: {}", cacheDir.getAbsolutePath(), e);
            throw e;
        } catch (Exception e) {
            startUpLatch.setException(e);
            LOG.info("File cache start-up failed. Used directory: {}", cacheDir.getAbsolutePath(), e);
            throw CompositionSpaceErrorCode.ERROR.create(e, "Failed start-up of file cache");
        }
    }

    /**
     * Awaits the start-up.
     *
     * @throws Exception If start-up fails
     */
    public void awaitStartUp() throws OXException {
        // Await start-up
        try {
            if (deprecatedFlag.compareAndSet(true, false)) {
                // Re-accessed formerly deprecated file cache...
                initCleanUpTask(MailStorageCompositionSpaceConfig.getInstance().getFileCacheMaxIdleSeconds(), services.getServiceSafe(TimerService.class));
            }
            startUpLatch.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CompositionSpaceErrorCode.ERROR.create(e, "Interrupted during start-up");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw CompositionSpaceErrorCode.ERROR.create(cause == null ? e : cause, "Failed start-up of file cache");
        }
    }

    /**
     * Shuts-down this cache manager instance.
     */
    public void shutDown() {
        dropCleanUpTask(Optional.ofNullable(services.getOptionalService(TimerService.class)));
        LOG.info("File cache shut-down. Used directory: {}", cacheDir.getAbsolutePath());
    }

    /**
     * Marks this file cache manager as deprecated.
     */
    public void markDeprecated() {
        deprecatedFlag.set(true);
        LOG.info("File cache marked as deprecated. Used directory: {}", cacheDir.getAbsolutePath());
    }

    /**
     * Re-initializes this cache manager's clean-up task.
     *
     * @param maxIdleSeconds The max. idle seconds
     * @throws OXException If clean-up task cannot be re-initialized
     */
    public void reinitCleanUpTaskWith(long maxIdleSeconds) throws OXException {
        TimerService timerService = services.getServiceSafe(TimerService.class);
        dropCleanUpTask(Optional.of(timerService));
        initCleanUpTask(maxIdleSeconds, timerService);
    }

    @Override
    public Result cacheMessage(UUID compositionSpaceId, MimeMessage mimeMessage) {
        // Try to cache MIME message
        File mimeFile = null;
        long fileSize = -1L;
        FileOutputStream fos = null;
        try {
            // Create file
            mimeFile = TmpFileFileHolder.newTempFile(FILE_NAME_PREFIX + UUIDs.getUnformattedString(compositionSpaceId) + "-", false, this.cacheDir);

            // Flush MIME message content to file
            fos = new FileOutputStream(mimeFile);
            mimeMessage.writeTo(fos);
            fos.flush();
            Streams.close(fos);
            fos = null;
            fileSize = mimeFile.length();

            // Create reference-carrying result for it
            LOG.debug("Created cache file for composition space {}: {} ({})", UUIDs.getUnformattedString(compositionSpaceId), mimeFile.getAbsolutePath(), getSize(fileSize, 0, false, true));
            Result result = Result.successfulResultFor(new FileCacheReferenceImpl(mimeFile));
            mimeFile = null;
            return result;
        } catch (Exception e) {
            return Result.exceptionResultFor(e);
        } finally {
            Streams.close(fos);
            if (mimeFile == null) {
                // Cache file successfully created if file size is greater than 0 (zero)
                if (fileSize > 0L) {
                    Counter.builder("appsuite.mailcompose.cache.files.created")
                    .register(Metrics.globalRegistry)
                    .increment();
                }
            } else {
                // Something went wrong; otherwise it would be null
                if (!mimeFile.delete()) {
                    LOG.error("Failed to delete cache file for composition space {}: {}", UUIDs.getUnformattedString(compositionSpaceId), mimeFile.getAbsolutePath());
                }
            }
        }
    }

    // ------------------------------------------------- Metrics stuff ---------------------------------------------------------------------

    private void initMetrics() {
        Tags tags = Tags.of("dir", cacheDir.getAbsolutePath());
        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry, "appsuite.mailcompose.cache.files.total",
            tags, "Number of locally cached MIME files holding composition space drafts",
            null,
            this, (c) -> c.getCacheFiles().length);
        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry, "appsuite.mailcompose.cache.files.size.total",
            tags, "Size of all locally cached MIME files holding composition space drafts",
            "bytes",
            this, (c) -> c.getCacheFileStream().collect(summingLong(f -> f.length())));
        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry, "appsuite.mailcompose.cache.space.free",
            tags, "Usable (free) disk space for MIME files holding composition space drafts",
            "bytes",
            this, (c) -> {
                File cacheDir = c.getCacheDir();
                if (cacheDir == null) {
                    return Double.valueOf(-1.0D);
                }

                return Double.valueOf(cacheDir.getUsableSpace());
            });
        Micrometer.registerOrUpdateGauge(Metrics.globalRegistry, "appsuite.mailcompose.cache.space.total",
            tags, "Total disk space for MIME files holding composition space drafts",
            "bytes",
            this, (c) -> {
                File cacheDir = c.getCacheDir();
                if (cacheDir == null) {
                    return Double.valueOf(-1.0D);
                }

                return Double.valueOf(cacheDir.getTotalSpace());
            });
    }

    // ------------------------------------------------- Clean-up stuff --------------------------------------------------------------------

    /**
     * Initializes the clean-up task for file cache (if not done yet).
     *
     * @param maxIdleSeconds The max. idle seconds
     * @param timerService The timer service to use
     */
    private void initCleanUpTask(long maxIdleSeconds, TimerService timerService) {
        FutureTask<ScheduledTimerTask> ft = new FutureTask<>(() -> doInitCleanUpTask(maxIdleSeconds, timerService));
        if (timerTaskReference.compareAndSet(null, ft)) {
            ft.run();
            LOG.info("Initialized clean-up task for directory: {}", cacheDir.getAbsolutePath());
        }
    }

    private ScheduledTimerTask doInitCleanUpTask(long maxIdleSeconds, TimerService timerService) {
        Runnable task = () -> {
            try {
                int numDeleted = 0;
                File[] files = getCacheFiles();
                if (files.length > 0) {
                    long now = System.currentTimeMillis();
                    for (File file : files) {
                        long lastModified = file.lastModified();
                        if (lastModified <= (now - TimeUnit.SECONDS.toMillis(maxIdleSeconds))) {
                            if (file.delete()) {
                                LOG.debug("Deleted expired file from cache: {}", file);
                                ++numDeleted;
                                incrementDeleteCounter("expired");
                            }
                        }
                    }
                } else {
                    if (deprecatedFlag.get()) {
                        // Deprecated and no further files in cache directory
                        dropCleanUpTask(Optional.ofNullable(services.getOptionalService(TimerService.class)));
                        LOG.info("No further files in deprecated file cache directory: {}", cacheDir.getAbsolutePath());
                        return;
                    }
                }
                LOG.info("File cache clean-up run done. Deleted {} expired files from cache from directory: {}", I(numDeleted), cacheDir.getAbsolutePath());
            } catch (Exception e) {
                LOG.warn("File cache clean-up run failed. Clould no check for expired files in directory: {}", cacheDir.getAbsolutePath(), e);
            }
        };

        long delay = maxIdleSeconds / 2;
        return timerService.scheduleWithFixedDelay(task, 0L, delay, TimeUnit.SECONDS);
    }

    /**
     * Drops the clean-up task (if not done yet).
     *
     * @param optionalTimerService The optional timer service
     */
    private void dropCleanUpTask(Optional<TimerService> optionalTimerService) {
        Future<ScheduledTimerTask> timerTaskFuture = timerTaskReference.getAndSet(null);
        if (timerTaskFuture != null) {
            try {
                ThreadPools.getFrom(timerTaskFuture).cancel();
                if (optionalTimerService.isPresent()) {
                    optionalTimerService.get().purge();
                }
                LOG.info("Dropped clean-up task for directory: {}", cacheDir.getAbsolutePath());
            } catch (Exception e) {
                LOG.warn("Failed to drop clean-up task for directory: {}", cacheDir.getAbsolutePath(), e);
            }
        }
    }

    private Stream<File> getCacheFileStream() {
        return StreamSupport.stream(Arrays.spliterator(getCacheFiles()), true);
    }

    private File[] getCacheFiles() {
        File cacheDir = this.cacheDir;
        File[] files = cacheDir.listFiles(f -> f.isFile() && f.canWrite() && f.getName().startsWith(FILE_NAME_PREFIX));
        if (files == null) {
            // ... Returns null if this abstract pathname does not denote a directory, or if an I/O error occurs
            LOG.warn("File cache clean-up run failed. Failed to list available files in cache directory: {}", cacheDir.getAbsolutePath());
            files = new File[0];
        }
        return files;
    }

    static void incrementDeleteCounter(String reason) {
        Counter.builder("appsuite.mailcompose.cache.files.deleted")
            .description("The number of locally cached MIME files that were deleted again since server start")
            .tag("reason", reason)
            .register(Metrics.globalRegistry)
            .increment();
    }

}
