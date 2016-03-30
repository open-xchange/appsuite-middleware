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

package com.openexchange.contact.vcard.storage.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.impl.DefaultVCardStorageMetadataStore;
import com.openexchange.contact.vcard.storage.impl.DefaultVCardStorageService;
import com.openexchange.contact.vcard.storage.impl.DefaultVCardStorageFactory;
import com.openexchange.contact.vcard.storage.impl.VCardCleaner;
import com.openexchange.contact.vcard.storage.impl.VCardFilestoreLocationUpdater;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * {@link ContactVCardStorageActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ContactVCardStorageActivator extends HousekeepingActivator {

    private static final String COM_OPENEXCHANGE_CONTACT_STORE_V_CARDS = "com.openexchange.contact.storeVCards";

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactVCardStorageActivator.class);

    /**
     * Initializes a new {@link ContactVCardStorageActivator}.
     */
    public ContactVCardStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, DatabaseService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.contact.vcard.storage.impl");

            registerService(FileLocationHandler.class, new VCardFilestoreLocationUpdater());
            VCardStorageFactory vCardStorageFactory = new DefaultVCardStorageFactory(new DefaultVCardStorageService());
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] { VCardCleaner.EVENT_TOPIC });
            registerService(EventHandler.class, new VCardCleaner(getService(ConfigViewFactory.class), vCardStorageFactory), serviceProperties);
            registerService(VCardStorageMetadataStore.class, new DefaultVCardStorageMetadataStore(getService(DatabaseService.class)));

            boolean enabled = getService(ConfigurationService.class).getBoolProperty(COM_OPENEXCHANGE_CONTACT_STORE_V_CARDS, true);
            if (enabled) {
                registerService(VCardStorageFactory.class, vCardStorageFactory);
            }
        } catch (Exception exception) {
            LOG.error("error starting com.openexchange.contact.vcard.storage.impl", exception);
            throw exception;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.contact.vcard.storage.impl");

        super.stopBundle();
    }
}
