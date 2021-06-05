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

package com.openexchange.drive.json.internal;

import java.util.List;
import java.util.Locale;
import com.openexchange.drive.DirectoryPattern;
import com.openexchange.drive.DriveClientType;
import com.openexchange.drive.DriveClientVersion;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveMetaMode;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.FilePattern;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link DefaultDriveSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultDriveSession implements DriveSession {

    private final String rootFolderID;
    private final ServerSession session;
    private final int apiVersion;
    private final HostData hostData;
    private final DriveClientVersion clientVersion;
    private String deviceName;
    private final Locale localeOverride;
    private Boolean diagnostics;
    private List<DriveFileField> fields;
    private List<FilePattern> fileExclusions;
    private List<DirectoryPattern> directoryExclusions;
    private String driveMeta;
    private boolean includeQuota;

    /**
     * Initializes a new {@link DefaultDriveSession}.
     *
     * @param session The server session
     * @param rootFolderID The root folder ID
     * @param hostData The host data as extracted from the corresponding http request
     * @param apiVersion The API version as set by the client, or <code>0</code> if not set
     * @param clientVersion The client version as set by the client, or {@link DriveClientVersion#VERSION_0} if not set
     * @param localeOverride The locale string if overridden by client, or <code>null</code> to fall back to the user's locale
     */
    public DefaultDriveSession(ServerSession session, String rootFolderID, HostData hostData, int apiVersion, DriveClientVersion clientVersion, Locale localeOverride) {
        super();
        this.session = session;
        this.rootFolderID = rootFolderID;
        this.hostData = hostData;
        this.apiVersion = apiVersion;
        this.clientVersion = clientVersion;
        this.localeOverride = localeOverride;
    }

    /**
     * Sets the diagnostics
     *
     * @param diagnostics The diagnostics to set
     */
    public void setDiagnostics(Boolean diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * Sets the deviceName
     *
     * @param deviceName The deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Sets the fields
     *
     * @param fields The fields to set
     */
    public void setFields(List<DriveFileField> fields) {
        this.fields = fields;
    }

    /**
     * Sets the fileExclusions
     *
     * @param fileExclusions The fileExclusions to set
     */
    public void setFileExclusions(List<FilePattern> fileExclusions) {
        this.fileExclusions = fileExclusions;
    }

    /**
     * Sets the directoryExclusions
     *
     * @param directoryExclusions The directoryExclusions to set
     */
    public void setDirectoryExclusions(List<DirectoryPattern> directoryExclusions) {
        this.directoryExclusions = directoryExclusions;
    }

    /**
     * Configures the .drive-meta mode, independently of the used API version.
     *
     * @param driveMeta <code>true</code> to force (default) drive meta synchronization, <code>inline</code> to force inline drive meta
     *        synchronization, <code>false</code> to forcibly disable it, or <code>null</code> to decide based on the API version
     */
    public void setDriveMeta(String driveMeta) {
        this.driveMeta = driveMeta;
    }

    /**
     * Configures to include current quota and usage in the response or not, when applicable.
     *
     * @param includeQuota <code>true</code> if quota information should be included, <code>false</code>, otherwise
     */
    public void setIncludeQuota(boolean includeQuota) {
        this.includeQuota = includeQuota;
    }

    @Override
    public String getRootFolderID() {
        return rootFolderID;
    }

    @Override
    public ServerSession getServerSession() {
        return session;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public Boolean isDiagnostics() {
        return diagnostics;
    }

    @Override
    public Locale getLocale() {
        if (null != localeOverride) {
            return localeOverride;
        }
        Locale locale = null;
        if (null != session) {
            User user = session.getUser();
            if (null != user) {
                locale = user.getLocale();
            }
        }
        return null != locale ? locale : Locale.US;
    }

    @Override
    public HostData getHostData() {
        return hostData;
    }

    @Override
    public List<DriveFileField> getFields() {
        return fields;
    }

    @Override
    public int getApiVersion() {
        return apiVersion;
    }

    @Override
    public DriveClientVersion getClientVersion() {
        return clientVersion;
    }

    @Override
    public DriveClientType getClientType() {
        return DriveClientType.parse(null != session ? session.getClient() : null);
    }

    @Override
    public List<DirectoryPattern> getDirectoryExclusions() {
        return directoryExclusions;
    }

    @Override
    public List<FilePattern> getFileExclusions() {
        return fileExclusions;
    }

    @Override
    public boolean useDriveMeta() {
        if (Strings.isEmpty(driveMeta)) {
            return 3 <= apiVersion;
        }
        return "inline".equalsIgnoreCase(driveMeta) || Boolean.parseBoolean(driveMeta);
    }

    @Override
    public DriveMetaMode getDriveMetaMode() {
        if ("inline".equalsIgnoreCase(driveMeta)) {
            return DriveMetaMode.INLINE;
        }
        return useDriveMeta() ? DriveMetaMode.DEFAULT : DriveMetaMode.DISABLED;
    }

    @Override
    public boolean isIncludeQuota() {
        return includeQuota;
    }

    @Override
    public String toString() {
        return "DriveSession [sessionID=" + session.getSessionID() + ", rootFolderID=" + rootFolderID + ", contextID=" +
            session.getContextId() + ", clientVersion=" + clientVersion + ", deviceName=" + deviceName + ", apiVersion=" +
            apiVersion + ", diagnostics=" + diagnostics + "]";
    }

}
