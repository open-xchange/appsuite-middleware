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

package com.openexchange.groupware.settings;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SharedNode implements IValueHandler {

    private final String name;

    private final int id;

    public SharedNode(String name) {
        this(name, -1);
    }

    public SharedNode(String name, int id) {
        super();
        this.name = name;
        this.id = id;
    }

    @Override
    public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
        throw SettingExceptionCodes.NOT_LEAF.create(name);
    }

    @Override
    public boolean isAvailable(UserConfiguration userConfig) {
        return true;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException {
        throw SettingExceptionCodes.NO_WRITE.create(name);
    }

    @Override
    public int getId() {
        return id;
    }
}
