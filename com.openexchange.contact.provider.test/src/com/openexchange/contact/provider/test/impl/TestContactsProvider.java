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

package com.openexchange.contact.provider.test.impl;

import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONObject;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.AutoProvisioningContactsProvider;
import com.openexchange.contact.provider.ContactsAccessCapability;
import com.openexchange.contact.provider.ContactsProvider;
import com.openexchange.contact.provider.basic.BasicContactsAccess;
import com.openexchange.contact.provider.basic.BasicContactsProvider;
import com.openexchange.contact.provider.basic.ContactsSettings;
import com.openexchange.contact.provider.test.impl.utils.TestContacts;
import com.openexchange.contact.provider.test.storage.TestContactsStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link TestContactsProvider} - An "in memory" implementation of {@link ContactsProvider} for testing purpose only
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class TestContactsProvider implements AutoProvisioningContactsProvider, BasicContactsProvider {

    public static final String PROVIDER_ID = "com.openexchange.contact.provider.test";
    public static final String PROVIDER_DISPLAY_NAME = "c.o.contact.provider.test";

    private final TestContactsStorage storage;

    /**
     * Initializes a new {@link TestContactsProvider}.
     */
    public TestContactsProvider() {
        this.storage = new TestContactsStorage(TestContacts.TEST_DATA);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return PROVIDER_DISPLAY_NAME;
    }

    @Override
    public EnumSet<ContactsAccessCapability> getCapabilities() {
        return ContactsAccessCapability.getCapabilities(TestContactsAccess.class);
    }

    @Override
    public void onAccountDeleted(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException {
        // no-op
    }

    @Override
    public void onAccountDeleted(Context context, ContactsAccount account, ContactsParameters parameters) throws OXException {
        // no-op
    }

    @Override
    public JSONObject autoConfigureAccount(Session session, JSONObject userConfig, ContactsParameters parameters) throws OXException {
        return new JSONObject();
    }

    @Override
    public ContactsSettings probe(Session session, ContactsSettings settings, ContactsParameters parameters) throws OXException {
        return settings;
    }

    @Override
    public JSONObject configureAccount(Session session, ContactsSettings settings, ContactsParameters parameters) throws OXException {
        return new JSONObject();
    }

    @Override
    public JSONObject reconfigureAccount(Session session, ContactsAccount account, ContactsSettings settings, ContactsParameters parameters) throws OXException {
        return new JSONObject();
    }

    @Override
    public BasicContactsAccess connect(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException {
        return new TestContactsAccess(storage, account);
    }
}
