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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.importexport.json;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class ImportRequest {

	private AJAXRequestData request;
	private ServerSession session;
	private final List<String> folders;
	private InputStream inputStream;

	public AJAXRequestData getRequest() {
		return request;
	}

	public void setRequest(AJAXRequestData request) {
		this.request = request;
	}

	public ServerSession getSession() {
		return session;
	}

	public void setSession(ServerSession session) {
		this.session = session;
	}

	public ImportRequest(AJAXRequestData request, ServerSession session) throws OXException {
		this.session = session;
		this.request = request;
		if (!request.containsParameter("callback")) {
			request.putParameter("callback", "import"); // hack to stay backwards-compatible with 6.20 version that did not comply to the HTTP API
		}
		if(request.getParameter(AJAXServlet.PARAMETER_FOLDERID) == null){
			throw ImportExportExceptionCodes.NEED_FOLDER.create();
		}
		this.folders = Arrays.asList(request.getParameter(AJAXServlet.PARAMETER_FOLDERID).split(","));

		long maxSize = sysconfMaxUpload();
		if(!request.hasUploads(-1, maxSize > 0 ? maxSize : -1L)){
			throw ImportExportExceptionCodes.NO_FILE_UPLOADED.create();
		}
		if(request.getFiles(-1, maxSize > 0 ? maxSize : -1L).size() > 1){
			throw ImportExportExceptionCodes.ONLY_ONE_FILE.create();
		}
		UploadFile uploadFile = request.getFiles().get(0);
		try {
			inputStream = new FileInputStream(uploadFile.getTmpFile());
		} catch (FileNotFoundException e) {
			throw ImportExportExceptionCodes.TEMP_FILE_NOT_FOUND.create();
		}
	}

	private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

	public int getContextId() {
		return session.getContextId();
	}

	public List<String> getFolders() {
		return this.folders;
	}

	public InputStream getImportFileAsStream() {
		return this.inputStream;
	}

	/**
	 * Gets all optional parameters as found in the underlying request wrapped into a set.
	 *
	 * @return The optional parameters
	 */
	public Map<String, String[]> getOptionalParams() {
	    Set<String> defaultParameters = new HashSet<String>(Arrays.asList(
	        "callback", AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_ACTION, AJAXServlet.PARAMETER_SESSION));
	    Map<String, String[]> optionalParameters = new HashMap<String, String[]>();
	    Map<String, String> parameters = this.request.getParameters();
	    if (null != parameters) {
	        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
	            if (false == defaultParameters.contains(parameter.getKey())) {
	                optionalParameters.put(parameter.getKey(), new String[] { parameter.getValue() });
	            }
            }
	    }
        return optionalParameters;
	}
}
