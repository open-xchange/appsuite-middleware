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

package com.openexchange.filemanagement.json.actions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Requesting a formerly uploaded file", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "The ID of the uploaded file.")
}, responseDescription = "The content of the requested file is directly written into output stream.")
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public final class GetAction implements ETagAwareAJAXActionService {

    private final ServiceLookup services;

    public GetAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean checkETag(final String clientETag, final AJAXRequestData request, final ServerSession session) throws OXException {
        if (clientETag == null || clientETag.length() == 0) {
            return false;
        }
        try {
            final ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            management.getByID(clientETag);
            request.setExpires(ManagedFileManagement.TIME_TO_LIVE);
            return true;
        } catch (final OXException e) {
            if (ManagedFileExceptionErrorMessage.NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void setETag(final String eTag, final long expires, final AJAXRequestResult result) throws OXException {
        result.setExpires(expires);
        result.setHeader("ETag", eTag);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final String id = requestData.getParameter(AJAXServlet.PARAMETER_ID);
            if (id == null || id.length() == 0) {
                throw UploadException.UploadCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ID).setAction(AJAXServlet.ACTION_GET);
            }
            final ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            final ManagedFile file = management.getByID(id);
            /*
             * Content type
             */
            final String fileName = file.getFileName();
            final String disposition = file.getContentDisposition();
            final ContentType contentType = new ContentType(file.getContentType());
            if (null != fileName) {
                // Resolve Content-Type by file name if set to default
                if (contentType.startsWith("application/octet-stream")) {
                    /*
                     * Try to determine MIME type
                     */
                    final String ct = MimeType2ExtMap.getContentType(fileName);
                    final int pos = ct.indexOf('/');
                    contentType.setPrimaryType(ct.substring(0, pos));
                    contentType.setSubType(ct.substring(pos + 1));
                }
                // Set as "name" parameter
                contentType.setParameter("name", fileName);
            }
            /*
             * Write from content's input stream to response output stream
             */
            final ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            {
                final InputStream contentInputStream = new FileInputStream(file.getFile());
                try {
                    fileHolder.write(contentInputStream);
                } finally {
                    Streams.close(contentInputStream);
                }
            }
            /*
             * Parameterize file holder
             */
            if (fileName != null) {
                fileHolder.setName(fileName);
            }
            fileHolder.setContentType(contentType.toString());
            fileHolder.setDisposition(disposition);
            final AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
            /*
             * Set ETag
             */
            setETag(id, ManagedFileManagement.TIME_TO_LIVE, result);
            /*
             * Return result
             */
            return result;
        } catch (final IOException e) {
            throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        }
    }

}
