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

package com.openexchange.importexport.exporters;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.formats.Format;
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
