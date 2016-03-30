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

package com.openexchange.drive.json.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.json.DriveFieldMapper;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileMetadataAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileMetadataAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        try {
            /*
             * get parameters
             */
            String path = requestData.getParameter("path");
            if (Strings.isEmpty(path)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }
            String columnsValue = requestData.getParameter("columns");
            if (Strings.isEmpty(columnsValue)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("columns");
            }
            String[] splitted = Strings.splitByComma(columnsValue);
            int[] columnIDs = new int[splitted.length];
            for (int i = 0; i < splitted.length; i++) {
                try {
                    columnIDs[i] = Integer.parseInt(splitted[i]);
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("columns");
                }
            }
            DriveFileField[] fields = DriveFieldMapper.getInstance().getFields(columnIDs);
            List<FileVersion> fileVersions;
            Object data = requestData.getData();
            if (null != data) {
                /*
                 * get requested versions from body
                 */
                if (false == JSONArray.class.isInstance(data)) {
                    throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
                }
                fileVersions = JsonFileVersion.deserialize((JSONArray)data);
            } else if (requestData.containsParameter("name")) {
                /*
                 * get requested version from url
                 */
                String name = requestData.getParameter("name");
                if (Strings.isEmpty(name)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
                }
                String checksum = requestData.getParameter("checksum");
                if (Strings.isEmpty(checksum)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
                }
                fileVersions = new ArrayList<FileVersion>(1);
                fileVersions.add(new JsonFileVersion(checksum, name));
            } else {
                /*
                 * no specific versions specified
                 */
                fileVersions = null;
            }
            /*
             * get & return metadata as json
             */
            List<DriveFileMetadata> fileMetadata = getDriveService().getFileMetadata(session, path, fileVersions, Arrays.asList(fields));
            JSONArray jsonArray = DriveFieldMapper.getInstance().serialize(fileMetadata, fields, TimeZones.UTC, session.getServerSession());
            return new AJAXRequestResult(jsonArray, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
