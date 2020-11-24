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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls.user;

import java.util.Map;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.calls.user.UserInformation.Builder;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link GetUserCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class GetUserCall extends AbstractGetCall<UserInformation> {

    private String id;

    /**
     * Initializes a new {@link GetUserCall}.
     */
    public GetUserCall() {
        this(null);
    }

    /**
     * Initializes a new {@link GetUserCall}.
     * 
     * @param id The ID of the user to get. <code>null</code> for the current session user
     */
    public GetUserCall(String id) {
        super();
        this.id = id;
    }

    @Override
    @NonNull
    public String getModule() {
        return "user";
    }

    @Override
    protected String getAction() {
        return "get";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        if (Strings.isNotEmpty(id)) {
            parameters.put("id", id);
        }
    }

    @Override
    public HttpResponseParser<UserInformation> getParser() {
        return new AbstractHttpResponseParser<UserInformation>() {

            @Override
            public UserInformation parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONObject jsonObject = commonResponse.getJSONObject();
                Builder builder = new UserInformation.Builder(); // @formatter:off
                builder.lastModified(jsonObject.optLong("last_modified"))
                    .lastModifiedUtc(jsonObject.optLong("last_modified_utc"))
                    .numberOfImages(jsonObject.optInt("number_of_images"))
                    .folderId(jsonObject.optString("folder_id"))
                    .sortName(jsonObject.optString("sort_name"))
                    .userId(jsonObject.optInt("user_id"))
                    .createdBy(jsonObject.optInt("created_by"))
                    .modifiedBy(jsonObject.optInt("modified_by"))
                    .id(jsonObject.optInt("id"))
                    .email1(jsonObject.optString("email1"))
                    .creationDate(jsonObject.optLong("creation_date"))
                    .locale(jsonObject.optString("locale"))
                    .contactId(jsonObject.optInt("contact_id"))
                    .guestCreatedBy(jsonObject.optInt("guest_created_by"))
                    .timezone(jsonObject.optString("timezone"));
                // @formatter:on

                JSONArray groups = jsonObject.optJSONArray("groups");
                if (null != groups) {
                    for (int i = 0; i < groups.length(); i++) {
                        builder.addGroup(groups.optInt(i));
                    }
                }
                JSONArray aliases = jsonObject.optJSONArray("aliases");
                if (null != groups) {
                    for (int i = 0; i < aliases.length(); i++) {
                        builder.addAliases(aliases.optString(i));
                    }
                }
                return builder.build();
            }
        };
    }

}
