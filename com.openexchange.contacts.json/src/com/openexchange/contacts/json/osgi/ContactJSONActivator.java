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

package com.openexchange.contacts.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.converters.ContactInsertDataHandler;
import com.openexchange.contacts.json.converters.ContactJSONDataHandler;
import com.openexchange.contacts.json.converters.ContactJSONResultConverter;
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

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContactService.class, VCardService.class, ConfigViewFactory.class };
    }

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
        Dictionary<String, Object> props = new Hashtable<String, Object>(1);
        props.put("identifier", "com.openexchange.contact.json");
        registerService(DataHandler.class, new ContactJSONDataHandler(this), props);
        props = new Hashtable<String, Object>(1);
        props.put("identifier", "com.openexchange.contact");
        registerService(DataHandler.class, new ContactInsertDataHandler(this), props);
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
