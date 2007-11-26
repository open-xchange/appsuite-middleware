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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.settings.tree;

import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.settings.SettingSetup;
import com.openexchange.groupware.settings.SharedValue;
import com.openexchange.session.Session;

/**
 * Adds a configuration tree entry for the contact identifier of the user.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ContactId extends AbstractNode {

    public static final String NAME = "contact_id";

    /**
     * Default constructor.
     */
    public ContactId() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SettingSetup[] getParents() {
        return new SettingSetup[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharedValue getSharedValue() {
        return new ReadOnlyValue() {
            /**
             * {@inheritDoc}
             */
            public boolean isAvailable(final Session session) {
                return true;
            }
            /**
             * {@inheritDoc}
             */
            public void getValue(final Session session, final Setting setting)
                throws SettingException {
                try {
                    final UserStorage stor = UserStorage.getInstance();
                    final User user = stor.getUser(session.getUserId(), session
                        .getContext());
                    setting.setSingleValue(Integer.valueOf(user.getContactId()));
                } catch (LdapException e) {
                    throw new SettingException(e);
                }
            }
        };
    }
}
