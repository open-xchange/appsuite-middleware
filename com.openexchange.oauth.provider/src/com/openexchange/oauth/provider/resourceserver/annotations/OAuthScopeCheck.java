/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
