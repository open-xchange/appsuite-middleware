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

package com.openexchange.ajax.folder.actions;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.webdav.xml.fields.FolderFields;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.api2.OXException;

/**
 * {@link GetResponse}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * 
 */
public final class GetResponse extends AbstractAJAXResponse {

	private FolderObject folder;

	/**
	 * Initializes a new {@link GetResponse}
	 * 
	 * @param response
	 *            The response
	 */
	public GetResponse(final Response response) {
		super(response);
	}

	/**
     * @return the folder
     * @throws OXJSONException parsing the folder out of the response fails.
     */
    public FolderObject getFolder() throws OXJSONException, OXException {
        if(hasError()) {
            return null;
        }
        if (null == folder) {
            final FolderObject parsed = new FolderObject();
            JSONObject data = (JSONObject) getData();
            try {
                rearrangeId(data);
            } catch (JSONException e) {
                throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e);
            }
            new FolderParser().parse(parsed, data);//.parse(parsed, (JSONObject) getData());
            fillInFullName(data, parsed);
            this.folder = parsed;
        }
        return folder;
    }

    private void fillInFullName(JSONObject data, FolderObject parsed) {
        if(data.has("full_name")) {
            parsed.setFullName(data.optString("full_name"));
        }
    }

    private void rearrangeId(JSONObject data) throws JSONException {
        try {
            Integer.parseInt(data.getString(FolderFields.ID));
        } catch (NumberFormatException x) {
            String id = data.getString(FolderFields.ID);
            data.remove(FolderFields.ID);
            data.put("full_name", id);
        }
    }
}
