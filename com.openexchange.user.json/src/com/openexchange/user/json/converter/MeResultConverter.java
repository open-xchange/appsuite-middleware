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

package com.openexchange.user.json.converter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.json.dto.Me;


/**
 * {@link MeResultConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class MeResultConverter implements ResultConverter {

    @Override
    public String getInputFormat() {
        return "user/me";
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
        if (object instanceof Me) {
            Me me = (Me) object;
            User user = me.getUser();
            Context context = me.getContext();

            JSONObject jReturn = new JSONObject(8);
            try {
                jReturn.put("context_id", context.getContextId());
                jReturn.put("user_id", user.getId());
                jReturn.put("context_admin", context.getMailadmin());
                String str = me.getLoginName();
                jReturn.put("login_name", str == null ? "<unknown>" : str);
                str = user.getDisplayName();
                jReturn.put("display_name", str == null ? "<unknown>" : str);

                String mailLogin = me.getMailLogin();
                if (null != mailLogin) {
                    jReturn.put("mail_login", mailLogin);
                }

                jReturn.put("email_address", user.getMail());

                JSONArray jAliases;
                {
                    String[] aliases = user.getAliases();
                    if (aliases != null && aliases.length > 0) {
                        jAliases = new JSONArray(aliases.length);
                        for (String alias : aliases) {
                            jAliases.put(alias);
                        }
                    } else {
                        jAliases = JSONArray.EMPTY_ARRAY;
                    }
                }
                jReturn.put("email_aliases", jAliases);

                result.setResultObject(jReturn, "json");
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }

}
