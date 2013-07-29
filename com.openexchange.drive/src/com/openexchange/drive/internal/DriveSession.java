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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jonelo.jacksum.algorithm.MD;
import org.apache.commons.logging.Log;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
import com.openexchange.drive.storage.DriveStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DriveSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveSession {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveSession.class);

    private final ServerSession session;

    private ChecksumStore checksumStore;
    private final String rootFolderID;
    private DriveStorage storage;
    private DirectLinkGenerator linkGenerator;
    private StringAllocator diagnosticsLog;
    private Boolean diagnostics;

    /**
     * Initializes a new {@link DriveSession}.
     *
     * @param session The underlying server session
     * @param rootFolderID The root folder ID
     */
    public DriveSession(ServerSession session, String rootFolderID) {
        super();
        this.session = session;
        this.rootFolderID = rootFolderID;
    }

    /**
     * Gets the underlying server session
     *
     * @return The server session
     */
    public ServerSession getServerSession() {
        return session;
    }

    /**
     * Gets the drive storage
     *
     * @return The drive storage
     */
    public DriveStorage getStorage() {
        if (null == storage) {
            storage = new DriveStorage(this, rootFolderID);
        }
        return storage;
    }

    /**
     * Gets the checksumStore
     *
     * @return The checksumStore
     */
    public ChecksumStore getChecksumStore() throws OXException {
        if (null == checksumStore) {
            checksumStore = new RdbChecksumStore(getServerSession().getContextId());
        }
        return checksumStore;
    }

    /**
     * Creates a new MD5 instance.
     *
     * @return A new MD5 instance.
     * @throws OXException
     */
    public MD newMD5() throws OXException {
        try {
            return new MD("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the device name as supplied by the client.
     *
     * @return The device name, or <code>null</code> if not set
     */
    public String getDeviceName() {
        Object parameter = getServerSession().getParameter("com.openexchange.drive.device");
        return null != parameter && String.class.isInstance(parameter) ? (String)parameter : null;
    }

    public DirectLinkGenerator getLinkGenerator() {
        if (null == this.linkGenerator) {
            linkGenerator = new DirectLinkGenerator(this);
        }
        return linkGenerator;
    }

    /**
     * Appends a new line for the supplied message into the trace log.
     *
     * @param message The message to trace
     */
    public void trace(Object message) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(message);
        }
        if (isDiagnostics()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZones.UTC);
            diagnosticsLog
                .append(sdf.format(new Date()))
                .append(" [").append(Thread.currentThread().getId()).append("]\n")
                .append(message).append("\n\n");
        }
    }

    public String getDiagnosticsLog() {
        return null != diagnosticsLog ? diagnosticsLog.toString() : null;
    }

    /**
     * Gets a value indicating whether tracing is enabled either in the named logger instance or the drive-internal diagnostics log
     * generator.
     *
     * @return <code>true</code> if tracing is enabled, <code>false</code>, otherwise
     */
    public boolean isTraceEnabled() {
        return LOG.isTraceEnabled() || isDiagnostics();
    }

    private boolean isDiagnostics() {
        if (null == diagnostics) {
            Object parameter = getServerSession().getParameter("com.openexchange.drive.diagnostics");
            diagnostics = null != parameter && Boolean.class.isInstance(parameter) ? diagnostics = (Boolean)parameter : Boolean.FALSE;
            if (diagnostics.booleanValue()) {
                diagnosticsLog = new StringAllocator();
            }
        }
        return diagnostics.booleanValue();
    }

}
