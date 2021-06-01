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

package com.openexchange.halo.xing;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.Contacts;
import com.openexchange.xing.Path;
import com.openexchange.xing.User;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class XingInvestigationResultConverter implements ResultConverter {

    @Override
    public String getInputFormat() {
        return XingInvestigationResult.class.getName();
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object object = result.getResultObject();
        if (object == null) {
            throw new IllegalStateException("Result object was null.");
        }

        if (!XingInvestigationResult.class.isAssignableFrom(object.getClass())) {
            throw new IllegalStateException("Result object is of wrong type '" + object.getClass().getName() +
                "'. Expected '" + XingInvestigationResult.class.getName() + "'.");
        }

        XingInvestigationResult investigation = (XingInvestigationResult) object;
        JSONObject jResult = new JSONObject();
        try {
            User user = investigation.getUser();
            if (user != null) {
                jResult.put("profile", user.toJSON());
                Path shortestPath = investigation.getShortestPath();
                if (shortestPath != null) {
                    jResult.put("path", buildUsersArray(shortestPath.getInBetween()));
                }

                Contacts sharedContacts = investigation.getSharedContacts();
                if (sharedContacts != null) {
                    jResult.put("sharedContacts", buildUsersArray(sharedContacts.getUsers()));
                }
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create();
        }

        result.setResultObject(jResult, "json");
    }

    private JSONArray buildUsersArray(List<User> users) {
        if (users == null) {
            return null;
        }

        JSONArray jUsers = new JSONArray(users.size());
        for (User user : users) {
            jUsers.put(user.toJSON());
        }
        return jUsers;
    }

//    TODO: implement or delete based on UI requirements. See https://dev.xing.com/docs/get/users/:id
//    private static JSONObject convertUser(User user) {
//      JSONObject jUser = new JSONObject();
//      try {
//          jUser.putOpt("id", user.getId());
//          jUser.putOpt("first_name", user.getFirstName());
//          jUser.putOpt("last_name", user.getLastName());
//          jUser.putOpt("display_name", user.getDisplayName());
//          jUser.putOpt("page_name", user.getPageName());
//          jUser.putOpt("permalink", user.getPermalink());
//          jUser.putOpt("employment_status", user.get);
//          jUser.putOpt("gender", user.getGender());
//          Date birthDate = user.getBirthDate();
//          if (birthDate != null) {
//              jUser.put("birth_date", getDateInstance(MEDIUM, US).format(birthDate));
//          }
//          jUser.putOpt("active_email", user.getActiveMail());
//          jUser.putOpt("", user.);
//          result.setResultObject(jUser, "json");
//      } catch (JSONException e) {
//          throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
//      }
//    }

}
