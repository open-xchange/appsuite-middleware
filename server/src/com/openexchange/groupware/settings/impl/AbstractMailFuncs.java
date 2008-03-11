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

package com.openexchange.groupware.settings.impl;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

/**
 * This class contains the shared, same functions for all mail bit settings.
 */
public abstract class AbstractMailFuncs implements IValueHandler {

    /**
     * Default constructor.
     */
    protected AbstractMailFuncs() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void getValue(final Session session, final Context ctx,
        final User user, final UserConfiguration userConfig,
        final Setting setting) throws SettingException {
        final UserSettingMail settings;
        try {
            settings = UserSettingMailStorage.getInstance().loadUserSettingMail(
                session.getUserId(), ctx);
        } catch (final UserConfigurationException e) {
            throw new SettingException(e);
        }
		if (userConfig.hasWebMail()) {
			setting.setSingleValue(isSet(settings));
		}
	}

    /**
	 * @param settings
	 *            in this mail settings the bit will be requested.
	 * @return the value of the bit.
	 */
    protected abstract Object isSet(UserSettingMail settings);

    /**
     * {@inheritDoc}
     */
    public boolean isWritable() {
        return true;
    }

    /**
	 * {@inheritDoc}
	 */
	public void writeValue(final Context ctx, final User user,
	    final Setting setting) throws SettingException {
        final UserSettingMailStorage storage = UserSettingMailStorage
            .getInstance();
		final UserSettingMail settings;
        try {
            settings = storage.loadUserSettingMail(user.getId(), ctx);
        } catch (final UserConfigurationException e) {
            throw new SettingException(e);
        }
		setValue(settings, (String) setting.getSingleValue());
		try {
			storage.saveUserSettingMail(settings, user.getId(), ctx);
		} catch (OXException e) {
			throw new SettingException(e);
		}
	}

	/**
	 * @param settings
	 *            in this mail settings the bit will be set.
	 * @param value
	 *            value of the bit that should be set.
	 */
    protected abstract void setValue(UserSettingMail settings,
        String value);

    /**
     * {@inheritDoc}
     */
    public int getId() {
        return -1;
    }
}