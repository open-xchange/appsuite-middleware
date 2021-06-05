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

package com.openexchange.api.client.impl;

import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.api.client.common.calls.user.UserInformation;

/**
 * {@link CompositingLoginInformation}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class CompositingLoginInformation implements LoginInformation {

    private LoginInformation delegatee;
    private UserInformation userInformation;

    /**
     * Initializes a new {@link CompositingLoginInformation}.
     * 
     * @param loginInformation The original login information
     * @param userInformation Specific user information retrieved to the WhoAmI call
     */
    public CompositingLoginInformation(LoginInformation loginInformation, UserInformation userInformation) {
        super();
        delegatee = loginInformation;
        this.userInformation = userInformation;
    }

    @Override
    @Nullable
    public String getRemoteSessionId() {
        return delegatee.getRemoteSessionId();
    }

    @Override
    @Nullable
    public String getRemoteMailAddress() {
        return userInformation.getEmail1();
    }

    @Override
    public int getRemoteUserId() {
        return userInformation.getId();
    }

    @Override
    public int getRemoteContextId() {
        return delegatee.getRemoteContextId();
    }

    @Override
    @Nullable
    public String getRemoteFolderId() {
        return delegatee.getRemoteFolderId();
    }

    @Override
    public String getModule() {
        return delegatee.getModule();
    }

    @Override
    public String getItem() {
        return delegatee.getItem();
    }

    @Override
    public String getLoginType() {
        return delegatee.getLoginType();
    }

    @Override
    @Nullable
    public String getAdditional(String key) {
        return delegatee.getAdditional(key);
    }
}
