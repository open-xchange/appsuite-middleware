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

package com.openexchange.share.impl.xctx;

import com.openexchange.authentication.GuestAuthenticated;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link XctxAuthenticated}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxAuthenticated implements GuestAuthenticated, SessionEnhancement {

    private final User user;
    private final Context context;

    /**
     * Initializes a new {@link XctxAuthenticated}.
     *
     * @param user The user
     * @param context The context
     */
    public XctxAuthenticated(User user, Context context) {
        super();
        this.user = user;
        this.context = context;
    }

    @Override
    public String getContextInfo() {
        String[] loginInfo = context.getLoginInfo();
        return null != loginInfo && 0 < loginInfo.length ? loginInfo[0] : context.getName();
    }

    @Override
    public String getUserInfo() {
        return user.getMail();
    }

    @Override
    public int getContextID() {
        return context.getContextId();
    }

    @Override
    public int getUserID() {
        return user.getId();
    }

    @Override
    public void enhanceSession(Session session) {
        session.setParameter(Session.PARAM_GUEST, Boolean.TRUE);
    }

}
