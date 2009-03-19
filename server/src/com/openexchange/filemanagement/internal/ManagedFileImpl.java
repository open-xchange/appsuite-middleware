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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileException;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileExceptionFactory;

/**
 * {@link ManagedFileImpl} - Implementation of a managed file.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ManagedFileImpl implements ManagedFile, FileRemovedRegistry {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ManagedFileImpl.class);

    private final String id;

    private final File file;

    private volatile long lastAccessed;

    private final BlockingQueue<FileRemovedListener> listeners;

    private volatile String contentType;

    private volatile String fileName;

    private volatile long size;

    /**
     * Initializes a new {@link ManagedFileImpl}.
     * 
     * @param id The unique ID
     * @param file The kept file
     */
    ManagedFileImpl(final String id, final File file) {
        super();
        this.id = id;
        this.file = file;
        lastAccessed = System.currentTimeMillis();
        listeners = new LinkedBlockingQueue<FileRemovedListener>();
    }

    public void delete() {
        if (file.exists()) {
            while (!listeners.isEmpty()) {
                final FileRemovedListener frl = listeners.poll();
                if (null != frl) {
                    frl.removePerformed(file);
                }
            }
            if (!file.delete() && LOG.isWarnEnabled()) {
                LOG.warn("Temporary file could not be deleted: " + file.getPath());
            }
        }
        ManagedFileManagementImpl.getInstance().removeFromFiles(id);
    }

    public File getFile() {
        if (!file.exists()) {
            return null;
        }
        touch();
        return file;
    }

    public long getLastAccess() {
        return lastAccessed;
    }

    public boolean isDeleted() {
        return !file.exists();
    }

    public void touch() {
        lastAccessed = System.currentTimeMillis();
    }

    public String getID() {
        return id;
    }

    public InputStream getInputStream() throws ManagedFileException {
        if (!file.exists()) {
            return null;
        }
        touch();
        try {
            final CallbackInputStream retval = new CallbackInputStream(new BufferedInputStream(new FileInputStream(file)), this);
            listeners.offer(retval);
            return retval;
        } catch (final FileNotFoundException e) {
            throw ManagedFileExceptionFactory.getInstance().create(ManagedFileExceptionErrorMessage.FILE_NOT_FOUND, e, file.getPath());
        }
    }

    public void removeListener(final FileRemovedListener listener) {
        listeners.remove(listener);
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }
}
