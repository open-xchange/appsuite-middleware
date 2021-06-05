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

package com.openexchange.importexport.exporters.ical;

import java.io.OutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ICalExport}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public interface ICalExport {

    /**
     * Handles the export request
     *
     * @param session The session object
     * @param requestData The optional AJAX request data (in preference over output stream)
     * @param out The optional output stream
     * @param isSaveToDisk The value of the optional parameter isSaveToDisk
     * @param filename The file name
     * @return InputStream in requested format.
     * @throws OXException if export fails
     */
    SizedInputStream exportData(ServerSession session, AJAXRequestData requestData, OutputStream out, boolean isSaveToDisk, String filename) throws OXException;

     /**
     * Checks if the export is folder or batch based
     *
     * @param session The session object
     * @param out The output stream to write to
     * @return ThresholdFileHolder The file holder to export
     * @throws OXException if folder export fails
     */
    ThresholdFileHolder getExportDataSource(ServerSession session, OutputStream out) throws OXException;

}
