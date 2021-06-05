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

package com.openexchange.gdpr.dataexport;

import java.util.UUID;
import org.json.JSONObject;

/**
 * {@link DataExportWorkItem} - Represents an individual work item for a certain module which is processed by executing a data export
 * task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportWorkItem {

    private UUID uuid;
    private String moduleId;
    private JSONObject savePoint;
    private String fileStorageLocation;
    private DataExportStatus status;
    private JSONObject info;

    /**
     * Initializes a new {@link DataExportWorkItem}.
     */
    public DataExportWorkItem() {
        super();
    }

    /**
     * Gets the identifier of this work item.
     *
     * @return The work item identifier
     */
    public UUID getId() {
        return uuid;
    }

    /**
     * Sets the identifier of this work item.
     *
     * @param id The work item identifier
     */
    public void setId(UUID id) {
        this.uuid = id;
    }

    /**
     * Gets the module identifier; e.g. <code>"mail"</code>
     * <p>
     * Only consists of ASCII characters and must not be longer than 32 characters.
     *
     * @return The module identifier
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Sets the module identifier; e.g. <code>"mail"</code>
     * <p>
     * Only consists of ASCII characters and must not be longer than 32 characters.
     *
     * @param moduleId The module identifier
     */
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * Gets the save point data.
     *
     * @return The save point data
     */
    public JSONObject getSavePoint() {
        return savePoint;
    }

    /**
     * Sets the save point data
     *
     * @param savePoint The save point data
     */
    public void setSavePoint(JSONObject savePoint) {
        this.savePoint = savePoint;
    }

    /**
     * Gets the location in the parental file storage to which this work item's artifacts are persisted.
     *
     * @return The file storage location
     */
    public String getFileStorageLocation() {
        return fileStorageLocation;
    }

    /**
     * Sets the location in the parental file storage to which this work item's artifacts are persisted.
     *
     * @param fileStorageLocation The file storage location
     */
    public void setFileStorageLocation(String fileStorageLocation) {
        this.fileStorageLocation = fileStorageLocation;
    }

    /**
     * Gets the status.
     *
     * @return The status
     */
    public DataExportStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param done The status
     */
    public void setStatus(DataExportStatus status) {
        this.status = status;
    }

    /**
     * Gets the info data.
     *
     * @return The info data
     */
    public JSONObject getInfo() {
        return info;
    }

    /**
     * Sets the info data.
     *
     * @param info The info data
     */
    public void setInfo(JSONObject info) {
        this.info = info;
    }

}
