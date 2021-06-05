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

import java.util.EnumMap;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.user.User;

/**
 * {@link UserMapper}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UserMapper extends DefaultDbMapper<User, UserField> {

    @Override
    public User newInstance() {
        return new UserImpl();
    }

    @Override
    public UserField[] newArray(int size) {
        return new UserField[size];
    }

    @Override
    protected EnumMap<UserField, ? extends DbMapping<? extends Object, User>> createMappings() {
        EnumMap<UserField, DbMapping<? extends Object, User>> mapping = new EnumMap<UserField, DbMapping<? extends Object, User>>(UserField.class);
        mapping.put(UserField.IMAP_SERVER, new IMAPServerMapping());
        mapping.put(UserField.IMAP_LOGIN, new IMAPLoginMapping());
        mapping.put(UserField.PREFERRED_LANGUAGE, new PreferredLanguageMapping());
        mapping.put(UserField.SMTP_SERVER, new SMTPServerMapping());
        mapping.put(UserField.TIME_ZONE, new TimeZoneMapping());
        return mapping;
    }
}
