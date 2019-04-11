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

package com.openexchange.ajax.share.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;

/**
 * {@link FileSharesResponse}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileSharesResponse extends AbstractAJAXResponse {

    private final int[] columns;
    private final JSONArray data;

    /**
     * Initializes a new {@link FileSharesResponse}.
     *
     * @param response The underlying response
     * @param columns The requested columns
     */
    public FileSharesResponse(Response response, int[] columns) throws JSONException {
        super(response);
        this.columns = columns;
        this.data = response.hasError() ? null : (JSONArray) response.getData();
    }

    /**
     * Gets the shared files.
     *
     * @param timeZone The client timezone to consider
     * @return The shared files
     */
    public List<FileShare> getShares(TimeZone timeZone) throws JSONException, OXException {
        if (null != data) {
            return parse(data, columns, timeZone);
        }
        return null;
    }

    private static List<FileShare> parse(JSONArray jsonArray, int[] columns, TimeZone timeZone) throws JSONException, OXException {
        List<FileShare> shares = new ArrayList<FileShare>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            shares.add(FileShare.parse(jsonArray.getJSONArray(i), columns, timeZone));
        }
        return shares;
    }

}
