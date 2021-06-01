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

package com.openexchange.contacts.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.common.DataHandlers;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.converters.ContactInsertDataHandler;
import com.openexchange.contacts.json.converters.ContactJSONDataHandler;
import com.openexchange.contacts.json.converters.ContactJSONResultConverter;
import com.openexchange.contacts.json.converters.Json2OXExceptionDataHandler;
import com.openexchange.contacts.json.converters.Json2XPropertiesDataHandler;
import com.openexchange.contacts.json.converters.OXException2JsonDataHandler;
import com.openexchange.contacts.json.converters.XProperties2JsonDataHandler;
import com.openexchange.conversion.DataHandler;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;

/**
 * {@link ContactJSONActivator} - OSGi Activator for the Contact JSON interface.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactJSONActivator extends AJAXModuleActivator {

    private static final String IDENTIFIER = "identifier";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContactService.class, VCardService.class, ConfigViewFactory.class, IDBasedContactsAccessFactory.class };
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void startBundle() throws Exception {
        /*
         * register ajax module
         */
        registerModule(new ContactActionFactory(this), "contacts");
        /*
         * register result converter & data handler
         */
        registerService(ResultConverter.class, new ContactJSONResultConverter());
        registerService(DataHandler.class, new ContactJSONDataHandler(this), singletonDictionary(IDENTIFIER, DataHandlers.CONTACT2JSON.getId()));
        registerService(DataHandler.class, new ContactInsertDataHandler(this), singletonDictionary(IDENTIFIER, DataHandlers.CONTACT.getId()));
        registerService(DataHandler.class, new XProperties2JsonDataHandler(), singletonDictionary(IDENTIFIER, DataHandlers.XPROPERTIES2JSON.getId()));
        registerService(DataHandler.class, new Json2XPropertiesDataHandler(), singletonDictionary(IDENTIFIER, DataHandlers.JSON2XPROPERTIES.getId()));
        registerService(DataHandler.class, new OXException2JsonDataHandler(), singletonDictionary(IDENTIFIER, DataHandlers.OXEXCEPTION2JSON.getId()));
        registerService(DataHandler.class, new Json2OXExceptionDataHandler(), singletonDictionary(IDENTIFIER, DataHandlers.JSON2OXEXCEPTION.getId()));
        /*
         * define oauth scopes
         */
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ContactActionFactory.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.CONTACTS.getCapabilityName());
            }
        });
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ContactActionFactory.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.CONTACTS.getCapabilityName());
            }
        });
        /*
         * track vCard storage service
         */
        track(VCardStorageFactory.class);
        openTrackers();
    }
}
