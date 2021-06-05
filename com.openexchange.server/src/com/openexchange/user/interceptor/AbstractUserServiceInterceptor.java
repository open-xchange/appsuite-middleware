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

package com.openexchange.user.interceptor;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link AbstractUserServiceInterceptor}
 *
 * Stub implementation of the {@link UserServiceInterceptor} interface.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractUserServiceInterceptor implements UserServiceInterceptor {

    /** The default interceptor ranking */
    protected static final int DEFAULT_RANKING = 100;

    @Override
    public int getRanking() {
        return DEFAULT_RANKING;
    }

    @Override
    public void beforeCreate(Context context, User user, Contact contactData) throws OXException {
        // no
    }

    @Override
    public void afterCreate(Context context, User user, Contact contactData) throws OXException {
        // no
    }

    @Override
    public void beforeUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        // no
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        // no
    }

    @Override
    public void beforeDelete(Context context, User user, Contact contactData) throws OXException {
        // no
    }

    @Override
    public void afterDelete(Context context, User user, Contact contactData) throws OXException {
        // no
    }

}
