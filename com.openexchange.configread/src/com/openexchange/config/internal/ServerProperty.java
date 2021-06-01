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

package com.openexchange.config.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;

/**
 * {@link ServerProperty}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ServerProperty implements BasicProperty {

    private final String value;
    private final Map<String, String> metadata;

    /**
     * Initializes a new {@link ServerProperty}.
     *
     * @param value The property's value
     * @param metadata The optional metadata of the property
     */
    public ServerProperty(String value, Map<String, String> metadata) {
        super();
        this.value = value;
        this.metadata = metadata;
    }

    /**
     * Gets the metadata reference.
     *
     * @return The metadata reference
     */
    Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public String get(String metadataName) {
        return null == metadata ? null : metadata.get(metadataName);
    }

    @Override
    public boolean isDefined() {
        return null != value;
    }

    /**
     * Unsupported.
     */
    @Override
    public void set(String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create("", ConfigViewScope.SERVER.getScopeName());
    }

    /**
     * Unsupported.
     */
    @Override
    public void set(String metadataName, String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, ConfigViewScope.SERVER.getScopeName());
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return new ArrayList<String>(metadata.keySet());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("ServerProperty [");
        if (value != null) {
            builder.append("value=").append(value).append(", ");
        }
        builder.append("defined=").append(isDefined()).append(", ");
        if (metadata != null) {
            builder.append("metadata=").append(metadata);
        }
        builder.append("]");
        return builder.toString();
    }

}
