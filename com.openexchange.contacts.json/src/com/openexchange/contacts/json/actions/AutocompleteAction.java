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

import static com.openexchange.java.Autoboxing.B;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AutocompleteAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.READ)
public class AutocompleteAction extends IDBasedContactAction {

    private static final Set<String> OPTIONAL_PARAMETERS = ImmutableSet.of(PARAM_FIELDS, PARAM_ORDER, PARAM_ORDER_BY, PARAM_COLLATION, PARAM_RIGHT_HAND_LIMIT);

    /**
     * Initializes a new {@link AutocompleteAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public AutocompleteAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        access.set(ContactsParameters.PARAMETER_REQUIRE_EMAIL, B(request.isRequireEmail()));
        List<String> folderIds = null != request.optFolderID() ? Collections.singletonList(request.optFolderID()) : null;
        List<Contact> contacts = access.autocompleteContacts(folderIds, request.getQuery());
        Collections.sort(contacts, new UseCountComparator(request.getSession().getUser().getLocale()));

        return new AJAXRequestResult(contacts, getLatestTimestamp(contacts), "contact");
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }
}
