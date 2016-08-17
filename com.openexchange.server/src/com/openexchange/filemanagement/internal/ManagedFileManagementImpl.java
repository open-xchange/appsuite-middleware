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

package com.openexchange.filemanagement.internal;

import static com.openexchange.java.Strings.isEmpty;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileFilter;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ManagedFileManagementImpl} - The file management designed to keep large content as a temporary file on disk.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedFileManagementImpl implements ManagedFileManagement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagedFileManagementImpl.class);

    private static final int DELAY = 120000;

    private class FileManagementPropertyListener implements PropertyListener {

        private final AtomicReference<File> ttmpDirReference;

        public FileManagementPropertyListener(final AtomicReference<File> tmpDirReference) {
            super();
            ttmpDirReference = tmpDirReference;
        }

        @Override
        public void onPropertyChange(final PropertyEvent event) {
            if (PropertyEvent.Type.CHANGED.equals(event.getType())) {
                ttmpDirReference.set(getTmpDirByPath(event.getValue()));
                startUp();
            } else {
                // No property for temporary directory available
                shutDown(false);
            }
        }
    }

    private static class FileManagementTask implements Runnable {

        private final org.slf4j.Logger logger;
        private final ConcurrentMap<String, ManagedFileImpl> tfiles;
        private final int time2live;
        private final AtomicReference<File> tmpDirReference;
        final String defaultPrefix;

        /**
         * Initializes a new {@link FileManagementTask}.
         */
        FileManagementTask(ConcurrentMap<String, ManagedFileImpl> files, int time2live, AtomicReference<File> tmpDirReference, final String defaultPrefix, org.slf4j.Logger logger) {
            super();
            tfiles = files;
            this.time2live = time2live;
            this.tmpDirReference = tmpDirReference;
            this.defaultPrefix = defaultPrefix;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                // Grab all existing files belonging to this JVM instance (at least those with default prefix) older than 30 minutes
                Map<String, File> orphanedFiles = null;
                {
                    File directory = tmpDirReference.get();
                    if (!directory.canRead()) {
                        logger.warn("Unable to read directory {}. {} run aborted...", directory.getAbsolutePath(), FileManagementTask.class.getSimpleName());
                        return;
                    }
                    final long stamp = System.currentTimeMillis() - 1800000; // Older than 30 minutes
                    File[] expiredFiles = directory.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            // only consider expired files with designated prefix
                            return file.isFile() && file.getName().startsWith(defaultPrefix) && file.lastModified() < stamp;
                        }
                    });
                    if (expiredFiles != null) {
                        int numberOfExpiredFiles = expiredFiles.length;
                        if (numberOfExpiredFiles > 0) {
                            orphanedFiles = new HashMap<String, File>(numberOfExpiredFiles, 0.9f);
                            for (File tmpFile : expiredFiles) {
                                orphanedFiles.put(tmpFile.getName(), tmpFile);
                            }
                        }
                    }
                }

                // Check for expired ManagedFile entries
                long now = System.currentTimeMillis();
                for (Iterator<ManagedFileImpl> filesIter = tfiles.values().iterator(); filesIter.hasNext();) {
                    ManagedFileImpl cur = filesIter.next();
                    if (null == cur) {
                        filesIter.remove();
                    } else {
                        // Expired if deleted OR time-to-live has elapsed
                        int optTimeToLive = cur.optTimeToLive();
                        long timeElapsed = now - cur.getLastAccess();
                        if (cur.isDeleted() || (timeElapsed > (optTimeToLive > 0 ? optTimeToLive : time2live))) {
                            File file = cur.getFilePlain();
                            String fname = null != file && file.exists() ? file.getName() : "";
                            cur.delete();
                            filesIter.remove();
                            logger.debug("Removed expired managed file {}", fname);
                        } else {
                            // For Safety's sake, cleanse from orphaned files
                            if (null != orphanedFiles) {
                                // Use getFileName() so that the underlying ManagedFile is not 'touched' again (something that will reset the LastAccess timestamp)
                                orphanedFiles.remove(cur.getFilePlain().getName());
                            }
                        }
                    }
                }

                // Check for orphaned files belonging to this JVM instance
                if (null != orphanedFiles) {
                    for (Map.Entry<String, File> entry : orphanedFiles.entrySet()) {
                        String name = entry.getKey();
                        File orphaned = entry.getValue();
                        if (!orphaned.delete()) {
                            logger.warn("Temporary file could not be deleted: {}", name);
                        }
                        logger.debug("Removed orphaned managed file {}", name);
                    }
                }
            } catch (Exception t) {
                logger.error("", t);
            }
        }
    }

    /*-
     * ############################ MEMBER SECTION ############################
     */

    private static final String PREFIX = "open-xchange-managedfile-" + com.openexchange.exception.OXException.getServerId() + "-";
    private static final String PREFIX_WO_EVICT = "open-xchange-newfile-" + com.openexchange.exception.OXException.getServerId() + "-";

    private static final String SUFFIX = ".tmp";

    private final ConfigurationService cs;
    private final TimerService timer;
    private final DispatcherPrefixService dispatcherPrefixService;
    private final ConcurrentMap<String, ManagedFileImpl> files;
    private final PropertyListener propertyListener;
    private final AtomicReference<File> tmpDirReference;
    private final AtomicReference<ScheduledTimerTask> timerTaskReference;

    /**
     * Initializes a new {@link ManagedFileManagementImpl}.
     */
    public ManagedFileManagementImpl(ConfigurationService cs, TimerService timer, DispatcherPrefixService dispatcherPrefixService) {
        super();
        this.cs = cs;
        this.timer = timer;
        this.dispatcherPrefixService = dispatcherPrefixService;
        files = new ConcurrentHashMap<String, ManagedFileImpl>();

        final AtomicReference<File> tmpDirReference = new AtomicReference<File>();
        this.tmpDirReference = tmpDirReference;
        propertyListener = new FileManagementPropertyListener(tmpDirReference);
        final String path = cs.getProperty("UPLOAD_DIRECTORY", propertyListener);
        tmpDirReference.set(getTmpDirByPath(path));

        // Register timer task
        ScheduledTimerTask timerTask = timer.scheduleWithFixedDelay(new FileManagementTask(files, TIME_TO_LIVE, tmpDirReference, PREFIX, LOG), DELAY, DELAY);
        timerTaskReference = new AtomicReference<ScheduledTimerTask>(timerTask);
    }

    @Override
    public InputStream createInputStream(final byte[] bytes) throws OXException {
        return new ManagedInputStream(bytes, this);
    }

    @Override
    public InputStream createInputStream(final byte[] bytes, final int capacity) throws OXException {
        return new ManagedInputStream(bytes, capacity, this);
    }

    @Override
    public InputStream createInputStream(final InputStream in) throws OXException {
        return new ManagedInputStream(in, this);
    }

    @Override
    public InputStream createInputStream(final InputStream in, final int capacity) throws OXException {
        return new ManagedInputStream(in, capacity, this);
    }

    @Override
    public InputStream createInputStream(final InputStream in, final int size, final int capacity) throws OXException {
        return new ManagedInputStream(in, size, capacity, this);
    }

    @Override
    public void clear() {
        for (ManagedFileImpl managedFileImpl : files.values()) {
            managedFileImpl.delete();
        }
        files.clear();
    }

    @Override
    public File newTempFile() throws OXException {
        // Set another prefix to exclude from auto-eviction
        return newTempFile(PREFIX_WO_EVICT, SUFFIX);
    }

    @Override
    public File newTempFile(String prefix, String suffix) throws OXException {
        String prfx = prefix;
        if (PREFIX.equals(prfx)) {
            prfx = PREFIX_WO_EVICT;
        }

        File tmpFile = null;
        File directory = null;
        do {
            directory = tmpDirReference.get();
            try {
                if (null == tmpFile) {
                    tmpFile = File.createTempFile(prfx, suffix, directory);
                    tmpFile.deleteOnExit();
                } else {
                    final File tmp = File.createTempFile(prfx, suffix, directory);
                    if (!tmpFile.delete()) {
                        LOG.warn("Temporary file could not be deleted: {}", tmpFile.getPath());
                    }
                    tmpFile = tmp;
                    tmpFile.deleteOnExit();
                }
            } catch (final IOException e) {
                if (tmpFile != null && !tmpFile.delete()) {
                    LOG.warn("Temporary file could not be deleted: {}", tmpFile.getPath(), e);
                }
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            }
        } while (!tmpDirReference.compareAndSet(directory, directory));// Directory changed in the meantime
        return tmpFile;
    }

    @Override
    public ManagedFile createManagedFile(final File temporaryFile) throws OXException {
        return createManagedFile(temporaryFile, -1);
    }

    @Override
    public ManagedFile createManagedFile(final File temporaryFile, int ttl) throws OXException {
        final ManagedFileImpl mf = new ManagedFileImpl(this, UUID.randomUUID().toString(), temporaryFile, ttl, dispatcherPrefixService.getPrefix());
        mf.setSize(temporaryFile.length());
        files.put(mf.getID(), mf);
        return mf;
    }

    @Override
    public ManagedFile createManagedFile(final byte[] bytes) throws OXException {
        return createManagedFile0(null, new UnsynchronizedByteArrayInputStream(bytes), false, null, -1);
    }

    @Override
    public ManagedFile createManagedFile(byte[] bytes, boolean distribute) throws OXException {
        return createManagedFile0(null, new UnsynchronizedByteArrayInputStream(bytes), false, null, -1, distribute);
    }

    @Override
    public ManagedFile createManagedFile(final InputStream inputStream) throws OXException {
        return createManagedFile(null, inputStream);
    }

    @Override
    public ManagedFile createManagedFile(InputStream inputStream, boolean distribute) throws OXException {
        return createManagedFile(null, inputStream, distribute);
    }

    @Override
    public ManagedFile createManagedFile(final String id, final InputStream inputStream) throws OXException {
        return createManagedFile0(id, inputStream, true, null, -1);
    }

    @Override
    public ManagedFile createManagedFile(final String id, final InputStream inputStream, boolean distribute) throws OXException {
        return createManagedFile0(id, inputStream, true, null, -1, distribute);
    }

    @Override
    public ManagedFile createManagedFile(final String id, final InputStream inputStream, final int ttl) throws OXException {
        return createManagedFile0(id, inputStream, true, null, ttl);
    }

    @Override
    public ManagedFile createManagedFile(final InputStream inputStream, final String optExtension) throws OXException {
        return createManagedFile0(null, inputStream, true, optExtension, -1);
    }

    @Override
    public ManagedFile createManagedFile(InputStream inputStream, String optExtension, int ttl) throws OXException {
        return createManagedFile0(null, inputStream, true, optExtension, ttl);
    }

    @Override
    public ManagedFile createManagedFile(InputStream inputStream, int ttl) throws OXException {
        return createManagedFile0(null, inputStream, true, null, ttl);
    }

    private ManagedFile createManagedFile0(String identifier, InputStream inputStream, boolean closeStream, String optExtension, int optTtl) throws OXException {
        return createManagedFile0(identifier, inputStream, closeStream, optExtension, optTtl, true);
    }

    private ManagedFile createManagedFile0(String identifier, InputStream inputStream, boolean closeStream, String optExtension, int optTtl, boolean distribute) throws OXException {
        if (null == inputStream) {
            throw new IllegalArgumentException("Missing input stream.");
        }

        ManagedFileImpl mf = null;
        File tmpFile = null;
        File directory = null;
        String id = identifier;
        do {
            directory = tmpDirReference.get();
            boolean errorDuringFileCreation = true;
            try {
                if (null == tmpFile) {
                    // Create new file
                    tmpFile = File.createTempFile(PREFIX, null == optExtension ? SUFFIX : optExtension, directory);
                    tmpFile.deleteOnExit();

                    // Flush input stream's content via output stream to newly created file
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile, false));
                    try {
                        int blen = 65536;
                        byte[] buf = new byte[blen];
                        for (int read; (read = inputStream.read(buf, 0, blen)) > 0;) {
                            out.write(buf, 0, read);
                        }
                        out.flush();
                    } finally {
                        Streams.close(out);
                    }
                } else {
                    // Create new file
                    File newTmpFile = File.createTempFile(PREFIX, null == optExtension ? SUFFIX : optExtension, directory);
                    newTmpFile.deleteOnExit();

                    // Copy content of previous file to newly created file
                    boolean errorDuringCopy = true;
                    try {
                        copyFile(tmpFile, newTmpFile);
                        errorDuringCopy = false;
                    } finally {
                        if (errorDuringCopy && !newTmpFile.delete()) {
                            LOG.warn("Temporary file could not be deleted: {}", newTmpFile.getPath());
                        }
                    }

                    // Delete the old file
                    if (!tmpFile.delete()) {
                        LOG.warn("Temporary file could not be deleted: {}", tmpFile.getPath());
                    }
                    tmpFile = newTmpFile;
                }

                // Unset error marker
                errorDuringFileCreation = false;
            } catch (IOException e) {
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            } catch (RuntimeException e) {
                throw ManagedFileExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (closeStream) {
                    Streams.close(inputStream);
                }
                if (errorDuringFileCreation && null != tmpFile) {
                    if (!tmpFile.delete()) {
                        LOG.warn("Temporary file could not be deleted: {}", tmpFile.getPath());
                    }
                }
            }

            // Create ManagedFileImpl instance
            if (isEmpty(id)) {
                id = UUID.randomUUID().toString();
            }
            mf = new ManagedFileImpl(this, id, tmpFile, optTtl, dispatcherPrefixService.getPrefix());
            mf.setFileName(tmpFile.getName());
            mf.setSize(tmpFile.length());
        } while (!tmpDirReference.compareAndSet(directory, directory));// Directory changed in the meantime
        // Put into map
        files.put(mf.getID(), mf);
        // Check whether supposed to be distributed
        if (distribute) {
            DistributedFileManagement distributed = getDistributed();
            if (distributed != null && !distributed.exists(id)) {
                distributed.register(id);
            }
        }
        return mf;
    }

    @Override
    public boolean containsLocal(final String id) {
        final ManagedFile mf = files.get(id);
        if (null == mf || mf.isDeleted()) {
            return false;
        }
        mf.touch();
        return true;
    }

    @Override
    public boolean contains(final String id) {
        final ManagedFile mf = files.get(id);
        if (null == mf || mf.isDeleted()) {
            return containsDistributed(id);
        }
        mf.touch();
        return true;
    }

    private boolean containsDistributed(final String id) {
        final DistributedFileManagement distributedFileManagement = getDistributed();
        if (distributedFileManagement == null) {
            return false;
        }

        try {
            if (distributedFileManagement.exists(id)) {
                distributedFileManagement.touch(id);
                return true;
            }
        } catch (final OXException e) {
            return false;
        }

        return false;
    }

    @Override
    public List<ManagedFile> getManagedFiles() throws OXException {
        return getManagedFiles(null);
    }

    @Override
    public List<ManagedFile> getManagedFiles(final ManagedFileFilter filter) throws OXException {
        if (null == filter) {
            return new ArrayList<ManagedFile>(files.values());
        }
        final List<ManagedFile> list = new ArrayList<ManagedFile>(files.size());
        for (final ManagedFile managedFile : files.values()) {
            if (filter.accept(managedFile)) {
                list.add(managedFile);
            }
        }
        return list;
    }

    @Override
    public ManagedFile getByID(final String id) throws OXException {
        ManagedFile mf = optByID(id);
        if (null == mf) {
            throw ManagedFileExceptionErrorMessage.NOT_FOUND.create(id);
        }
        return mf;
    }

    @Override
    public ManagedFile optByID(String id) {
        ManagedFile mf = files.get(id);
        if (null != mf) {
            // Locally available
            if (mf.isDeleted()) {
                return null;
            }
            mf.touch();
            return mf;
        }

        // Do remote look-up
        mf = getByIDDistributed(id);
        if (null == mf || mf.isDeleted()) {
            return null;
        }
        mf.touch();
        return mf;
    }

    private ManagedFile getByIDDistributed(final String id) {
        final DistributedFileManagement distributedFileManagement = getDistributed();
        if (distributedFileManagement == null) {
            return null;
        }

        try {
            if (!distributedFileManagement.exists(id)) {
                return null;
            }
            // Get remote file
            final ManagedFile managedFile = createManagedFile(id, distributedFileManagement.get(id));
            // Safe touch
            try {
                distributedFileManagement.touch(id);
            } catch (final Exception e) {
                // Ignore
            }
            // Return
            return managedFile;
        } catch (final OXException e) {
            LOG.warn("Could not load remote file: {}", id, e);
            return null;
        }

    }

    File getTmpDirByPath(final String path) {
        if (null == path) {
            throw new IllegalArgumentException("Path is null. Probably property \"UPLOAD_DIRECTORY\" is not set.");
        }
        final File tmpDir = new File(path);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                throw new IllegalArgumentException("Directory " + path + " does not exist and can not be created.");
            }
            LOG.info("Directory " + path + " did not exist, but could be created.");
        }
        if (!tmpDir.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory.");
        }
        return tmpDir;
    }

    @Override
    public void removeByID(final String id) {
        final ManagedFile mf = files.get(id);
        if (null == mf) {
            removeByIDDistributed(id);
            return;
        }
        try {
            if (!mf.isDeleted()) {
                mf.delete();
                final DistributedFileManagement distributedFileManagement = getDistributed();
                if (distributedFileManagement != null) {
                    distributedFileManagement.unregister(id);
                }
            }
        } catch (final OXException e) {
            // Do nothing.
        } finally {
            files.remove(mf.getID());
        }
    }

    private void removeByIDDistributed(final String id) {
        final DistributedFileManagement distributedFileManagement = getDistributed();
        if (distributedFileManagement == null) {
            return;
        }

        try {
            distributedFileManagement.remove(id);
        } catch (final OXException e) {
            // Do nothing.
        }
    }

    void removeFromFiles(final String id) {
        final DistributedFileManagement distributedFileManagement = getDistributed();
        if (distributedFileManagement != null) {
            try {
                distributedFileManagement.unregister(id);
            } catch (final OXException e) {
                // Do nothing.
            }
        }
        files.remove(id);
    }

    /**
     * Shuts-down this managed file management instance.
     */
    public void shutDown() {
        shutDown(true);
    }

    void shutDown(final boolean complete) {
        if (complete && propertyListener != null) {
            cs.removePropertyListener("UPLOAD_DIRECTORY", propertyListener);
        }

        stopTimerTask();

        tmpDirReference.set(null);
        clear();
    }

    private boolean stopTimerTask() {
        ScheduledTimerTask timerTask;
        do {
            timerTask = timerTaskReference.get();
            if (null == timerTask) {
                return false;
            }
        } while (!timerTaskReference.compareAndSet(timerTask, null));
        timerTask.cancel(true);
        timer.purge();
        return true;
    }

    void startUp() {
        if (stopTimerTask()) {
            timerTaskReference.set(timer.scheduleWithFixedDelay(new FileManagementTask(files, TIME_TO_LIVE, tmpDirReference, PREFIX, LOG), DELAY, DELAY));
        }
    }

    private static void copyFile(final File sourceFile, final File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private DistributedFileManagement getDistributed() {
        return ServerServiceRegistry.getInstance().getService(DistributedFileManagement.class);
    }
}
