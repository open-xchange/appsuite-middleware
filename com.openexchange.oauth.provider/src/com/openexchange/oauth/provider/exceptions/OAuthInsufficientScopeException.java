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

import com.openexchange.exception.Category;


/**
 * {@link OAuthInsufficientScopeException}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthInsufficientScopeException extends OAuthRequestException {

    private static final long serialVersionUID = -5103133884480860890L;

    private final String requiredScope;

    public OAuthInsufficientScopeException() {
        this(null);
    }

    public OAuthInsufficientScopeException(String requiredScope) {
        super();
        this.requiredScope = requiredScope;
    }

    @Override
    public String getError() {
        return "insufficient_scope";
    }

    @Override
    public int getCode() {
        return 2;
    }

    /**
     * Gets the required scope that wasn't satisfied by the request.
     * If no certain scope was responsible for the exception,
     * <code>null</code> is returned.
     *
     * @return The required scope
     */
    public String getScope() {
        return requiredScope;
    }

    @Override
    public Category getCategory() {
        return Category.CATEGORY_PERMISSION_DENIED;
    }

}
