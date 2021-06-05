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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

public class ImportRequest {

	private final AJAXRequestData request;
	private final ServerSession session;
	private final List<String> folders;
	private final UploadFile uploadFile;

	/**
	 * Initializes a new {@link ImportRequest}.
	 *
	 * @param uploadFile The upload file providing data of the file to import
	 * @param request The request data
	 * @param session The session
	 * @throws OXException If initialization fails
	 */
	public ImportRequest(UploadFile uploadFile, AJAXRequestData request, ServerSession session) throws OXException {
	    super();
	    if (!request.containsParameter("callback")) {
	        request.putParameter("callback", "import"); // hack to stay backwards-compatible with 6.20 version that did not comply to the HTTP API
	    }
	    String folderId = request.getParameter(AJAXServlet.PARAMETER_FOLDERID);
	    if (folderId == null) {
	        throw ImportExportExceptionCodes.NEED_FOLDER.create();
	    }
		this.session = session;
		this.request = request;
		this.folders = Arrays.asList(Strings.splitByComma(folderId));
		this.uploadFile = uploadFile;
	}

	public AJAXRequestData getRequest() {
        return request;
    }

    public ServerSession getSession() {
        return session;
    }

	public int getContextId() {
		return session.getContextId();
	}

	public List<String> getFolders() {
		return this.folders;
	}

	public InputStream getImportFileAsStream() throws OXException {
	    try {
            return new FileInputStream(uploadFile.getTmpFile());
        } catch (FileNotFoundException e) {
            throw ImportExportExceptionCodes.TEMP_FILE_NOT_FOUND.create();
        }
	}

	/**
	 * Gets all optional parameters as found in the underlying request wrapped into a map.
	 *
	 * @return The optional parameters
	 */
	public Map<String, String[]> getOptionalParams() {
	    Set<String> defaultParameters = new HashSet<String>(Arrays.asList(
	        "callback", AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_ACTION, AJAXServlet.PARAMETER_SESSION));
	    Map<String, String[]> optionalParameters = new HashMap<String, String[]>();
	    Map<String, String> parameters = this.request.getParameters();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (false == defaultParameters.contains(parameter.getKey())) {
                optionalParameters.put(parameter.getKey(), new String[] { parameter.getValue() });
            }
        }
        return optionalParameters;
	}
}
