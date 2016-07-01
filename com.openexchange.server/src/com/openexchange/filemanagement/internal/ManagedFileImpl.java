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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.openexchange.ajax.requesthandler.DefaultDispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;

/**
 * {@link ManagedFileImpl} - Implementation of a managed file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedFileImpl implements ManagedFile, FileRemovedRegistry, TtlAware {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagedFileImpl.class);

    private final ManagedFileManagementImpl management;
    private final String id;
    private final File file;
    private final int optTtl;

    private final BlockingQueue<FileRemovedListener> listeners;

    private volatile long lastAccessed;
    private volatile String contentType;
    private volatile String fileName;
    private volatile long size;
    private volatile String contentDisposition;
    private volatile String affiliation;

    /**
     * Initializes a new {@link ManagedFileImpl}.
     *
     * @param id The unique ID
     * @param file The kept file
     */
    public ManagedFileImpl(ManagedFileManagementImpl management, String id, File file) {
        this(management, id, file, -1);
    }

    /**
     * Initializes a new {@link ManagedFileImpl}.
     *
     * @param id The unique ID
     * @param file The kept file
     * @param optTtl The optional TTL
     */
    public ManagedFileImpl(ManagedFileManagementImpl management, String id, File file, int optTtl) {
        super();
        this.management = management;
        this.optTtl = optTtl;
        this.id = id;
        this.file = file;
        lastAccessed = System.currentTimeMillis();
        listeners = new LinkedBlockingQueue<FileRemovedListener>();
    }

    @Override
    public int optTimeToLive() {
        return optTtl;
    }

    @Override
    public String constructURL(final Session session) throws OXException {
        if (null != contentType && contentType.regionMatches(true, 0, "image/", 0, 6)) {
            return new ManagedFileImageDataSource(management).generateUrl(new ImageLocation.Builder(id).build(), session);
        }
        final StringBuilder sb = new StringBuilder(64);
        final String prefix;
        final String route;
        {
            final HostData hostData = (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
            if (hostData == null) {
                /*
                 * Compose relative URL
                 */
                prefix = "";
                route = null;
            } else {
                /*
                 * Compose absolute URL
                 */
                sb.append(hostData.isSecure() ? "https://" : "http://");
                sb.append(hostData.getHost());
                final int port = hostData.getPort();
                if ((hostData.isSecure() && port != 443) || (!hostData.isSecure() && port != 80)) {
                    sb.append(':').append(port);
                }
                prefix = sb.toString();
                sb.setLength(0);
                route = hostData.getRoute();
            }
        }
        /*
         * Compose URL parameters
         */
        sb.append(prefix).append(DefaultDispatcherPrefixService.getInstance().getPrefix()).append("file");
        if (null != route) {
            sb.append(";jsessionid=").append(route);
        }
        sb.append('?').append("id=").append(id);
        sb.append('&').append("session=").append(session.getSessionID());
        sb.append('&').append("action=get");
        return sb.toString();
    }

    @Override
    public void delete() {
        if (file.exists()) {
            while (!listeners.isEmpty()) {
                final FileRemovedListener frl = listeners.poll();
                if (null != frl) {
                    frl.removePerformed(file);
                }
            }
            if (!file.delete()) {
                LOG.warn("Temporary file could not be deleted: {}", file.getPath());
            }
        }
        management.removeFromFiles(id);
    }

    @Override
    public File getFile() {
        if (!file.exists()) {
            return null;
        }
        touch();
        return file;
    }

    /**
     * Gets the file reference w/o touching last-accessed time stamp
     *
     * @return The file
     */
    public File getFilePlain() {
        return file;
    }

    @Override
    public long getLastAccess() {
        return lastAccessed;
    }

    @Override
    public boolean isDeleted() {
        return !file.exists();
    }

    @Override
    public void touch() {
        lastAccessed = System.currentTimeMillis();
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        if (!file.exists()) {
            return null;
        }
        touch();
        try {
            final CallbackInputStream retval = new CallbackInputStream(new BufferedInputStream(new FileInputStream(file), 65536), this);
            listeners.offer(retval);
            return retval;
        } catch (final FileNotFoundException e) {
            throw ManagedFileExceptionErrorMessage.FILE_NOT_FOUND.create(e, file.getPath());
        }
    }

    @Override
    public int writeTo(OutputStream out, int off, int len) throws OXException {
        if (null == out) {
            return 0;
        }
        if (!file.exists()) {
            return -1;
        }
        touch();
        RandomAccessFile raf = null;
        try {
            final File tmpFile = file;
            raf = new RandomAccessFile(tmpFile, "r");
            final long total = raf.length();
            if (off >= total) {
                return 0;
            }
            // Check available bytes
            {
                final long actualLen = total - off;
                if (actualLen < len) {
                    len = (int) actualLen;
                }
            }
            // Set file pointer & start reading
            raf.seek(off);
            final int buflen = 2048;
            final byte[] bytes = new byte[buflen];
            int n = 0;
            while (n < len) {
                final int available = len - n;
                final int read = raf.read(bytes, 0, buflen > available ? available : buflen);
                if (read > 0) {
                    out.write(bytes, 0, read);
                    n += read;
                } else {
                    break;
                }
            }
            return n;
        } catch (final IOException e) {
            throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(raf);
        }

    }

    @Override
    public void removeListener(final FileRemovedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public String getContentDisposition() {
        return contentDisposition;
    }

    @Override
    public void setContentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }
}
