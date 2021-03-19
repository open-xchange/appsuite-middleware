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
 *    trademarks of the OX Software GmbH. group of companies.
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
