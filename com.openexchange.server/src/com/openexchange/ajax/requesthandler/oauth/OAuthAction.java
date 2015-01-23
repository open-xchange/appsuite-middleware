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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.oauth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;


/**
 * <p>
 * This annotation indicates {@link AJAXActionService} that shall be available
 * for requests that use OAuth 2.0 authentication.<br>
 * <b>Please note:</b>
 * <ul>
 * <li>The according {@link AJAXActionServiceFactory} must be annotated with {@link OAuthModule}.</li>
 * <li>Allowed characters are %x21 / %x23-5B / %x5D-7E</li>
 * </ul>
 * </p>
 * <p>
 * Before an action is called, the used access token is verified in terms of the
 * needed OAuth 2.0 scope. The required scope to call an action is defined via
 * the annotations value, i.e. the result of {@link OAuthAction#value()}. It is
 * considered best practice to use the modules name prefixed by 'r_', 'w_' or 'rw_'
 * to require read, write or combined access (e.g. 'rw_contacts'). Scopes that follow
 * this notation are automatically considered as satisfied, if an access token has
 * the same or a higher scope (i.e. 'r_contacts' and 'w_contacts' are satisfied if
 * a request is authorized in scope 'rw_contacts'). If no certain scope is required,
 * the annotations value does not need to be set. In that case {@link OAuthAction#GRANT_ALL}
 * is returned.
 * </p>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OAuthAction {

    public static final String GRANT_ALL = "*";

    /**
     * Indicates the required OAuth 2.0 scope to call this action.
     * @return The scope. If all requests are authorized to call this action
     * {@link OAuthAction#GRANT_ALL} must be returned.
     */
    String value();

}
