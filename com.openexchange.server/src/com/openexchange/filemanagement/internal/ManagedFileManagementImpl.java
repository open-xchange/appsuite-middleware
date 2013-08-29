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

package com.openexchange.filemanagement.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileFilter;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
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

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ManagedFileManagementImpl.class));

    private static final int DELAY = 10000;

    private static final int INITIAL_DELAY = 1000;

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

        private final org.apache.commons.logging.Log logger;

        private final ConcurrentMap<String, ManagedFileImpl> tfiles;

        private final int time2live;

        public FileManagementTask(final ConcurrentMap<String, ManagedFileImpl> files, final int time2live, final org.apache.commons.logging.Log logger) {
            super();
            tfiles = files;
            this.time2live = time2live;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                final long now = System.currentTimeMillis();
                for (final Iterator<ManagedFileImpl> iter = tfiles.values().iterator(); iter.hasNext();) {
                    final ManagedFileImpl cur = iter.next();
                    final int optTimeToLive = cur.optTimeToLive();
                    if (cur.isDeleted() || ((now - cur.getLastAccess()) > (optTimeToLive > 0 ? optTimeToLive : time2live))) {
                        cur.delete();
                        iter.remove();
                    }
                }
            } catch (final Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    /*-
     * ############################ MEMBER SECTION ############################
     */

    private static final String PREFIX = "open-xchange-";

    private static final String SUFFIX = ".tmp";

    private final ConfigurationService cs;
    private final TimerService timer;
    private final ConcurrentMap<String, ManagedFileImpl> files;

    private final PropertyListener propertyListener;

    private ScheduledTimerTask timerTask;

    private final AtomicReference<File> tmpDirReference;

    public ManagedFileManagementImpl(ConfigurationService cs, TimerService timer) {
        super();
        this.cs = cs;
        this.timer = timer;
        files = new ConcurrentHashMap<String, ManagedFileImpl>();
        tmpDirReference = new AtomicReference<File>();

        propertyListener = new FileManagementPropertyListener(tmpDirReference);
        final String path = cs.getProperty("UPLOAD_DIRECTORY", propertyListener);
        tmpDirReference.set(getTmpDirByPath(path));
        // Register timer task
        timerTask = timer.scheduleWithFixedDelay(
            new FileManagementTask(files, TIME_TO_LIVE, LOG),
            INITIAL_DELAY,
            DELAY,
            TimeUnit.MILLISECONDS);
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
        for (final Iterator<ManagedFileImpl> iter = files.values().iterator(); iter.hasNext();) {
            iter.next().delete();
        }
        files.clear();
    }

    @Override
    public File newTempFile() throws OXException {
        return newTempFile(PREFIX, SUFFIX);
    }

    @Override
    public File newTempFile(final String prefix, final String suffix) throws OXException {
        File tmpFile = null;
        File directory = null;
        do {
            directory = tmpDirReference.get();
            try {
                if (null == tmpFile) {
                    tmpFile = File.createTempFile(prefix, suffix, directory);
                    tmpFile.deleteOnExit();
                } else {
                    final File tmp = File.createTempFile(prefix, suffix, directory);
                    if (!tmpFile.delete()) {
                        LOG.warn("Temporary file could not be deleted: " + tmpFile.getPath());
                    }
                    tmpFile = tmp;
                    tmpFile.deleteOnExit();
                }
            } catch (final IOException e) {
                if (tmpFile != null && !tmpFile.delete() && LOG.isWarnEnabled()) {
                    LOG.warn("Temporary file could not be deleted: " + tmpFile.getPath(), e);
                }
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            }
        } while (!tmpDirReference.compareAndSet(directory, directory)); // Directory changed in the meantime
        return tmpFile;
    }

    @Override
    public ManagedFile createManagedFile(final File temporaryFile) throws OXException {
        final ManagedFileImpl mf = new ManagedFileImpl(this, UUID.randomUUID().toString(), temporaryFile);
        mf.setSize(temporaryFile.length());
        files.put(mf.getID(), mf);
        return mf;
    }

    @Override
    public ManagedFile createManagedFile(final byte[] bytes) throws OXException {
        return createManagedFile0(null, new UnsynchronizedByteArrayInputStream(bytes), false, null, -1);
    }

    @Override
    public ManagedFile createManagedFile(final InputStream inputStream) throws OXException {
        return createManagedFile(null, inputStream);
    }

    @Override
    public ManagedFile createManagedFile(final String id, final InputStream inputStream) throws OXException {
        return createManagedFile0(id, inputStream, true, null, -1);
    }

    @Override
    public ManagedFile createManagedFile(final String id, final InputStream inputStream, final int ttl) throws OXException {
        return createManagedFile0(id, inputStream, true, null, ttl);
    }

    @Override
    public ManagedFile createManagedFile(final InputStream inputStream, final String optExtension) throws OXException {
        return createManagedFile0(null, inputStream, true, optExtension, -1);
    }

    private ManagedFile createManagedFile0(final String identifier, final InputStream inputStream, final boolean closeStream, final String optExtension, final int optTtl) throws OXException {
        if (null == inputStream) {
            throw new IllegalArgumentException("Missing input stream.");
        }
        ManagedFileImpl mf = null;
        File tmpFile = null;
        File directory = null;
        String id = identifier;
        do {
            directory = tmpDirReference.get();
            try {
                if (null == tmpFile) {
                    // Flush input stream's content via output stream to newly created file
                    tmpFile = File.createTempFile(PREFIX, null == optExtension ? SUFFIX : optExtension, directory);
                    tmpFile.deleteOnExit();
                    final OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile, false));
                    try {
                        final byte[] buf = new byte[8192];
                        int len = -1;
                        while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.flush();
                    } finally {
                        Streams.close(out);
                    }
                } else {
                    // Copy content of previous file to newly created file
                    final File tmp = File.createTempFile(PREFIX, SUFFIX, directory);
                    copyFile(tmpFile, tmp);
                    if (!tmpFile.delete()) {
                        LOG.warn("Temporary file could not be deleted: " + tmpFile.getPath());
                    }
                    tmpFile = tmp;
                    tmpFile.deleteOnExit();
                }
            } catch (final IOException e) {
                if (tmpFile != null && !tmpFile.delete() && LOG.isWarnEnabled()) {
                    LOG.warn("Temporary file could not be deleted: " + tmpFile.getPath(), e);
                }
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            } finally {
                if (closeStream) {
                    Streams.close(inputStream);
                }
            }
            if (isEmpty(id)) {
                id = UUID.randomUUID().toString();
            }
            mf = new ManagedFileImpl(this, id, tmpFile, optTtl);
            mf.setSize(tmpFile.length());
        } while (!tmpDirReference.compareAndSet(directory, directory)); // Directory changed in the meantime
        files.put(mf.getID(), mf);

        final DistributedFileManagement distributed = getDistributed();
        if (distributed != null && !distributed.exists(id)) {
            distributed.register(id);
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
        ManagedFile mf = files.get(id);
        if (null != mf) {
            // Locally available
            if (mf.isDeleted()) {
                throw ManagedFileExceptionErrorMessage.NOT_FOUND.create(id);
            }
            mf.touch();
            return mf;
        }

        // Do remote look-up
        mf = getByIDDistributed(id);
        if (null == mf || mf.isDeleted()) {
            throw ManagedFileExceptionErrorMessage.NOT_FOUND.create(id);
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
            return null;
        }

    }

    File getTmpDirByPath(final String path) {
        if (null == path) {
            throw new IllegalArgumentException("Path is null. Probably property \"UPLOAD_DIRECTORY\" is not set.");
        }
        final File tmpDir = new File(path);
        if (!tmpDir.exists()) {
            throw new IllegalArgumentException("Directory " + path + " does not exist.");
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

    public void shutDown() {
        shutDown(true);
    }

    void shutDown(final boolean complete) {
        if (complete && propertyListener != null) {
            cs.removePropertyListener("UPLOAD_DIRECTORY", propertyListener);
        }
        if (timerTask != null) {
            timerTask.cancel(true);
            timer.purge();
        }
        tmpDirReference.set(null);
        clear();
    }

    void startUp() {
        timerTask.cancel(true);
        timerTask = timer.scheduleWithFixedDelay(
            new FileManagementTask(files, TIME_TO_LIVE, LOG),
            INITIAL_DELAY,
            DELAY,
            TimeUnit.MILLISECONDS);
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
        final DistributedFileManagement service = ServerServiceRegistry.getInstance().getService(DistributedFileManagement.class);
        return service;
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
