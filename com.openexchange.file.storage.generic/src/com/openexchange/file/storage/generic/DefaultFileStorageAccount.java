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

package com.openexchange.file.storage.generic;

import java.util.Map;
import org.json.JSONObject;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.ServiceAware;

/**
 * {@link DefaultFileStorageAccount} - The default {@link FileStorageAccount} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class DefaultFileStorageAccount implements FileStorageAccount, ServiceAware {

    private static final long serialVersionUID = -8295765793020470243L;

    /**
     * The account's configuration.
     */
    protected Map<String, Object> configuration;

    /**
     * The account's display name.
     */
    protected String displayName;

    /**
     * The account identifier.
     */
    protected String id;

    /**
     * The file storage service identifier.
     */
    protected String serviceId;

    /**
     * The transient reference to file storage service.
     */
    protected transient FileStorageService fsService;

    /**
     * The account's meta data
     */
    protected JSONObject metaData = new JSONObject();

    /**
     * Initializes a new {@link DefaultFileStorageAccount}.
     */
    public DefaultFileStorageAccount() {
        super();
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public FileStorageService getFileStorageService() {
        return fsService;
    }

    @Override
    public JSONObject getMetadata() {
        return metaData;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service identifier
     *
     * @param serviceId The service identifier to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration The configuration to set
     */
    public void setConfiguration(final Map<String, Object> configuration) {
        this.configuration = configuration; // Collections.unmodifiableMap(configuration);
    }

    /**
     * Sets the display name.
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the ID.
     *
     * @param id The ID to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the file storage service.
     *
     * @param fsService The file storage service to set
     */
    public void setFileStorageService(final FileStorageService fsService) {
        this.fsService = fsService;
        serviceId = null == fsService ? null : fsService.getId();
    }

    /**
     * Sets the meta data
     *
     * @param metaData The meta data to set
     */
    public void setMetaData(JSONObject metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder(64);
        stringBuilder.append("DefaultFileStorageAccount ( configuration = ");
        stringBuilder.append(configuration);
        stringBuilder.append(", displayName = ");
        stringBuilder.append(displayName);
        stringBuilder.append(", id = ");
        stringBuilder.append(id);
        stringBuilder.append(", fsService = ");
        stringBuilder.append(fsService);
        stringBuilder.append(" )");
        return stringBuilder.toString();
    }

}
