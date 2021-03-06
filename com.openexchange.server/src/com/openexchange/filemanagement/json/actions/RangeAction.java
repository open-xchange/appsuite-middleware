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

package com.openexchange.filemanagement.json.actions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RangeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public final class RangeAction implements AJAXActionService {

    /**
     * Initializes a new {@link RangeAction}.
     */
    public RangeAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        RandomAccessFile raf = null;
        ThresholdFileHolder fileHolder = null;
        try {
            final String id = requestData.getParameter(AJAXServlet.PARAMETER_ID);
            if (id == null || id.length() == 0) {
                throw UploadException.UploadCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ID).setAction("range");
            }
            final int off = requestData.getIntParameter("off");
            if (off < 0) {
                throw UploadException.UploadCode.MISSING_PARAM.create("off").setAction("range");
            }
            int len = requestData.getIntParameter("len");
            if (len < 0) {
                throw UploadException.UploadCode.MISSING_PARAM.create("len").setAction("range");
            }

            // Look-up managed file
            ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            ManagedFile file;
            try {
                file = management.getByID(id);
                // Check affiliation
                {
                    String affiliation = file.getAffiliation();
                    if (null != affiliation && !affiliation.equals(session.getSessionID())) {
                        throw ManagedFileExceptionErrorMessage.NOT_FOUND.create(id);
                    }
                }
            } catch (OXException e) {
                if (requestData.setResponseHeader("Content-Type", "text/html; charset=UTF-8")) {
                    if (ManagedFileExceptionErrorMessage.NOT_FOUND.equals(e) || ManagedFileExceptionErrorMessage.FILE_NOT_FOUND.equals(e)) {
                        try {
                            HttpServletResponse resp = requestData.optHttpServletResponse();
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            String desc = "An error occurred inside the server which prevented it from fulfilling the request.";
                            SessionServlet.writeErrorPage(HttpServletResponse.SC_NOT_FOUND, desc, resp);
                            return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                        } catch (IOException ioe) {
                            throw AjaxExceptionCodes.IO_ERROR.create(ioe, ioe.getMessage());
                        }
                    }
                }
                throw e;
            }

            // Content type
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
            // Create RandomAccessFile
            final File tmpFile = file.getFile();
            raf = new RandomAccessFile(tmpFile, "r");
            final long total = raf.length();
            fileHolder = new ThresholdFileHolder();
            if (off >= total) {
                fileHolder.write(new byte[0]);
            } else {
                // Check available bytes
                final long available = total - off;
                if (available < len) {
                    len = (int) available;
                }
                // Set file pointer & read
                raf.seek(off);
                final int buflen = 2048;
                final byte[] bytes = new byte[buflen];
                int n = 0;
                while (n < len) {
                    final int toRead = len - n;
                    final int read = raf.read(bytes, 0, buflen > toRead ? toRead : buflen);
                    if (read > 0) {
                        fileHolder.write(bytes, 0, read);
                        n += read;
                    } else {
                        break;
                    }
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
            /*
             * Return result
             */
            requestData.setFormat("file");
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
            fileHolder = null;
            return requestResult;
        } catch (IOException e) {
            throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(raf, fileHolder);
        }
    }

}
