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

package com.openexchange.importexport;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.tools.session.ServerSession;

/**
 * Defines a class able to export a certain type of PIM objects (contacts, appointments, and tasks) as a certain format.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a> - export file name, batch data
 * @see com.openexchange.groupware.Types
 */
public interface Exporter {

    /**
     * The default character set used to generate output.
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * Checks if the given folder can be exported in the given format
     *
     * @param session The session object to be able to check permissions.
     * @param format Format the exported data is supposed to be in
     * @param folder Folder that should be exported. Note: A folder can only contain data of one type
     * @param optionalParams Parameters that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
     * @return true, if the given folders can be exported in the given format; false otherwise
     * @throws OXException if check fails
     */
    boolean canExport(ServerSession session, Format format, String folder, Map<String, Object> optionalParams) throws OXException;

    /**
     * Checks if the given batch data can be exported in the given format
     *
     * @param session The session object to be able to check permissions.
     * @param format Format the exported data is supposed to be in
     * @param batchIds Batch data that should be exported. Note: A batch can only contain data of one type
     * @param optionalParams Parameters that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
     * @return true, if the given folders and objects can be exported in the given format; false otherwise
     * @throws OXException if check fails
     */
    boolean canExportBatch(ServerSession session, Format format, Map.Entry<String, List<String>> batchIds, Map<String, Object> optionalParams) throws OXException;

    /**
     * Exports the data of a given folder
     *
     * @param session The session object to be able to check permissions.
     * @param format Format the returned InputStream should be in.
     * @param folder Folder that should be exported. Note: A folder can only contain data of one type.
     * @param fieldsToBeExported A list of fields of that folder that should be exported. Convention: If the list is empty, all fields are exported.
     * @param optionalParams Parameters that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
     * @return InputStream in requested format.
     * @throws OXException if export fails
     */
    SizedInputStream exportFolderData(ServerSession session, Format format, String folder, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException;

    /**
     * Exports the data of the given folders and objects
     *
     * @param session The session object to be able to check permissions.
     * @param format Format the returned InputStream should be in.
     * @param batchIds Identifiers of multiple entries in different folders
     * @param fieldsToBeExported A list of fields of that folder that should be exported. Convention: If the list is empty, all fields are exported.
     * @param optionalParams Parameters that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
     * @return InputStream in requested format.
     * @throws OXException if export fails
     */
    SizedInputStream exportBatchData(ServerSession session, Format format, Map<String, List<String>> batchIds, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException;

    /**
     * Creates a proper export file name based on the folder to export
     *
     * @param session The session object to be able to check permissions.
     * @param folder The folder to name the export file after.
     * @param extension The file name extension.
     * @return String the name of the export file.
     */
    String getFolderExportFileName(ServerSession session, String folder, String extension);

    /**
     * Creates a proper export file name based on the batch of ids to export
     *
     * @param session The session object to be able to check permissions.
     * @param batchIds The Identifiers which determine the export file name.
     * @param extension The file name extension.
     * @return String the name of the export file.
     */
    String getBatchExportFileName(ServerSession session, Map<String, List<String>> batchIds, String extension);

}
