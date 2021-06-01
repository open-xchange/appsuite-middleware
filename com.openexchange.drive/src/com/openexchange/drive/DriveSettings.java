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

import java.util.Map;
import java.util.Set;

/**
 * {@link DriveSettings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveSettings {

    private String helpLink;
    private String serverVersion;
    private DriveQuota quota;
    private String supportedApiVersion;
    private String minApiVersion;
    private Map<String, String> localizedFolders;
    private Set<String> capabilities;
    private Long minUploadChunk;
    private int minSearchChars;
    private boolean hasTrashFolder;
    private String pathToRoot;
    private int maxConcurrentSyncFiles;

    /**
     * Initializes a new {@link DriveSettings}.
     */
    public DriveSettings() {
        super();
    }

    /**
     * Gets the helpLink
     *
     * @return The helpLink
     */
    public String getHelpLink() {
        return helpLink;
    }

    /**
     * Sets the helpLink
     *
     * @param helpLink The helpLink to set
     */
    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }

    /**
     * Gets the quota
     *
     * @return The quota
     */
    public DriveQuota getQuota() {
        return quota;
    }

    /**
     * Sets the quota
     *
     * @param quota The quota to set
     */
    public void setQuota(DriveQuota quota) {
        this.quota = quota;
    }

    /**
     * Sets the server version
     *
     * @param serverVersion The server version to set
     */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * Gets the server version
     *
     * @return The server version
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * Gets the supportedApiVersion
     *
     * @return The supportedApiVersion
     */
    public String getSupportedApiVersion() {
        return supportedApiVersion;
    }

    /**
     * Sets the supportedApiVersion
     *
     * @param supportedApiVersion The supportedApiVersion to set
     */
    public void setSupportedApiVersion(String supportedApiVersion) {
        this.supportedApiVersion = supportedApiVersion;
    }

    /**
     * Gets the minApiVersion
     *
     * @return The minApiVersion
     */
    public String getMinApiVersion() {
        return minApiVersion;
    }

    /**
     * Sets the minApiVersion
     *
     * @param minApiVersion The minApiVersion to set
     */
    public void setMinApiVersion(String minApiVersion) {
        this.minApiVersion = minApiVersion;
    }

    /**
     * Gets the localizedFolders
     *
     * @return The localizedFolders
     */
    public Map<String, String> getLocalizedFolders() {
        return localizedFolders;
    }

    /**
     * Sets the localizedFolders
     *
     * @param localizedFolders The localizedFolders to set
     */
    public void setLocalizedFolders(Map<String, String> localizedFolders) {
        this.localizedFolders = localizedFolders;
    }

    /**
     * Gets the capabilities
     *
     * @return The capabilities
     */
    public Set<String> getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the capabilities
     *
     * @param capabilities The capabilities to set
     */
    public void setCapabilities(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Gets the minUploadChunk
     *
     * @return The minUploadChunk
     */
    public Long getMinUploadChunk() {
        return minUploadChunk;
    }

    /**
     * Sets the minUploadChunk
     *
     * @param minUploadChunk The minUploadChunk to set
     */
    public void setMinUploadChunk(Long minUploadChunk) {
        this.minUploadChunk = minUploadChunk;
    }

    /**
     * Gets the minSearchChars
     *
     * @return The minSearchChars
     */
    public int getMinSearchChars() {
        return minSearchChars;
    }

    /**
     * Sets the minSearchChars
     *
     * @param minSearchChars The minSearchChars to set
     */
    public void setMinSearchChars(int minSearchChars) {
        this.minSearchChars = minSearchChars;
    }

    /**
     * Gets the hasTrashFolder
     *
     * @return The hasTrashFolder
     */
    public boolean hasTrashFolder() {
        return hasTrashFolder;
    }

    /**
     * Sets the hasTrashFolder
     *
     * @param hasTrashFolder The hasTrashFolder to set
     */
    public void setHasTrashFolder(boolean hasTrashFolder) {
        this.hasTrashFolder = hasTrashFolder;
    }

    /**
     * Gets the path from driveRootFolder to internal 'real' root folder
     *
     * @return The path
     */
    public String getPathToRoot() {
        return pathToRoot;
    }

    /**
     * Sets the path from driveRootFolder to internal 'real' root folder
     *
     * @param pathToRoot The path to set
     */
    public void setPathToRoot(String pathToRoot) {
        this.pathToRoot = pathToRoot;
    }

    /**
     * Gets the maxConcurrentSyncFiles
     *
     * @return The maxConcurrentSyncFiles
     */
    public int getMaxConcurrentSyncFiles() {
        return maxConcurrentSyncFiles;
    }

    /**
     * Sets the maxConcurrentSyncFiles
     *
     * @param maxConcurrentSyncFiles The maxConcurrentSyncFiles to set
     */
    public void setMaxConcurrentSyncFiles(int maxConcurrentSyncFiles) {
        this.maxConcurrentSyncFiles = maxConcurrentSyncFiles;
    }

}
