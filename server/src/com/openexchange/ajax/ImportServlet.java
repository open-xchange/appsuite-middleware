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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.writer.ImportExportWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadFile;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.session.Session;

@OXExceptionSource(
    classId=ImportExportExceptionClasses.IMPORTSERVLET, 
    component=Component.IMPORT_EXPORT
)
@OXThrowsMultiple(
	category = { 
		Category.USER_INPUT, 
		Category.USER_INPUT,
		Category.USER_INPUT,
		Category.USER_INPUT
    }, 
	desc = { "", "", "", "" }, 
	exceptionId = { 0, 1, 2, 3 }, 
	msg = { 
		"Can only handle one file, not %s",
		"Unknown format: %s",
		"Uploaded file is of type %s, cannot handle that",
		"Empty file uploaded."
    }
)
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
	
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ImportServlet.class);

	private final static ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(ImportServlet.class);
	public static final String JSON_CALLBACK = "import"; //identifying part of the ajax method that does the callback after the upload
	private static final long serialVersionUID = -4691910391290394603L;

	public ImportServlet() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override 
	protected void doPost(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        final Response resObj = new Response();
        final List<ImportResult> importResult;
		try {
			//checking format
			final String formatStr = DataServlet.parseMandatoryStringParameter(
                req, PARAMETER_ACTION);
			final Format format = Format.getFormatByConstantName(formatStr);
			if(format == null){
				throw EXCEPTIONS.create(1, formatStr);
			}
			//getting folders
			final List <String> folders = Arrays.asList(DataServlet
                .parseStringParameterArray(req, PARAMETER_FOLDERID));
			//getting file
			UploadEvent event = null;
			try {
				event = processUpload(req);
				final Iterator<UploadFile> iter = event.getUploadFilesIterator();
				if(event.getNumberOfUploadFiles() != 1){
					throw EXCEPTIONS.create(0, Integer.valueOf(event
                        .getNumberOfUploadFiles()));
				}
				final UploadFile file = iter.next();
				final File upload = file.getTmpFile();
				if(upload.length() == 0){
					throw EXCEPTIONS.create(3);
				}
				//actual import
                ServerSession session = new ServerSessionAdapter(getSessionObject(
                        req));
                importResult = importerExporter.importData(session, format, new FileInputStream(upload), folders,
                    req.getParameterMap());
            } finally {
				if (event != null) {
					event.cleanUp();
				}
			}
            //writing response
            final ImportExportWriter writer = new ImportExportWriter();
            try {
                writer.writeObjects(importResult);
                resObj.setData(writer.getObject());
            } catch (JSONException e){
                throw new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR,
                    e);
            }
        } catch (AbstractOXException e) {
            if (Category.USER_INPUT.equals(e.getCategory())) {
                LOG.debug(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            resObj.setException(e);
        }
        Send.sendCallbackResponse(resObj, JSON_CALLBACK, resp);
	}
}
