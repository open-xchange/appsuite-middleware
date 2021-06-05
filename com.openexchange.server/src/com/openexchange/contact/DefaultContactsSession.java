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

package com.openexchange.contact;

import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.common.ContactsSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link DefaultContactsSession}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DefaultContactsSession implements ContactsSession {

    private final ContactsParameters parameters;
    private final ServerSession session;

    /**
     * Initializes a new {@link DefaultContactsSession}.
     * 
     * @param session The session
     */
    public DefaultContactsSession(Session session) throws OXException {
        this(session, null);
    }

    /**
     * Initializes a new {@link DefaultContactsSession}.
     * 
     * @param session The session
     * @param parameters The contacts parameters
     */
    public DefaultContactsSession(Session session, ContactsParameters parameters) throws OXException {
        super();
        this.parameters = null != parameters ? parameters : new DefaultContactsParameters();
        this.session = ServerSessionAdapter.valueOf(session);
    }

    @Override
    public <T> ContactsParameters set(String parameter, T value) {
        return parameters.set(parameter, value);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return parameters.get(parameter, clazz);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        return parameters.get(parameter, clazz, defaultValue);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.contains(parameter);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return parameters.entrySet();
    }

    @Override
    public ServerSession getSession() {
        return session;
    }

    @Override
    public int getUserId() {
        return session.getUserId();
    }

    @Override
    public int getContextId() {
        return session.getContextId();
    }

    //////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Checks that the session's user has permissions for the <i>contacts</i> module.
     *
     * @param session The session to check
     * @return The passed session, after the capability was checked
     * @throws OXException {@link ContactExceptionCodes#MISSING_CAPABILITY}
     */
    public static ServerSession hasContacts(ServerSession session) throws OXException {
        if (false == session.getUserPermissionBits().hasContact()) {
            throw ContactExceptionCodes.MISSING_CAPABILITY.create(com.openexchange.groupware.userconfiguration.Permission.CONTACTS.getCapabilityName());
        }
        return session;
    }

}
