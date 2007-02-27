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

import com.openexchange.ajax.writer.ImportExportWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.ImporterExporter;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * ImportExport
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class ImportExport extends SessionServlet {
	
	public static final String AJAX_TYPE = "type";
	
	private static final Log LOG = LogFactory.getLog(ImportExport.class);
	
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			final int type = DataServlet.parseMandatoryIntParameter(req, AJAX_TYPE);
			final String folder = DataServlet.parseMandatoryStringParameter(req, PARAMETER_FOLDERID);
			final int[] fieldsToBeExported = DataServlet.parsIntParameterArray(req, PARAMETER_COLUMNS);
			
			final String mimeType = req.getContentType();
			final Format f = Format.getFormatByMimeType(mimeType);
			
			final ImporterExporter importerExporter = new ImporterExporter();
			final InputStream inputStream = importerExporter.exportData(getSessionObject(req), f, folder, type, fieldsToBeExported, req.getParameterMap());
			
			final OutputStream outputStream = resp.getOutputStream();
			
			final byte[] b = new byte[1024];
			int i = 0; 
			while ((i = inputStream.read()) != -1) {
				outputStream.write(b, 0, i);
				outputStream.flush();
			}		
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			final int[] type = DataServlet.parseMandatoryIntParameterArray(req, AJAX_TYPE);
			final String[] folder = DataServlet.parseStringParameterArray(req, PARAMETER_FOLDERID);
			
			final String mimeType = req.getContentType();
			final Format f = Format.getFormatByMimeType(mimeType);
			
			final HashMap hashMap = new HashMap<String, Integer>();
			if (type.length != folder.length) {
				resp.setStatus(HttpServletResponse.SC_CONFLICT, "invalid data in request");
				return;
			}
			
			for (int a = 0; a < type.length; a++) {
				hashMap.put(folder[a], type[a]);
			}
			
			final ImporterExporter importerExporter = new ImporterExporter();
			final List<ImportResult> importResult = importerExporter.importData(getSessionObject(req), f, req.getInputStream(), hashMap, req.getParameterMap());
			final StringWriter stringWriter = new StringWriter();
			final JSONWriter jsonWriter = new JSONWriter(stringWriter);
			
			final ImportExportWriter importExportWriter = new ImportExportWriter(jsonWriter);
			
			jsonWriter.array();
			for (int a = 0; a < importResult.size(); a++) {
				importExportWriter.writeObject(importResult.get(a));
			}
			jsonWriter.endArray();
			
			final OutputStream outputStream = resp.getOutputStream();
			
			resp.setContentType(CONTENTTYPE_JAVASCRIPT);
			
			final String content = stringWriter.toString();
			resp.setContentLength(content.length());
			
			outputStream.write(content.getBytes("UTF-8"));
			outputStream.flush();
		} catch (ImportExportException ex) {
			LOG.error(ex.toString(), ex);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
