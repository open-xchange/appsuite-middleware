/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
