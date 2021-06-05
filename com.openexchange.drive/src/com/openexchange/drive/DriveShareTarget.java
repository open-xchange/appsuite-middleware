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

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;

/**
 * {@link DriveShareTarget}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class DriveShareTarget extends ShareTarget {

    private static final long serialVersionUID = -8685321482126335866L;
    private static final int MODULE_ID = FolderObject.INFOSTORE;

    private String name;
    private String path;
    private String checksum;

    /**
     * Initializes a new {@link DriveShareTarget}.
     */
    public DriveShareTarget() {
        super();
        setModule(MODULE_ID);
    }

    /**
     * Initializes a new {@link DriveShareTarget}.
     *
     * @param shareTarget The parent share target
     */
    public DriveShareTarget(ShareTarget shareTarget) {
        super(shareTarget);
        setModule(MODULE_ID);
    }

    /**
     * Initializes a new {@link DriveShareTarget}.
     *
     * @param shareTarget The parent share target
     * @param path The drive path
     * @param checksum The checksum
     */
    public DriveShareTarget(ShareTarget shareTarget, String path, String checksum) {
        this(shareTarget, path, null, checksum);
    }

    /**
     * Initializes a new {@link DriveShareTarget}.
     *
     * @param shareTarget The parent share target
     * @param path The drive path
     * @param name The filename, or <code>null</code> if this is a folder target
     * @param checksum The checksum
     */
    public DriveShareTarget(ShareTarget shareTarget, String path, String name, String checksum) {
        this(shareTarget);
        this.path = path;
        this.name = name;
        this.checksum = checksum;
    }

    /**
     * Gets a value indicating whether this share target points to a folder, i.e. there is no item defined, or not.
     *
     * @return <code>true</code> if this target points to a folder, <code>false</code>, otherwise
     */
    @Override
    public boolean isFolder() {
        return null == name;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDrivePath() {
        return path;
    }

    public void setDrivePath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DriveShareTarget other = (DriveShareTarget) obj;
        if (checksum == null) {
            if (other.checksum != null)
                return false;
        } else if (!checksum.equals(other.checksum))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

}
