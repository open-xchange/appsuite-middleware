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

package com.openexchange.user.internal.mapping;

import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.user.User;

/**
 * {@link IMAPLoginMapping}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class IMAPLoginMapping extends VarCharMapping<User> {

    public IMAPLoginMapping() {
        super("imapLogin", "IMAP login");
    }

    @Override
    public boolean isSet(User user) {
        return null != user.getImapLogin();
    }

    @Override
    public void set(User user, String value) {
        // Normally this method should only be called on objects created by {@link UserMapper#newInstance()}.
        if (user instanceof UserImpl) {
            ((UserImpl) user).setImapLogin(value);
        } else {
            throw new UnsupportedOperationException("com.openexchange.groupware.ldap.User.setImapLogin(String)");
        }
    }

    @Override
    public String get(User user) {
        return user.getImapLogin();
    }

    @Override
    public void remove(User user) {
        throw new UnsupportedOperationException();
    }
}
