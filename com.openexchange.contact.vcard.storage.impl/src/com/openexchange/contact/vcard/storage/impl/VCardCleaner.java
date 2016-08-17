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

package com.openexchange.contact.vcard.storage.impl;

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
                    if (!Strings.isEmpty(vCardID)) {
                        try {
                            VCardStorageService vCardStorageService = vCardStorageFactory.getVCardStorageService(configViewFactory, contextID);
                            if (vCardStorageService != null) {
                                vCardStorageService.deleteVCard(vCardID, contextID);
                            }
                        } catch (OXException oxException) {
                            LOG.warn("Error while deleting the VCard with id {} in context {} from storage.", vCardID, contextID, oxException);
                        }
                    }
                }
            }
        }
    }
}
