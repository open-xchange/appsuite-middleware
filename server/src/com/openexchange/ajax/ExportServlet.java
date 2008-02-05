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

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.SizedInputStream;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Servlet for doing exports of data like contacts in CSV format.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (spring configuration and refactoring)
 */
public class ExportServlet extends ImportExport {
	
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ExportServlet.class);

	private static final long serialVersionUID = -4391378107330348835L;

	public ExportServlet(){
		super();
	}
	
	@SuppressWarnings("unchecked")
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			final String folder = DataServlet.parseMandatoryStringParameter(req, PARAMETER_FOLDERID);
			final int[] fieldsToBeExported = DataServlet.parsIntParameterArray(req, PARAMETER_COLUMNS);
			
			//checking format
			final Format format = Format.getFormatByConstantName(
					DataServlet.parseMandatoryStringParameter(req, PARAMETER_ACTION));
			if(format == null){
				resp.sendError(HttpServletResponse.SC_CONFLICT, "unknown format");
			}

            ServerSession session = new ServerSessionAdapter(getSessionObject(req));
            final SizedInputStream inputStream = importerExporter.exportData(session, format, folder, fieldsToBeExported, req.getParameterMap());
			
			final OutputStream outputStream = resp.getOutputStream();
			resp.setContentLength((int) inputStream.getSize());
			resp.setContentType(inputStream.getFormat().getMimeType());
			
			final byte[] b = new byte[1024];
			int i = 0; 
			while ((i = inputStream.read(b)) != -1) {
				outputStream.write(b, 0, i);
				outputStream.flush();
			}		
		} catch (Exception ex) {
			LOG.error("unknown exception: " , ex);
		}		
	}

}
