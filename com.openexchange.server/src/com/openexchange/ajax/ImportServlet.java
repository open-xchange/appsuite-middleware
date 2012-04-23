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

package com.openexchange.ajax;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.writer.ImportExportWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportExportExceptionCodes;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * Servlet for doing imports of data like contacts stored in CSV format,
 * contacts stored as VCards or tasks and appointments within an ICAL file.
 *
 * You do a basic POST request uploading a file. A bit different is the
 * response: Since this servlet works with an AJAX GUI, the upload is
 * normally targetted at a hidden frame somewhere (not to cause a reload
 * of the whole GUI) and this frame needs a JavaScript method call to come
 * back from the dead.
 * So the response is a HTML page calling a JavaScript.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a> (development)
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (refactoring, redesign)
 */
public class ImportServlet extends ImportExport {

    private static final long serialVersionUID = 5639598623111215315L;
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ImportServlet.class));
    //identifying part of the ajax method that does the callback after the upload
    public static final String JSON_CALLBACK = "import";

    public ImportServlet() {
        super();
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response resObj = new Response();
        final List<ImportResult> importResult;
        try {
            init();
            final ServerSession session = getSessionObject(req);
            resObj.setLocale(session.getUser().getLocale());
            //checking format
            final String formatStr = DataServlet.parseMandatoryStringParameter(req, PARAMETER_ACTION);
            final Format format = Format.getFormatByConstantName(formatStr);
            if (format == null) {
                throw ImportExportExceptionCodes.UNKNOWN_FORMAT.create(formatStr);
            }
            //getting folders
            final List <String> folders = Arrays.asList(DataServlet.parseStringParameterArray(req, PARAMETER_FOLDERID));
            //getting file
            UploadEvent event = null;
            try {
                event = processUpload(req);
                final Iterator<UploadFile> iter = event.getUploadFilesIterator();
                if (event.getNumberOfUploadFiles() != 1) {
                    if (event.getNumberOfUploadFiles() == 0) {
                        throw ImportExportExceptionCodes.FILE_NOT_EXISTS.create();
                    }
                    throw ImportExportExceptionCodes.ONLY_ONE_FILE.create(I(event.getNumberOfUploadFiles()));
                }
                final UploadFile file = iter.next();
                final File upload = file.getTmpFile();
                if (upload.length() == 0) {
                    throw ImportExportExceptionCodes.EMPTY_FILE.create();
                }
                //actual import
                importResult = importerExporter.importData(session, format, new FileInputStream(upload), folders, req.getParameterMap());
            } finally {
                if (event != null) {
                    event.cleanUp();
                }
            }
            //writing response
            final ImportExportWriter writer = new ImportExportWriter(session);
            try {
                writer.writeObjects(importResult);
                resObj.setData(writer.getObject());
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
            }
        } catch (final OXException e) {
            if (Category.CATEGORY_USER_INPUT.equals(e.getCategory())) {
                LOG.debug(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            resObj.setException(e);
        }
        Send.sendCallbackResponse(resObj, JSON_CALLBACK, resp);
    }
}
