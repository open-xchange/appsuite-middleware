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

package com.openexchange.oauth.access;

import java.util.concurrent.Callable;
import com.openexchange.exception.OXException;


/**
 * {@link InitializeCallable} - Initializes the specified {@code OAuthAccess} instance.
 * <p>
 * Intended to be used for {@link OAuthAccessRegistry#addIfAbsent(int, int, OAuthAccess, Callable)} call.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class InitializeCallable implements Callable<Void> {

    private final OAuthAccess oAuthAccess;

    /**
     * Initializes a new {@link InitializeCallable}.
     */
    public InitializeCallable(OAuthAccess oAuthAccess) {
        super();
        this.oAuthAccess = oAuthAccess;
    }

    @Override
    public Void call() throws OXException {
        oAuthAccess.initialize();
        return null;
    }

}
