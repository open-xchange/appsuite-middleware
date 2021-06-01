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

package com.openexchange.test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import com.openexchange.ajax.contact.action.DoAssociationRequest;
import com.openexchange.ajax.contact.action.DoAssociationResponse;
import com.openexchange.ajax.contact.action.DoSeparationRequest;
import com.openexchange.ajax.contact.action.DoSeparationResponse;
import com.openexchange.ajax.contact.action.GetAssociatedContactsRequest;
import com.openexchange.ajax.contact.action.GetAssociatedContactsResponse;
import com.openexchange.ajax.contact.action.GetAssociationRequest;
import com.openexchange.ajax.contact.action.GetAssociationResponse;
import com.openexchange.ajax.contact.action.GetContactByUIDRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.container.Contact;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AggregatingContactTestManager extends ContactTestManager {

    public AggregatingContactTestManager(AJAXClient client) throws OXException, IOException, JSONException {
        super(client);
    }

    public ContactUnificationState getAssociationBetween(Contact contributor, Contact aggregator) throws IOException, JSONException, OXException {
        GetAssociationResponse response = getClient().execute(new GetAssociationRequest(contributor, aggregator));

        doJanitorialTasks(response);
        return response.getState();
    }

    public void associateTwoContacts(Contact aggregator, Contact contributor) {
        try {
            DoAssociationResponse doAssociationResponse = getClient().execute(new DoAssociationRequest(contributor, aggregator));

            Contact temp;
            temp = getAction(aggregator);
            aggregator.setUserField20(temp.getUserField20());
            temp = getAction(contributor);
            contributor.setUserField20(temp.getUserField20());

            doJanitorialTasks(doAssociationResponse);
        } catch (Exception e) {
            doExceptionHandling(e, "DoAssociationRequest");
        }
    }

    public void separateTwoContacts(Contact aggregator, Contact contributor) {
        try {
            DoSeparationResponse doSeparationResponse = getClient().execute(new DoSeparationRequest(contributor, aggregator));
            aggregator = getAction(aggregator);
            contributor = getAction(contributor);
            doJanitorialTasks(doSeparationResponse);

        } catch (Exception e) {
            doExceptionHandling(e, "DoSeparationRequest");
        }
    }

    public Contact getContactByUID(UUID uid) {
        try {
            GetResponse response = getClient().execute(new GetContactByUIDRequest(uid, timeZone));
            doJanitorialTasks(response);
            return response.getContact();
        } catch (Exception e) {
            doExceptionHandling(e, "GetContactByUIDRequest");
        }
        return null;
    }

    public List<UUID> getAssociatedContactsByUID(UUID uid) {
        try {
            GetAssociatedContactsResponse response = getClient().execute(new GetAssociatedContactsRequest(uid, timeZone));
            doJanitorialTasks(response);
            return response.getUUIDs();
        } catch (Exception e) {
            doExceptionHandling(e, "GetAssociatedContactsRequest");
        }
        return null;
    }

    public List<UUID> getAssociatedContacts(Contact c) {
        try {
            GetAssociatedContactsResponse response = getClient().execute(new GetAssociatedContactsRequest(c, timeZone));
            doJanitorialTasks(response);
            return response.getUUIDs();
        } catch (Exception e) {
            doExceptionHandling(e, "GetAssociatedContactsRequest");
        }
        return null;
    }

}
