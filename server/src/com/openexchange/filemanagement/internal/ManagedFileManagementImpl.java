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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileException;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileExceptionFactory;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.Timer;

/**
 * {@link ManagedFileManagementImpl} - The file management designed to keep large content as a temporary file on disk.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ManagedFileManagementImpl implements ManagedFileManagement {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ManagedFileManagementImpl.class);

    private static final int DELAY = 10000;

    private static final int INITIAL_DELAY = 1000;

    private class FileManagementPropertyListener implements PropertyListener {

        private final AtomicReference<File> ttmpDirReference;

        public FileManagementPropertyListener(final AtomicReference<File> tmpDirReference) {
            super();
            ttmpDirReference = tmpDirReference;
        }

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

        private final ConcurrentMap<String, ManagedFile> tfiles;

        private final int time2live;

        public FileManagementTask(final ConcurrentMap<String, ManagedFile> files, final int time2live, final org.apache.commons.logging.Log logger) {
            super();
            tfiles = files;
            this.time2live = time2live;
            this.logger = logger;
        }

        public void run() {
            try {
                final long now = System.currentTimeMillis();
                for (final Iterator<ManagedFile> iter = tfiles.values().iterator(); iter.hasNext();) {
                    final ManagedFile cur = iter.next();
                    if (cur.isDeleted() || ((now - cur.getLastAccess()) > time2live)) {
                        cur.delete();
                        iter.remove();
                    }
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static volatile ManagedFileManagementImpl instance;

    /*-
     * ############################ MEMBER SECTION ############################
     */

    private static final String PREFIX = "open-xchange-";

    private static final String SUFFIX = ".tmp";

    private static final int TIME_TO_LIVE = 300000;

    /**
     * Gets the file management instance.
     * 
     * @return The file management instance
     */
    static ManagedFileManagementImpl getInstance() {
        ManagedFileManagementImpl tmp = instance;
        if (tmp == null) {
            synchronized (ManagedFileManagementImpl.class) {
                tmp = instance;
                if (tmp == null) {
                    tmp = instance = new ManagedFileManagementImpl();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the file management instance.
     */
    static void releaseInstance() {
        if (instance != null) {
            synchronized (ManagedFileManagementImpl.class) {
                if (instance != null) {
                    instance.shutDown(true);
                    instance = null;
                }
            }
        }
    }

    private final ConcurrentMap<String, ManagedFile> files;

    private PropertyListener propertyListener;

    private ScheduledTimerTask timerTask;

    private final AtomicReference<File> tmpDirReference;

    /**
     * Initializes a new {@link ManagedFileManagementImpl}.
     */
    private ManagedFileManagementImpl() {
        super();
        files = new ConcurrentHashMap<String, ManagedFile>();
        tmpDirReference = new AtomicReference<File>();
        ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
        // Get configuration service
        final ConfigurationService cs = registry.getService(ConfigurationService.class);
        if (null == cs) {
            throw new IllegalStateException("Missing configuration service");
        }
        final String path = cs.getProperty("UPLOAD_DIRECTORY", (propertyListener = new FileManagementPropertyListener(tmpDirReference)));
        tmpDirReference.set(getTmpDirByPath(path));
        // Register timer task
        final Timer timer = registry.getService(Timer.class);
        if (null == timer) {
            throw new IllegalStateException("Missing timer service");
        }
        timerTask = timer.scheduleWithFixedDelay(
            new FileManagementTask(files, TIME_TO_LIVE, LOG),
            INITIAL_DELAY,
            DELAY,
            TimeUnit.MILLISECONDS);
    }

    public InputStream createInputStream(final byte[] bytes) throws ManagedFileException {
        return new ManagedInputStream(bytes, this);
    }

    public InputStream createInputStream(final byte[] bytes, final int capacity) throws ManagedFileException {
        return new ManagedInputStream(bytes, capacity, this);
    }

    public InputStream createInputStream(final InputStream in) throws ManagedFileException {
        return new ManagedInputStream(in, this);
    }

    public InputStream createInputStream(final InputStream in, final int capacity) throws ManagedFileException {
        return new ManagedInputStream(in, capacity, this);
    }

    public InputStream createInputStream(final InputStream in, final int size, final int capacity) throws ManagedFileException {
        return new ManagedInputStream(in, size, capacity, this);
    }

    public void clear() {
        for (final Iterator<ManagedFile> iter = files.values().iterator(); iter.hasNext();) {
            iter.next().delete();
        }
        files.clear();
    }

    public ManagedFile createManagedFile(final byte[] bytes) throws ManagedFileException {
        ManagedFile mf = null;
        File tmpFile = null;
        do {
            OutputStream out = null;
            try {
                tmpFile = File.createTempFile(PREFIX, SUFFIX, tmpDirReference.get());
                tmpFile.deleteOnExit();
                out = new BufferedOutputStream(new FileOutputStream(tmpFile, false));
                out.write(bytes, 0, bytes.length);
                out.flush();
            } catch (final IOException e) {
                if (!tmpFile.delete() && LOG.isWarnEnabled()) {
                    LOG.warn("Temporary file could not be deleted: " + tmpFile.getPath(), e);
                }
                throw ManagedFileExceptionFactory.getInstance().create(ManagedFileExceptionErrorMessage.IO_ERROR, e, e.getMessage());
            } finally {
                if (null != out) {
                    try {
                        out.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
            mf = new ManagedFileImpl(UUID.randomUUID().toString(), tmpFile);
        } while (!tmpDirReference.compareAndSet(tmpFile, tmpFile)); // Directory changed in the meantime
        files.put(mf.getID(), mf);
        return mf;
    }

    public ManagedFile createManagedFile(final InputStream inputStream) throws ManagedFileException {
        ManagedFile mf = null;
        File tmpFile = null;
        do {
            OutputStream out = null;
            try {
                tmpFile = File.createTempFile(PREFIX, SUFFIX, tmpDirReference.get());
                tmpFile.deleteOnExit();
                out = new BufferedOutputStream(new FileOutputStream(tmpFile, false));
                final byte[] buf = new byte[8192];
                int len = -1;
                while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
            } catch (final IOException e) {
                if (!tmpFile.delete() && LOG.isWarnEnabled()) {
                    LOG.warn("Temporary file could not be deleted: " + tmpFile.getPath(), e);
                }
                throw ManagedFileExceptionFactory.getInstance().create(ManagedFileExceptionErrorMessage.IO_ERROR, e, e.getMessage());
            } finally {
                if (null != out) {
                    try {
                        out.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
            mf = new ManagedFileImpl(UUID.randomUUID().toString(), tmpFile);
        } while (!tmpDirReference.compareAndSet(tmpFile, tmpFile)); // Directory changed in the meantime
        files.put(mf.getID(), mf);
        return mf;
    }

    public ManagedFile getByID(final String id) throws ManagedFileException {
        final ManagedFile mf = files.get(id);
        if (null == mf || mf.isDeleted()) {
            throw ManagedFileExceptionFactory.getInstance().create(ManagedFileExceptionErrorMessage.NOT_FOUND, id);
        }
        mf.touch();
        return mf;
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

    public void removeByID(final String id) {
        final ManagedFile mf = files.get(id);
        if (null == mf) {
            return;
        }
        try {
            if (!mf.isDeleted()) {
                mf.delete();
            }
        } finally {
            files.remove(mf.getID());
        }
    }

    void removeFromFiles(final String id) {
        files.remove(id);
    }

    void shutDown(final boolean complete) {
        if (complete && propertyListener != null) {
            final ConfigurationService cs = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            if (null != cs) {
                cs.removePropertyListener("UPLOAD_DIRECTORY", propertyListener);
            }
            propertyListener = null;
        }
        if (timerTask != null) {
            timerTask.cancel(true);
            final Timer timer = ServerServiceRegistry.getInstance().getService(Timer.class);
            if (null != timer) {
                timer.purge();
            }
            timerTask = null;
        }
        tmpDirReference.set(null);
        clear();
    }

    void startUp() {
        if (timerTask == null) {
            // Register timer task
            final Timer timer = ServerServiceRegistry.getInstance().getService(Timer.class);
            if (null == timer) {
                throw new IllegalStateException("Missing timer service");
            }
            timerTask = timer.scheduleWithFixedDelay(
                new FileManagementTask(files, TIME_TO_LIVE, LOG),
                INITIAL_DELAY,
                DELAY,
                TimeUnit.MILLISECONDS);
        }
    }

}
