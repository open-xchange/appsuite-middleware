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

package com.openexchange.importexport.actions.exporter;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.importexport.json.ExportRequest;

/**
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public abstract class ContactExportAction extends AbstractExportAction {

    /** The parameter identifier whether distribution lists are supposed to be exported as well */
    public static final String PARAMETER_EXPORT_DLISTS = "export_dlists";

	@Override
	protected Map<String, Object> getOptionalParams(ExportRequest req) {
	    Map<String, Object> params = super.getOptionalParams(req);
	    if (params == null) {
	        params = new HashMap<String, Object>(2);
	    }

	    String exportDlistsParam = req.getRequest().getParameter(PARAMETER_EXPORT_DLISTS);
	    if (exportDlistsParam != null) {
	        params.put(PARAMETER_EXPORT_DLISTS, exportDlistsParam);
	    }

	    return params;
	}

}
