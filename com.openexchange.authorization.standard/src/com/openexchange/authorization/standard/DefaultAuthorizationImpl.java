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

package com.openexchange.authorization.standard;

import static com.openexchange.java.Autoboxing.I;
import java.lang.reflect.UndeclaredThrowableException;
import com.openexchange.authorization.AuthorizationExceptionCodes;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.context.ContextExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;


/**
 * {@link DefaultAuthorizationImpl}
 *
 */
public final class DefaultAuthorizationImpl implements AuthorizationService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuthorizationImpl.class);

    private static final DefaultAuthorizationImpl INSTANCE = new DefaultAuthorizationImpl();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DefaultAuthorizationImpl getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link DefaultAuthorizationImpl}.
     */
    private DefaultAuthorizationImpl() {
        super();
    }

    /**
     * @param ctx
     * @param user
     * @throws OXException
     */
    @Override
    public void authorizeUser(final Context ctx, final User user) throws OXException {
        try {
            if (!ctx.isEnabled()) {
                LOG.debug("Context {} ({}) is disabled.", Integer.valueOf(ctx.getContextId()), ctx.getName());
                throw AuthorizationExceptionCodes.USER_DISABLED.create(ContextExceptionCodes.CONTEXT_DISABLED.create(I(ctx.getContextId()), ctx.getName()), I(user.getId()), I(ctx.getContextId()));
            }
        } catch (UndeclaredThrowableException e) {
            throw AuthorizationExceptionCodes.UNKNOWN.create(e);
        }
        if (!user.isMailEnabled()) {
            throw AuthorizationExceptionCodes.USER_DISABLED.create(I(user.getId()), I(ctx.getContextId()));
        }
        if (user.getShadowLastChange() == 0) {
            throw AuthorizationExceptionCodes.PASSWORD_EXPIRED.create();
        }
    }

}
