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

package com.openexchange.groupware.settings.impl;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;

/**
 * This class defines the interface to the storage for user specific settings.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class SettingStorage {

    /**
     * Default constructor.
     */
    protected SettingStorage() {
        super();
    }

    /**
     * This method stores a specific setting.
     * @param setting the setting to store.
     * @throws OXException if an error occurs while saving the setting.
     */
    public abstract void save(Setting setting) throws OXException;

    /**
     * This method stores a specific setting.
     * @param con writable database connection.
     * @param setting the setting to store.
     * @throws OXException if an error occurs while saving the setting.
     */
    public abstract void save(Connection con, Setting setting) throws
        OXException;

    /**
     * This method reads the setting and its subsettings from the database.
     * @param setting setting to read.
     * @throws OXException if an error occurs while reading the setting.
     */
    public abstract void readValues(Setting setting)
        throws OXException;

    /**
     * This method reads the setting and its subsettings from the database.
     * @param con database connection.
     * @param setting setting to read.
     * @throws OXException if an error occurs while reading the setting.
     */
    public abstract void readValues(Connection con, Setting setting)
        throws OXException;

    /**
     * @param session Session.
     * @return an instance implementing this storage interface.
     */
    public static SettingStorage getInstance(final Session session) {
        try {
            return new RdbSettingStorage(session);
        } catch (final OXException e) {
            throw new RuntimeException(e);
        }
    }

    public static SettingStorage getInstance(final Session session,
        final Context ctx, final User user, final UserConfiguration userConfig) {
        return new RdbSettingStorage(session, ctx, user, userConfig);
    }

    /**
     * @param contextId unique identifier of the context.
     * @param userId unique identifier of the user.
     * @return an instance implementing this storage interface.
     */
    public static SettingStorage getInstance(final int contextId,
        final int userId) {
        return new RdbSettingStorage(contextId, userId);
    }
}
