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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE, type = RestrictedAction.Type.WRITE)
public class DeleteAction extends IDBasedContactAction {

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public DeleteAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        if (request.getData() instanceof JSONObject) {
            deleteSingle(access, request);
        } else {
            deleteMultiple(access, request);
        }
        return new AJAXRequestResult(new JSONObject(0), new Date(request.getTimestamp()), "json");
    }

    /**
     * Deletes a single contact
     *
     * @param access The {@link IDBasedContactsAccess}
     * @param request The {@link ContactRequest}
     * @throws OXException if an error is occurred
     */
    private void deleteSingle(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        access.deleteContact(request.getContactID(), request.getTimestamp());
    }

    /**
     * Deletes multiple contacts
     *
     * @param access The {@link IDBasedContactsAccess}
     * @param request The {@link ContactRequest}
     * @throws OXException if an error is occurred
     */
    private void deleteMultiple(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        Map<String, List<String>> objectIDsPerFolder = request.getObjectIDsPerFolder();
        if (null == objectIDsPerFolder || objectIDsPerFolder.isEmpty()) {
            return;
        }

        List<ContactID> ids = new LinkedList<>();
        for (Entry<String, List<String>> entry : objectIDsPerFolder.entrySet()) {
            for (String objectId : entry.getValue()) {
                ids.add(getContactID(entry.getKey(), objectId));
            }
        }
        access.deleteContacts(ids, request.getTimestamp());
    }
}
