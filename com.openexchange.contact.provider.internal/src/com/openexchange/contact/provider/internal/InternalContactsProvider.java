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

package com.openexchange.contact.provider.internal;

import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONObject;
import com.openexchange.contact.DefaultContactsSession;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.common.ContactsSession;
import com.openexchange.contact.provider.AutoProvisioningContactsProvider;
import com.openexchange.contact.provider.ContactsAccessCapability;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.GroupwareContactsProvider;
import com.openexchange.contact.provider.folder.FolderContactsAccess;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link InternalContactsProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class InternalContactsProvider implements AutoProvisioningContactsProvider, GroupwareContactsProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link InternalContactsProvider}.
     */
    public InternalContactsProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return Constants.PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return StringHelper.valueOf(locale).getString(InternalContactsStrings.PROVIDER_NAME);
    }

    @Override
    public EnumSet<ContactsAccessCapability> getCapabilities() {
        return ContactsAccessCapability.getCapabilities(InternalContactsAccess.class);
    }

    @Override
    public FolderContactsAccess connect(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException {
        return new InternalContactsAccess(init(session, parameters), services);
    }

    @Override
    public JSONObject autoConfigureAccount(Session session, JSONObject userConfig, ContactsParameters parameters) throws OXException {
        return new JSONObject();
    }

    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, ContactsParameters parameters) throws OXException {
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(Constants.PROVIDER_ID);
    }

    @Override
    public JSONObject reconfigureAccount(Session session, ContactsAccount account, JSONObject userConfig, ContactsParameters parameters) throws OXException {
        return null;
    }

    @Override
    public void onAccountDeleted(Context context, ContactsAccount account, ContactsParameters parameters) throws OXException {
        // noop
    }

    @Override
    public void onAccountDeleted(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException {
        // noop
    }

    /**
     * Initialises the {@link ContactsSession}
     *
     * @param session The session
     * @param parameters The {@link ContactsParameters}
     * @return The initialised {@link ContactsSession}
     */
    private ContactsSession init(Session session, ContactsParameters parameters) throws OXException {
        DefaultContactsSession contactsSession = new DefaultContactsSession(session, parameters);
        if (null != parameters) {
            parameters.entrySet().forEach(entry -> contactsSession.set(entry.getKey(), entry.getValue()));
        }
        return contactsSession;
    }

}
