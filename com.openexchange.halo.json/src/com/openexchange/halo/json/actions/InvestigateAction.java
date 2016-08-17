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
