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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link TokenLoginV2Response}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class TokenLoginV2Response extends AbstractAJAXResponse {

    private final int status;
    private final String redirectUrl;
    private final boolean loginSuccessful;

    /**
     * Initializes a new {@link TokenLoginV2Response}.
     * 
     * @param response
     */
    public TokenLoginV2Response(int status, String redirectUrl, boolean loginSuccessful) {
        super(null);
        this.status = status;
        this.redirectUrl = redirectUrl;
        this.loginSuccessful = loginSuccessful;
    }

    public int getStatus() {
        return status;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

}
