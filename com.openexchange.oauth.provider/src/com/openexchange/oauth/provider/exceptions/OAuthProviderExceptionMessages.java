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

package com.openexchange.oauth.provider.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OAuthProviderExceptionMessages} - Exception messages that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class OAuthProviderExceptionMessages implements LocalizableStrings {

    // You reached the max. number of 100 possible grants for 3rd party applications. Please revoke access for the ones you don't longer need.
    public static final String GRANTS_EXCEEDED_MSG = "You reached the max. number of %3$d possible grants for 3rd party applications. Please revoke access for the ones you don't longer need.";

    /**
     * Initializes a new {@link OAuthProviderExceptionMessages}.
     */
    private OAuthProviderExceptionMessages() {
        super();
    }

}
