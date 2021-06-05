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

import java.io.InputStream;
import java.util.Optional;
import org.json.JSONObject;
import com.openexchange.exception.OXException;

/**
 * {@link DataExportSink} - The export sink, which is used passed to providers to output their data and to signal changes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface DataExportSink {

    /**
     * Exports given item's data
     *
     * @param data The item's data
     * @param item The item
     * @return <code>true</code> if item's data has been successfully written to this sink;
     *         otherwise <code>false</code> if write attempt has been denied (typically because task has been canceled meanwhile)
     * @throws OXException If export fails
     */
    boolean export(InputStream data, Item item) throws OXException;

    /**
     * Exports given directory (marker).
     *
     * @param data The item's data
     * @param directory The directory
     * @return The actual directory path if directory has been successfully written to this sink;
     *         otherwise <code>null</code> if write attempt has been denied (typically because task has been canceled meanwhile)
     * @throws OXException If export fails
     */
    String export(Directory directory) throws OXException;

    /**
     * Finishes the export and closes resources.
     *
     * @return The optional file storage location if successfully finished; otherwise an empty instance if already finished
     * @throws OXException If finishing fails
     */
    Optional<String> finish() throws OXException;

    /**
     * Revokes (by deleting) any export data written to this sink and closes resources.
     *
     * @throws OXException If revoking fails
     */
    void revoke() throws OXException;

    /**
     * Adds specified message to task's report.
     *
     * @param message The message to add
     * @throws OXException If message cannot be added to report
     */
    void addToReport(Message message) throws OXException;

    /**
     * Sets given save-point.
     *
     * @param jSavePoint The save-point to set
     * @throws OXException If setting save-point fails
     * @throws IllegalArgumentException If save-point is <code>null</code>
     */
    void setSavePoint(JSONObject jSavePoint) throws OXException;

    /**
     * Attempts to increment the fail count for sink-associated work item.
     *
     * @return <code>true</code> if successfully incremented; otherwise <code>false</code> if max. fail count already reached
     * @throws OXException If operation fails
     */
    boolean incrementFailCount() throws OXException;
}
