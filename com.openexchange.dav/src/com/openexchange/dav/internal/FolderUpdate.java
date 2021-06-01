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

package com.openexchange.dav.internal;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.SetterAwareFolder;
import com.openexchange.folderstorage.UsedForSync;

/**
 * {@link FolderUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FolderUpdate extends AbstractFolder implements ParameterizedFolder, SetterAwareFolder {

    private static final long serialVersionUID = -367640273380922433L;

    private final Map<FolderField, FolderProperty> properties;

    private boolean containsSubscribed;
    private boolean containsUsedForSync;

    /**
     * Initializes a new {@link FolderUpdate}.
     */
    public FolderUpdate() {
        super();
        subscribed = true;
        usedForSync = UsedForSync.DEFAULT;
        this.properties = new HashMap<FolderField, FolderProperty>();
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

    @Override
    public void setProperty(FolderField name, Object value) {
        if (null == value) {
            properties.remove(name);
        } else {
            properties.put(name, new FolderProperty(name.getName(), value));
        }
    }

    @Override
    public Map<FolderField, FolderProperty> getProperties() {
        return properties;
    }

    @Override
    public void setSubscribed(boolean subscribed) {
        super.setSubscribed(subscribed);
        containsSubscribed = true;
    }

    @Override
    public boolean containsSubscribed() {
        return containsSubscribed;
    }
    
    @Override
    public void setUsedForSync(UsedForSync usedForSync) {
        super.setUsedForSync(usedForSync);
        containsUsedForSync=true;
    }

    @Override
    public boolean containsUsedForSync() {
        return containsUsedForSync;
    }

}
