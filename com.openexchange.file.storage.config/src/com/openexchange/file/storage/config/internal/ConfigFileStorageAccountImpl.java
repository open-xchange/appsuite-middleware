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

package com.openexchange.file.storage.config.internal;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.config.ConfigFileStorageAccount;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;

/**
 * {@link ConfigFileStorageAccountImpl} - The configuration {@link FileStorageAccount} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class ConfigFileStorageAccountImpl extends DefaultFileStorageAccount implements ConfigFileStorageAccount {

    private static final long serialVersionUID = -1711683802127778909L;

    /**
     * Initializes a new {@link ConfigFileStorageAccountImpl}.
     */
    public ConfigFileStorageAccountImpl() {
        super();
    }

    @Override
    public Object clone() {
        try {
            final ConfigFileStorageAccountImpl clone = (ConfigFileStorageAccountImpl) super.clone();
            clone.setFileStorageService(null);
            final Map<String, Object> thismap = this.configuration;
            final Map<String, Object> clonedConfig = null == thismap ? null : new HashMap<String, Object>(thismap);
            clone.configuration = clonedConfig;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Clone not supported although Cloneable is implemented.");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256).append(ConfigFileStorageAccountImpl.class.getSimpleName());
        sb.append(" ( serviceId=").append(this.serviceId);
        sb.append(", configuration=").append(null == configuration ? "<empty>" : configuration.toString()).append(" )").toString();
        return sb.toString();
    }

}
