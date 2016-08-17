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
     * @return The scope. If all requests are authorized to call this action
     * {@link OAuthAction#GRANT_ALL} must be returned.
     */
    String value();

}
