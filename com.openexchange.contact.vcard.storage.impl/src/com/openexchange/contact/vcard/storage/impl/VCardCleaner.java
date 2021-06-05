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

package com.openexchange.contact.vcard.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;

/**
 * {@link VCardCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.8.0
 */
public class VCardCleaner implements EventHandler {

    public static final String EVENT_TOPIC = "com/openexchange/groupware/contact/delete";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VCardCleaner.class);

    private final VCardStorageFactory vCardStorageFactory;

    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link VCardCleaner}.
     *
     * @param vCardStorageFactory The underlying vCard storage
     */
    public VCardCleaner(ConfigViewFactory configViewFactory, VCardStorageFactory vCardStorageFactory) {
        super();
        this.vCardStorageFactory = vCardStorageFactory;
        this.configViewFactory = configViewFactory;
    }

    @Override
    public void handleEvent(Event event) {
        if (null != event && EVENT_TOPIC.equals(event.getTopic()) && false == event.containsProperty(CommonEvent.REMOTE_MARKER)) {
            CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
            if (null != commonEvent && CommonEvent.DELETE == commonEvent.getAction()) {
                int contextID = commonEvent.getContextId();
                Contact contact = (Contact) commonEvent.getActionObj();
                if (null != contact) {
                    String vCardID = contact.getVCardId();
                    if (Strings.isNotEmpty(vCardID)) {
                        try {
                            VCardStorageService vCardStorageService = vCardStorageFactory.getVCardStorageService(configViewFactory, contextID);
                            if (vCardStorageService != null) {
                                vCardStorageService.deleteVCard(vCardID, contextID);
                            }
                        } catch (OXException oxException) {
                            LOG.warn("Error while deleting the VCard with id {} in context {} from storage.", vCardID, I(contextID), oxException);
                        }
                    }
                }
            }
        }
    }
}
