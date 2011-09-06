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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.filemanagement.json.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXFile;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NewAction implements AJAXActionService {

    private final ServiceLookup services;

    public NewAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        if (!request.hasUploads()) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Not an upload request.");
        }
        final UploadEvent upload = request.getUploadEvent();
        final String moduleParam = request.getParameter(AJAXFile.PARAMETER_MODULE);
        if (moduleParam == null) {
            throw UploadException.UploadCode.MISSING_PARAM.create(AJAXFile.PARAMETER_MODULE);
        }
        final String fileTypeFilter = request.getParameter(AJAXFile.PARAMETER_TYPE);
        if (fileTypeFilter == null) {
            throw UploadException.UploadCode.MISSING_PARAM.create(AJAXFile.PARAMETER_TYPE);
        }
        /*
         * Iterate uploaded files
         */
        final JSONArray jArray = new JSONArray();
        final ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
        for (final UploadFile uploadFile : upload.getUploadFiles()) {
            /*
             * Check file item's content type
             */
            final ContentType ct = new ContentType(uploadFile.getContentType());
            if (!checkFileType(fileTypeFilter, ct)) {
                throw UploadException.UploadCode.INVALID_FILE_TYPE.create("new", uploadFile.getContentType(), fileTypeFilter);
            }
            jArray.put(processFileItem(uploadFile, management));
        }
        return new AJAXRequestResult(jArray, "json");
    }

    private static String processFileItem(final UploadFile fileItem, final ManagedFileManagement management) throws OXException {
        try {
            final ManagedFile managedFile = management.createManagedFile(new FileInputStream(fileItem.getTmpFile()));
            managedFile.setFileName(fileItem.getPreparedFileName());
            managedFile.setContentType(fileItem.getContentType());
            managedFile.setSize(fileItem.getSize());
            return managedFile.getID();
        } catch (final FileNotFoundException e) {
            throw ManagedFileExceptionErrorMessage.FILE_NOT_FOUND.create(e, e.getMessage());
        }
    }

    private static final String FILE_TYPE_ALL = "file";

    private static final String FILE_TYPE_TEXT = "text";

    private static final String FILE_TYPE_MEDIA = "media";

    private static final String FILE_TYPE_IMAGE = "image";

    private static final String FILE_TYPE_AUDIO = "audio";

    private static final String FILE_TYPE_VIDEO = "video";

    private static final String FILE_TYPE_APPLICATION = "application";

    private static boolean checkFileType(final String filter, final ContentType fileContentType) {
        if (FILE_TYPE_ALL.equalsIgnoreCase(filter)) {
            return true;
        } else if (FILE_TYPE_TEXT.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("text/");
        } else if (FILE_TYPE_MEDIA.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("image/") || fileContentType.startsWith("audio/") || fileContentType.startsWith("video/");
        } else if (FILE_TYPE_IMAGE.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("image/");
        } else if (FILE_TYPE_AUDIO.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("audio/");
        } else if (FILE_TYPE_VIDEO.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("video/");
        } else if (FILE_TYPE_APPLICATION.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("application/");
        }
        return false;
    }

}
