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

package com.openexchange.groupware.settings.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

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
    @Override
    public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
        final UserSettingMail settings = UserSettingMailStorage.getInstance().loadUserSettingMail(user.getId(), ctx);
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
    @Override
    public boolean isWritable() {
        return true;
    }

    /**
	 * {@inheritDoc}
	 */
	@Override
    public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
	    ServerSession serverSession = ServerSessionAdapter.valueOf(session);
	    if (serverSession.getUserConfiguration().hasWebMail()) {
            final UserSettingMailStorage storage = UserSettingMailStorage.getInstance();
            final int userId = user.getId();
            final UserSettingMail settings = storage.loadUserSettingMail(userId, ctx);
            final int prevBitsValue = settings.getBitsValue();
            if (setting.getSingleValue() == null) {
                return;
            }
            setValue(settings, setting.getSingleValue().toString());
            if (settings.getBitsValue() != prevBitsValue) {
                storage.saveUserSettingMailBits(settings, userId, ctx);
            }
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
    @Override
    public int getId() {
        return -1;
    }
}
