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

package com.openexchange.file.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link AbstractRootFolder} - Abstract root folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractRootFolder implements FileStorageFolder {

    protected int createdBy;

    protected int modifiedBy;

    /**
     * Initializes a new {@link AbstractRootFolder}.
     */
    protected AbstractRootFolder() {
        super();
    }

    @Override
    public Set<String> getCapabilities() {
        return Collections.emptySet();
    }

    @Override
    public String getId() {
        return FileStorageFolder.ROOT_FULLNAME;
    }

    @Override
    public abstract String getName();

    @Override
    public String getLocalizedName(Locale locale) {
        return null;
    }

    @Override
    public FileStoragePermission getOwnPermission() {
        return DefaultFileStoragePermission.newInstance();
    }

    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public List<FileStoragePermission> getPermissions() {
        return Arrays.asList((FileStoragePermission) DefaultFileStoragePermission.newInstance());
    }

    @Override
    public boolean hasSubfolders() {
        return true;
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return false;
    }

    @Override
    public boolean isSubscribed() {
        return true;
    }

    @Override
    public Date getCreationDate() {
        return new Date();
    }

    @Override
    public Date getLastModifiedDate() {
        return new Date();
    }

    @Override
    public boolean isHoldsFolders() {
        return true;
    }

    @Override
    public boolean isHoldsFiles() {
        return true;
    }

    @Override
    public boolean isRootFolder() {
        return true;
    }

    @Override
    public boolean isDefaultFolder() {
        return false;
    }

    @Override
    public abstract int getFileCount();

    @Override
    public Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getMeta() {
        return Collections.emptyMap();
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    @Override
    public int getModifiedBy() {
        return this.modifiedBy;
    }

}
