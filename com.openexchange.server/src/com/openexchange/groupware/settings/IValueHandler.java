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

package com.openexchange.groupware.settings;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;

/**
 * Interface for settings that are shared between GUI and server.
 *
 * @see IValueHandlerExtended
 */
public interface IValueHandler {

    int NO_ID = -1;
    Object UNDEFINED = new Object();

    /**
     * @param session Session.
     * @param userConfig user configuration.
     * @param setting the value should be set in this setting object.
     * @throws OXException if an error occurs.
     */
    void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException;

    /**
     * @param session Session.
     * @return <code>true</code> if this setting is available due to {@link UserConfiguration}.
     * @see IValueHandlerExtended
     */
    boolean isAvailable(final UserConfiguration userConfig);

    /**
     * @return <code>true</code> if the setting can be written by the GUI.
     */
    boolean isWritable();

    /**
     * Write a new value to the setting.
     * @param session Session.
     * @param ctx Context.
     * @param user user object.
     * @param setting contains the value for the setting.
     * @throws OXException if the setting can't be written or an error occurs while writing the value.
     */
    void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException;

    /**
     * If the value should be written simply to the database and read from there a unique identifier must be returned instead of
     * implementing methods {@link #getValue(Session, Context, User, UserConfiguration, Setting)} and
     * {@link #writeValue(Context, User, Setting)}.
     *
     * @return the unique identifier of the value in the database.
     */
    int getId();
}
