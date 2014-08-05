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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.json.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.CreateRequest;
import com.openexchange.share.Guest;
import com.openexchange.share.Share;
import com.openexchange.share.ShareService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link NewAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class NewAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link NewAction}.
     * @param services
     */
    public NewAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            CreateRequest createRequest = parseCreateRequest((JSONObject) requestData.requireData());
            ShareService service = services.getService(ShareService.class);
//            Share share = service.create(createRequest, session);

//            return new AJAXRequestResult(writeShare(share), "json");
            return AJAXRequestResult.EMPTY_REQUEST_RESULT;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    private static JSONObject writeShare(Share share) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("token", share.getToken());
        json.put("module", share.getModule());
        json.put("folder", share.getFolder());
        json.put("item", share.getItem());
        json.put("created", share.getCreated().getTime());
        json.put("created_by", share.getCreatedBy());
        json.put("last_modified", share.getLastModified().getTime());
        json.put("modified_by", share.getModifiedBy());
        json.put("expires", share.getExpires());
        json.put("guest", share.getGuest());
        json.put("authentication", share.getAuthentication().getID());

        return json;
    }

    private static CreateRequest parseCreateRequest(JSONObject json) throws JSONException {
        CreateRequest createRequest = new CreateRequest();
        createRequest.setModule(json.getInt("module"));
        createRequest.setFolder(json.getString("folder"));
        JSONArray jEntities = json.getJSONArray("entities");
        for (int i = 0; i < jEntities.length(); i++) {
            JSONObject jEntity = jEntities.getJSONObject(i);
            Guest entity = new Guest();
            entity.setMailAddress(jEntity.getString("mail_address"));
            entity.setContactID(jEntity.optString("contact_id", null));
            entity.setContactFolderID(jEntity.optString("contact_folder", null));
//            entity.setPermissions(jEntity.getInt("permissions"));
            int jAuthentication = jEntity.optInt("authentication", -1);
            if (0 < jAuthentication) {
                AuthenticationMode authenticationMode = AuthenticationMode.fromID(jAuthentication);
                String password = null;
                if (authenticationMode != AuthenticationMode.ANONYMOUS) {
                    password = jEntity.getString("password");
                }

                entity.setAuthenticationMode(authenticationMode);
                entity.setPassword(password);
            }

            createRequest.addGuest(entity);
        }

        return createRequest;
    }

}
