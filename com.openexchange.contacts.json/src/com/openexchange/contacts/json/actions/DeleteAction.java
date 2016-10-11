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

package com.openexchange.contacts.json.actions;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
*/
@OAuthAction(ContactActionFactory.OAUTH_WRITE_SCOPE)
public class DeleteAction extends ContactAction {

    /**
     * Initializes a new {@link DeleteAction}.
     * @param serviceLookup
     */
    public DeleteAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest request) throws OXException {
        ServerSession session = request.getSession();
        Date lastRead = new Date(request.getTimestamp());
        if (request.getData() instanceof JSONObject) {
            int[] deleteRequestData = request.getDeleteRequestData();
            getContactService().deleteContact(session, Integer.toString(deleteRequestData[1]), Integer.toString(deleteRequestData[0]), lastRead);
        } else {
            Map<String, List<String>> objectIDsPerFolder = request.getObjectIDsPerFolder();
            if (null != objectIDsPerFolder && !objectIDsPerFolder.isEmpty()) {
                ContactService contactService = getContactService();
                for (Entry<String, List<String>> entry : objectIDsPerFolder.entrySet()) {
                    String[] objectIDs = entry.getValue().toArray(new String[entry.getValue().size()]);
                    contactService.deleteContacts(session, entry.getKey(), objectIDs, lastRead);
                }
            }
        }
        return new AJAXRequestResult(new JSONObject(0), lastRead, "json");
    }

}
