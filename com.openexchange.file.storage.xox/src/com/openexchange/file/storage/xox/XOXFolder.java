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

package com.openexchange.file.storage.xox;

import com.openexchange.file.storage.CacheAware;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link XOXFolder} - A folder shared from another OX instance
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXFolder extends DefaultFileStorageFolder implements TypeAware, CacheAware {

    private FileStorageFolderType type;
    private boolean cacheable;

    /**
     * Initializes a new {@link XOXFolder}.
     */
    public XOXFolder() {
        super();
    }

    @Override
    public boolean cacheable() {
        return cacheable;
    }

    /**
     * Sets if the folder is cacheable or not.
     * 
     * @param cacheable <code>true</code> if cacheable, <code>false</code>, otherwise
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type to set
     * @return This folder with type applied
     */
    public XOXFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

}
