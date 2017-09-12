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

import java.util.List;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.helpers.ExportFileNameCreator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractICalExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalExporter implements Exporter {

    @Override
    public String getFolderExportFileName(ServerSession session, String folder, String extension) {
        return ExportFileNameCreator.createFolderExportFileName(session, folder, extension);
    }

    @Override
    public String getBatchExportFileName(ServerSession session, Map<String, List<String>> batchIds, String extension) {
        return ExportFileNameCreator.createBatchExportFileName(session, batchIds, extension);
    }

    public String appendFileNameParameter(AJAXRequestData requestData, String fileName) {
        return ExportFileNameCreator.appendFileNameParameter(requestData, fileName);
    }

    /**
     * Gets a folder by it's identifier.
     *
     * @param session The session
     * @param folderID The folder identifier
     * @return The folder
     */
    protected static FolderObject getFolder(ServerSession session, String folderID) throws OXException {
        try {
            return new OXFolderAccess(session.getContext()).getFolderObject(Integer.parseInt(folderID));
        } catch (OXException e) {
            throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folderID);
        } catch (NumberFormatException e) {
            throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folderID);
        }
    }

    protected boolean isSaveToDisk(final Map<String, Object> optionalParams) {
        if (null == optionalParams) {
            return false;
        }
        final Object object = optionalParams.get("__saveToDisk");
        if (null == object) {
            return false;
        }
        return (object instanceof Boolean ? ((Boolean) object).booleanValue() : Boolean.parseBoolean(object.toString().trim()));
    }

}
