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
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.WRITE)
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
