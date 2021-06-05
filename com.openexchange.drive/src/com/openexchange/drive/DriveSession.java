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

package com.openexchange.drive;

import java.util.List;
import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.session.Session;
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
    static final String PARAMETER_PUSH_TOKEN = Session.PARAM_PUSH_TOKEN;

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
     * Gets a value indicating whether the current quota and usage should be included in the response or not, when applicable.
     *
     * @return <code>true</code> if quota information should be included, <code>false</code>, otherwise
     */
    boolean isIncludeQuota();

    /**
     * Gets the desired mode to use for metadata synchronization.
     *
     * @return The <code>.drive-meta</code>-mode
     */
    DriveMetaMode getDriveMetaMode();

}
