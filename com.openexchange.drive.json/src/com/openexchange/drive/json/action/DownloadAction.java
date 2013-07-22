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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.JsonFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@DispatcherNotes(defaultFormat = "file")
public class DownloadAction extends AbstractDriveAction {

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            /*
             * get parameters
             */
            String rootFolderID = requestData.getParameter("root");
            if (Strings.isEmpty(rootFolderID)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
            }
            String path = requestData.getParameter("path");
            if (Strings.isEmpty(path)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }
            String name = requestData.getParameter("name");
            if (Strings.isEmpty(name)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("name");
            }
            String checksum = requestData.getParameter("checksum");
            if (Strings.isEmpty(checksum)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
            }
            long length = -1;
            if (requestData.containsParameter("length")) {
                length = requestData.getParameter("length", Long.class).longValue();
            }
            long offset = 0;
            if (requestData.containsParameter("offset")) {
                offset = requestData.getParameter("offset", Long.class).longValue();
            }
            /*
             * get data
             */
            DriveService driveService = Services.getService(DriveService.class, true);
            IFileHolder fileHolder = driveService.download(session, rootFolderID, path, new JsonFileVersion(checksum, name), offset, length);
            if (null == fileHolder) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(name, checksum, path);
            }
            /*
             * return file result
             */
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
//            requestResult.setType(ResultType.DIRECT);//TODO: this leads to 0 bytes?
            return requestResult;
        } catch (OXException e) {
            /*
             * indicate error by setting HTTP status code
             */
            throw getHttpError(e);
        }
    }

    private static OXException getHttpError(OXException e) throws OXException {
        int status;
        if (DriveExceptionCodes.FILEVERSION_NOT_FOUND.equals(e) || DriveExceptionCodes.FILE_NOT_FOUND.equals(e) ||
            DriveExceptionCodes.PATH_NOT_FOUND.equals(e) || "FLS-017".equals(e.getErrorCode())) {
            status = HttpServletResponse.SC_NOT_FOUND;
        } else if (DriveExceptionCodes.INVALID_FILE_OFFSET.equals(e) || "FLS-018".equals(e.getErrorCode())
            || "FLS-019".equals(e.getErrorCode()) || "FLS-020".equals(e.getErrorCode())) {
            status = HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE;
        } else if (AjaxExceptionCodes.MISSING_PARAMETER.equals(e)) {
            status = HttpServletResponse.SC_BAD_REQUEST;
        } else if (OXException.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            status = HttpServletResponse.SC_FORBIDDEN;
        } else if (OXException.CATEGORY_CONFLICT.equals(e.getCategory())) {
            status = HttpServletResponse.SC_CONFLICT;
        } else if (OXException.CATEGORY_USER_INPUT.equals(e.getCategory())) {
            status = HttpServletResponse.SC_BAD_REQUEST;
        } else {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(status), e.getSoleMessage());
    }

}
