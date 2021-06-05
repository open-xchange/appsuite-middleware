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

package com.openexchange.halo.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.parser.ContactParser;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.ContactHalo;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link InvestigateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class InvestigateAction extends AbstractHaloAction {

    /**
     * Initializes a new {@link InvestigateAction}.
     *
     * @param services The OSGi service look-up
     */
    public InvestigateAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        ContactHalo contactHalo = requireContactHalo();
        String provider = requestData.requireParameter("provider");

        Contact contact = new Contact();
        {
            Object optData = requestData.getData();
            if (null == optData) {
                contact.setEmail1(requestData.getParameter("email1"));
                contact.setEmail2(requestData.getParameter("email2"));
                contact.setEmail3(requestData.getParameter("email3"));
                if (requestData.isSet("internal_userid")) {
                    contact.setInternalUserId(java.lang.Integer.parseInt(requestData.getParameter("internal_userid")));
                }
            } else {
                JSONObject jContact = (JSONObject) optData;
                ContactParser parser = new ContactParser(false, TimeZoneUtils.getTimeZone(requestData.getParameter("timezone")));
                parser.parse(contact, jContact);
                int userId = jContact.optInt("contact_id", 0);
                if (userId <= 0) {
                    userId = jContact.optInt("internal_userid", 0);
                }
                if (userId > 0) {
                    contact.setInternalUserId(userId);
                }
            }
        }

        return contactHalo.investigate(provider, contact, requestData, session);
    }

}
