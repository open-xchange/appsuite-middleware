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

package com.openexchange.ajax.importexport.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ImportExportParser {

    /**
     * Prevent instantiation.
     */
    private ImportExportParser() {
        super();
    }

    public static final ImportResult parse(final String data) throws JSONException {
        final Response response = ResponseParser.parse(data);
        final ImportResult retval;
        final JSONObject json = response.getJSON();
        final String id = json.optString(CommonFields.ID);
        final String folderId = json.optString(CommonFields.FOLDER_ID);
        final long lastModified = json.optLong(CommonFields.LAST_MODIFIED);
        retval = new ImportResult(id, folderId, lastModified);
        if (response.getWarnings() != null && response.getWarnings().size() > 0) {
        	retval.setException(response.getWarnings().get(0));
        }
        if(response.hasError()){
        	retval.setException(response.getException());
        }

        JSONArray warnings = json.optJSONArray("warnings");
        List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>();

        if (warnings != null) {
            for (int i = 0, size = warnings.length(); i < size; i++) {
                String message = warnings.getJSONObject(i).getString("error");
                ConversionWarning warning = new ConversionWarning(-1, message);
                conversionWarnings.add(warning);
            }
            retval.addWarnings(conversionWarnings);
        }

        return retval;
    }
}
