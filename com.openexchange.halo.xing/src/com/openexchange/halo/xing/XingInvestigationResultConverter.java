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
