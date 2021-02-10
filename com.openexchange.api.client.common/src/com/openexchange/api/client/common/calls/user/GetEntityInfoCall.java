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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;


/**
 * {@link GetEntityInfoCall}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class GetEntityInfoCall extends AbstractGetCall<EntityInfo> {

    private final String entityIdentifier;
    private final String id;

    /**
     * Initializes a new {@link GetEntityInfoCall}.
     * 
     * @param entityIdentifier The entity ID
     * @param id The user ID
     */
    public GetEntityInfoCall(String entityIdentifier, String id) {
        super();
        this.entityIdentifier = entityIdentifier;
        this.id = id;
    }

    /**
     * Initializes a new {@link GetEntityInfoCall}.
     * 
     * @param entityIdentifier The entity ID
     * @param id The user ID
     */
    public GetEntityInfoCall(String entityIdentifier, int id) {
        this(entityIdentifier, String.valueOf(id));
    }

    @Override
    @NonNull
    public String getModule() {
        return "/user";
    }

    @Override
    public HttpResponseParser<EntityInfo> getParser() {
        String identifier = String.valueOf(id);
        int entity = Integer.parseInt(id); //TODO: leave member as int
        return new AbstractHttpResponseParser<EntityInfo>() {

            @Override
            public EntityInfo parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                if (commonResponse.isJSONObject()) {
                    JSONObject data = commonResponse.getJSONObject();
                    String displayName = data.optString("display_name");
                    String title = data.optString("title");
                    String firstName = data.optString("first_name");
                    String lastName = data.optString("last_name");
                    String email1 = data.optString("email1");
                    String imageUrl = data.optString("image1_url");
                    Type type = Type.USER;
                    return new EntityInfo(identifier, displayName, title, firstName, lastName, email1, entity, imageUrl, type);
                }
                return null;
            }
            
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        putIfNotEmpty(parameters, "id", id);
    }

    @Override
    protected String getAction() {
        return "get";
    }

    protected String getEntityIdentifier() {
        return entityIdentifier;
    }

}
