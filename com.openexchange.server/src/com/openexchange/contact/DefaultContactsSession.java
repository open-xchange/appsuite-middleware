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
