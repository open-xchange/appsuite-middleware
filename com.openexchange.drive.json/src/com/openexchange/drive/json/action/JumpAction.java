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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DirectoryMetadata;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tokenlogin.TokenLoginService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link JumpAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class JumpAction extends AbstractDriveAction {

    private final TokenLoginService tokenLoginService;

    public JumpAction(TokenLoginService service) {
        super();
        this.tokenLoginService = service;
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        DriveService driveService = getDriveService();
        try {
            /*
             * get parameters
             */
            String path = requestData.getParameter("path");
            if (Strings.isEmpty(path)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }
            String method = requestData.getParameter("method");
            if (Strings.isEmpty(method)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("method");
            }
            String token = tokenLoginService.acquireToken(session.getServerSession());
            String link = null;
            String name = requestData.getParameter("name");
            if (Strings.isEmpty(name)) {
                DirectoryMetadata directoryMetadata = driveService.getDirectoryMetadata(session, path);
                link = directoryMetadata.getDirectLink();
            } else {
                String checksum = requestData.getParameter("checksum");
                if (Strings.isEmpty(checksum)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
                }
                List<FileVersion> fileVersions = new ArrayList<FileVersion>(1);
                fileVersions.add(new JsonFileVersion(checksum, name));
                List<DriveFileMetadata> metadata = driveService.getFileMetadata(session, path, fileVersions, null);
                for (DriveFileMetadata meta : metadata) {
                    link = meta.getDirectLink();
                }
            }
            /*
             * get & return metadata as json
             */
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("link", link);
            jsonObject.put("token", token);
            return new AJAXRequestResult(jsonObject, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
