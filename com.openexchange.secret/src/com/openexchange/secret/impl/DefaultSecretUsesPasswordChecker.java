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

package com.openexchange.secret.impl;

import com.openexchange.secret.SecretService;
import com.openexchange.secret.SecretUsesPasswordChecker;

/**
 * {@link DefaultSecretUsesPasswordChecker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DefaultSecretUsesPasswordChecker implements SecretUsesPasswordChecker {

    private final TokenBasedSecretService tokenBasedSecretService;
    private final boolean usesPassword;

    /**
     * Initializes a new {@link DefaultSecretUsesPasswordChecker}.
     *
     * @param tokenBasedSecretService The token entry possibly using password
     * @param usesPassword Whether password is used or not
     */
    public DefaultSecretUsesPasswordChecker(TokenBasedSecretService tokenBasedSecretService, boolean usesPassword) {
        this.tokenBasedSecretService = tokenBasedSecretService;
        this.usesPassword = usesPassword;
    }

    @Override
    public boolean usesPassword() {
        return usesPassword;
    }

    @Override
    public SecretService passwordUsingSecretService() {
        return usesPassword ? tokenBasedSecretService : null;
    }
}