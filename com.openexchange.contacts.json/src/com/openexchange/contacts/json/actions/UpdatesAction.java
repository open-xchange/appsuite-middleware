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
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;

/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.READ)
public class UpdatesAction extends IDBasedContactAction {

    private static final String MODIFIED = "modified";
    private static final String DELETED = "deleted";

    private static final Set<String> OPTIONAL_PARAMETERS = ImmutableSet.of(PARAM_FIELDS, PARAM_ORDER, PARAM_ORDER_BY);

    /**
     * Initializes a new {@link UpdatesAction}.
     *
     * @param serviceLookup
     */
    public UpdatesAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        Date since = new Date(request.getTimestamp());
        List<Contact> modifiedContacts = access.getModifiedContacts(request.getFolderID(), since);
        Date lastModified = getLatestTimestamp(modifiedContacts);
        List<Contact> deletedContacts = new LinkedList<>();
        if (false == DELETED.equals(request.getIgnore())) {
            deletedContacts = access.getDeletedContacts(request.getFolderID(), since);
            Date lastModified2 = getLatestTimestamp(deletedContacts);
            if (0 < deletedContacts.size() && null != lastModified2 && lastModified2.after(lastModified)) {
                lastModified = lastModified2;
            }
        }
        return new AJAXRequestResult(ImmutableMap.of(MODIFIED, modifiedContacts, DELETED, deletedContacts), lastModified, "contact");
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }
}
