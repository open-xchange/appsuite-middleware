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

package com.openexchange.drive;

import java.util.List;
import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DriveSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveSession {

    /**
     * The session parameter used to hold the client's push token
     */
    static final String PARAMETER_PUSH_TOKEN = "com.openexchange.drive.pushToken";

    /**
     * Gets the underlying server session.
     *
     * @return The server session
     */
    ServerSession getServerSession();

    /**
     * Get the identifier of the referenced root folder on the server.
     *
     * @return The root folder ID.
     */
    String getRootFolderID();

    /**
     * Gets a friendly name identifying the client device from a user's point of view, e.g. "My Tablet PC".
     *
     * @return The devie name, or <code>null</code> if not defined
     */
    String getDeviceName();

    /**
     * Gets a value indicating whether the diagnostics trace is requested from the client or not.
     *
     * @return <code>Boolean.TRUE</code> if tracing is enabled, <code>null</code> or <code>Boolean.FALSE</code>, othwerwise.
     */
    Boolean isDiagnostics();

    /**
     * Gets the locale of the session's user.
     *
     * @return The locale
     */
    Locale getLocale();

    /**
     * Gets the host data of the underlying request.
     *
     * @return The hostname
     */
    HostData getHostData();

    /**
     * Gets the file metadata fields relevant for the client.
     *
     * @return The file metadata fields, or <code>null</code> if not specified
     */
    List<DriveFileField> getFields();

    /**
     * Gets the API version targeted by the client
     *
     * @return The API version, or <code>0</code> if using the initial version
     */
    int getApiVersion();

    /**
     * Gets the client version.
     *
     * @return The client version
     */
    DriveClientVersion getClientVersion();

    /**
     * Gets the client type.
     *
     * @return The client type, or {@link DriveClientType#UNKNOWN} if not known
     */
    DriveClientType getClientType();

    /**
     * Gets a list of directory patterns matching those directory versions that should be excluded from synchronization.
     *
     * @return The directory patterns, or <code>null</code> if there are none
     */
    List<DirectoryPattern> getDirectoryExclusions();

    /**
     * Gets a list of file patterns matching those file versions that should be excluded from synchronization.
     *
     * @return The file patterns, or <code>null</code> if there are none
     */
    List<FilePattern> getFileExclusions();

    /**
     * Gets a value indicating whether this drive session makes use of inline metadata synchronization via <code>.drive-meta</code> files
     * or not, based on the requested API version.
     *
     * @return <code>true</code> if drive metadata is synchronized, <code>false</code>, otherwise
     */
    boolean useDriveMeta();

    /**
     * Gets the desired mode to use for metadata synchronization.
     *
     * @return The <code>.drive-meta</code>-mode
     */
    DriveMetaMode getDriveMetaMode();

}
