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

package com.openexchange.mail.autoconfig.json.converter;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AutoconfigResultConverter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoconfigResultConverter implements ResultConverter {

    @Override
    public String getInputFormat() {
        return "autoconfig";
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
        result.setResultObject(convert((Autoconfig) result.getResultObject()), "json");
    }

    private JSONObject convert(Autoconfig autoconfig) throws OXException {
        if (autoconfig == null) {
            return new JSONObject(1);
        }

        try {
            boolean noTransport = null == autoconfig.getTransportServer();
            JSONObject json = new JSONObject(16);
            json.put("login", autoconfig.getUsername());
            json.put("transport_login", noTransport ? null : autoconfig.getUsername());
            json.put("mail_server", autoconfig.getMailServer());
            json.put("transport_server", autoconfig.getTransportServer());
            json.put("mail_port", autoconfig.getMailPort());
            json.put("transport_port", autoconfig.getTransportPort());
            json.put("mail_protocol", autoconfig.getMailProtocol());
            json.put("transport_protocol", autoconfig.getTransportProtocol());
            json.put("mail_secure", autoconfig.isMailSecure());
            json.put("transport_secure", autoconfig.isTransportSecure());
            json.put("mail_starttls", autoconfig.isMailStartTls());
            json.put("transport_starttls", noTransport ? null :  Boolean.valueOf(autoconfig.isTransportStartTls()));
            Integer mailOAuthId = autoconfig.getMailOAuthId();
            if (null != mailOAuthId) {
                json.put("mail_oauth", mailOAuthId);
            }
            Integer transportOAuthId = autoconfig.getTransportOAuthId();
            if (null != transportOAuthId) {
                json.put("transport_oauth", transportOAuthId);
            }
            String sourceName = autoconfig.getSource();
            if (null != sourceName) {
                json.put("config_source", sourceName);
            }
            return json;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

}
