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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
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
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadFile;

@OXThrowsMultiple(
	category = { 
		Category.USER_INPUT, 
		Category.USER_INPUT,
		Category.USER_INPUT,
		Category.USER_INPUT,
		Category.CODE_ERROR}, 
	desc = { "" , "" , "", "" , ""}, 
	exceptionId = { 0,1,2,3,4 }, 
	msg = { 
		"Can only handle one file, not %s",
		"Unknown format: %s",
		"Uploaded file is of type %s, cannot handle that",
		"Empty file uploaded.",
		"Could not send results of importing process."})
@OXExceptionSource(
		classId=ImportExportExceptionClasses.IMPORTSERVLET, 
		component=Component.IMPORT_EXPORT)

/**
 * Servlet for doing imports of data like contacts stored in CSV format.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (spring configuration and refactoring)
 */
public class ImportServlet extends ImportExport {
	
	private final static ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(ImportServlet.class);
	public static final String JSON_CALLBACK = "import";
	private static final long serialVersionUID = -4691910391290394603L;

	public ImportServlet() {
		super();
		LOG = LogFactory.getLog(ImportServlet.class);
	}
	
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final Response resObj = new Response();
		try {
			//checking format
			final String formatStr = DataServlet.parseMandatoryStringParameter(req, PARAMETER_ACTION);
			final Format format = Format.getFormatByConstantName(formatStr);
			if(format == null){
				sendResponse(EXCEPTIONS.create(1, formatStr), resp);
				return;
			}
			//getting folders
			final List <String> folders = Arrays.asList(
				DataServlet.parseStringParameterArray(req, PARAMETER_FOLDERID));
			
			//getting file
			final List<ImportResult> importResult;
			UploadEvent ue = null;
			try {
				ue = processUpload(req);
				final Iterator<UploadFile> iter = ue.getUploadFilesIterator();
				if(ue.getNumberOfUploadFiles() != 1){
					sendResponse(EXCEPTIONS.create(0, ue.getNumberOfUploadFiles()), resp);
					return;
				}
				final UploadFile uf = iter.next();
				final File upload = uf.getTmpFile();
				if(upload.length() == 0){
					sendResponse(EXCEPTIONS.create(3), resp);
					return;
				}
				
				//actual import
				importResult = importerExporter.importData(
					getSessionObject(req), format, new FileInputStream(upload), folders, req.getParameterMap());

			} finally {
				if (ue != null) {
					ue.cleanUp();
				}
			}
			//writing response
			final ImportExportWriter importExportWriter = new ImportExportWriter();
			try {
				
				importExportWriter.writeObjects(importResult);
				resObj.setData( importExportWriter.getObject() );
				
				sendResponse(resObj.getJSON(), resp);
			} catch (JSONException e){
				LOG.warn(e);
				throw EXCEPTIONS.create(4);
			}
		} catch (AbstractOXException e){
			if(Category.USER_INPUT.equals( e.getCategory() ) ){
				LOG.debug("user error: ", e);
			} else {
				LOG.error("import exception: " , e);
			}
			sendResponse(e, resp);
		}
	}
	
	protected boolean isKnownContentType(final String mime) {
		return Format.getFormatByMimeType(mime) != null;
	}

	/**
	 * Send JSON object through response
	 * @param jsonObj
	 * @param resp
	 */
	protected void sendResponse(final JSONObject jsonObj, final HttpServletResponse resp){
		resp.setContentType("text/html");
		PrintWriter w = null;
		try {
			w = resp.getWriter();
			w.write(substitute(JS_FRAGMENT,"json",jsonObj.toString(),"action",JSON_CALLBACK));
			close(w);
		} catch (IOException e) {
			LOG.warn(e);
			try {
				sendError(resp);
			} catch (IOException e1) {
				LOG.error("Could not even send a HTTP error:", e1);
			}
		}
	}
	
	/**
	 * Send error message through response
	 * @param exception
	 * @param resp
	 */
	protected void sendResponse(final AbstractOXException exception, final HttpServletResponse resp){
		final Response error = new Response();
		error.setException(exception);
		try {
			sendResponse(error.getJSON(), resp);
		} catch (JSONException e) {
			LOG.warn(e);
			try {
				sendError(resp);
			} catch (IOException e1) {
				LOG.error("Could not even send a HTTP error:", e1);
			}
		}
	}

}
