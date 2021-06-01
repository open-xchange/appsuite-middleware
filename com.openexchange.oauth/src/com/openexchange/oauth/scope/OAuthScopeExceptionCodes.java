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

package com.openexchange.oauth.scope;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link OAuthScopeExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum OAuthScopeExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * No scopes found for the '%1$s' OAuth service.
     */
    NO_SCOPES("No scopes found for the '%1$s' OAuth service.", Category.CATEGORY_ERROR, 1),
    /**
     * No scope found for module '%1$s' in the '%2$s' OAuth service
     */
    NO_SCOPE_FOR_MODULE("No scope found for module '%1$s' in the '%2$s' OAuth service", Category.CATEGORY_ERROR, 2),
    /**
     * The specified string '%1$s' cannot be resolved to a valid module. Valid modules are: '%2$s'
     */
    CANNOT_RESOLVE_MODULE("The specified string '%1$s' cannot be resolved to a valid module. Valid modules are: '%2$s'", CATEGORY_ERROR, 3),
    /**
     * No legacy scopes found for the '%1$s' OAuth service.
     */
    NO_LEGACY_SCOPES("No legacy scopes found for the '%1$s' OAuth service.", Category.CATEGORY_ERROR, 4),
    ;

    private final Category category;
    private final int number;
    private final String message;
    private String displayMessage;
    private static final String PREFIX = "OAUTH_SCOPE_REGISTRY";

    /**
     * Initialises a new {@link OAuthScopeExceptionCodes}.
     */
    private OAuthScopeExceptionCodes(String message, Category category, int detailNumber) {
        this.message = message;
        this.category = category;
        number = detailNumber;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
