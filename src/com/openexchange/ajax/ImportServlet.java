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
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ImportExportWriter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadFile;

/**
 * Servlet for doing imports of data like contacts stored in CSV format.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (spring configuration and refactoring)
 */
public class ImportServlet extends ImportExport {
	
	public static final String JSON_CALLBACK = "import";
	private static final long serialVersionUID = -4691910391290394603L;

	public ImportServlet() {
		super();
		LOG = LogFactory.getLog(ImportServlet.class);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			//checking format
			final Format format = Format.getFormatByConstantName(
					DataServlet.parseMandatoryStringParameter(req, PARAMETER_ACTION));
			if(format == null){
				resp.sendError(HttpServletResponse.SC_CONFLICT, "unknown format");
			}
			//getting folders
			final List <String> folders = Arrays.asList(
				DataServlet.parseStringParameterArray(req, PARAMETER_FOLDERID));
			
			//getting file
			UploadEvent ue = null;
			try {
				ue = processUpload(req);
				Iterator<UploadFile> iter = ue.getUploadFilesIterator();
				if(ue.getNumberOfUploadFiles() != 1){
					resp.sendError(HttpServletResponse.SC_CONFLICT, "can handle one and only one file in request");
					return;
				}
				final UploadFile uf = iter.next();
				//actual import
				final List<ImportResult> importResult = importerExporter.importData(
					getSessionObject(req), 
					format, 
					new FileInputStream(uf.getTmpFile()), 
					folders, 
					req.getParameterMap());
				//writing response
				final StringWriter stringWriter = new StringWriter();
				final JSONWriter jsonWriter = new JSONWriter(stringWriter);
				
				final ImportExportWriter importExportWriter = new ImportExportWriter(jsonWriter);
				
				jsonWriter.array();
				for (int a = 0; a < importResult.size(); a++) {
					importExportWriter.writeObject(importResult.get(a));
				}
				jsonWriter.endArray();
				
				//TODO: just a quick fix using Sebastian's ImportExportWriter. Might be improved.
				resp.setContentType("text/html");
				final String content = stringWriter.toString();
				Response resObj = new Response();
				resObj.setData(new JSONArray(content) );
				PrintWriter w = null;
				try {
					w = resp.getWriter();
					w.write(substitute(JS_FRAGMENT,"json",resObj.getJSON().toString(),"import",JSON_CALLBACK));
					close(w);
				} catch (IOException e) {
					LOG.warn(e);
				}
				
			} finally {
				if (ue != null) {
					ue.cleanUp();
				}
			}
		} catch (ImportExportException ex) {
			LOG.error(ex.toString(), ex);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
