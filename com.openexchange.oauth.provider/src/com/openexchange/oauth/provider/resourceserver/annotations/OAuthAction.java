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

package com.openexchange.oauth.provider.resourceserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation indicates <code>com.openexchange.ajax.requesthandler.AJAXActionService</code> that shall be available
 * for requests that use OAuth 2.0 authentication.<br>
 * <b>Please note:</b>
 * <ul>
 * <li>The according <code>com.openexchange.ajax.requesthandler.AJAXActionServiceFactory</code> must be annotated with {@link OAuthModule}.</li>
 * <li>Allowed scope characters are %x21 / %x23-5B / %x5D-7E</li>
 * </ul>
 * </p>
 * <p>
 * Before an action is called, the used access token is verified in terms of the
 * provided and needed OAuth 2.0 scopes. The required scope to call an action can easily be
 * defined via the annotations {@link OAuthAction#value()} attribute. If no certain scope is required,
 * {@link OAuthAction#GRANT_ALL} must be set. If a simple scope check is not sufficient, a custom check
 * can be implemented. See {@link OAuthScopeCheck} for details.
 * </p>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Deprecated
public @interface OAuthAction {

    /**
     * Grants access to any valid OAuth request.
     */
    public static final String GRANT_ALL = "*";

    /**
     * Indicates that a custom scope check is necessary.
     * See {@link OAuthScopeCheck} for details.
     */
    public static final String CUSTOM = "__custom__";

    /**
     * Indicates the required OAuth 2.0 scope to call this action.
     *
     * @return The scope. If all requests are authorized to call this action
     *         {@link OAuthAction#GRANT_ALL} must be returned.
     */
    String value();

}
