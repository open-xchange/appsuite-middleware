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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * In most cases the permission to call an action can be based on a simple scope
 * string as defined via {@link OAuthAction}. In cases where this is not sufficient,
 * it is possible to check the scope manually. In order to do so you must
 * <ul>
 *   <li>Set the scope of {@link OAuthAction} to {@link OAuthAction#CUSTOM}</li>
 *   <li>
 *     Implement a method to perform the scope check and annotate it with <code>@OAuthScopeCheck</code>.
 *     The method
 *     <ul>
 *       <li>must be public</li>
 *       <li>must return a boolean - <code>true</code> if access is granted, <code>false</code> if not</li>
 *       <li>
 *         must take three parameters (in that order):
 *         <ul>
 *           <li><code>com.openexchange.ajax.requesthandler.AJAXRequestData</code></li>
 *           <li><code>com.openexchange.tools.session.ServerSession</code></li>
 *           <li><code>com.openexchange.oauth.provider.OAuthGrant</code></li>
 *         </ul>
 *       </li>
 *       <li>may throw an Exception</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * Example:
 * <pre>
 * &#064;OAuthScopeCheck
 * public boolean checkScope(AJAXRequestData request, ServerSession session, OAuthGrant grant) {
 *     return grant.getScopes().contains("custom_scope1") && grant.getScopes().contains("custom_scope2");
 * }
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OAuthScopeCheck {

}
