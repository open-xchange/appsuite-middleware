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

package com.openexchange.login.listener;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;

/**
 * {@link LoginListener} - A login listener, which receives call-backs for different phases of the login operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface LoginListener {

    /**
     * Invoked during login operation right before actual authentication is performed.
     *
     * @param request The associated login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     * @throws OXException If this call-back renders the login attempt as invalid
     */
    void onBeforeAuthentication(LoginRequest request, Map<String, Object> properties) throws OXException;

    /**
     * Invoked during login operation in case authentication succeeded.
     *
     * @param result The login result
     * @throws OXException If this call-back renders the login attempt as invalid
     */
    void onSucceededAuthentication(LoginResult result) throws OXException;

    /**
     * Invoked during login operation in case authentication failed.
     *
     * @param request The associated login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     * @param e The optional exception rendering the login attempt as invalid; e.g. <code>null</code> in case authentication service signaled <code>ResultCode.FAILED</code>
     * @throws OXException If this call-back is supposed to signal a different error
     */
    void onFailedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException;

    /**
     * Invoked during login operation in case authentication gets redirected to another end-point.
     *
     * @param request The associated login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     * @param e The optional exception rendering the login attempt as redirected; e.g. <code>null</code> in case authentication service signaled <code>ResultCode.REDIRECT</code>
     * @throws OXException If this call-back is supposed to signal a different error
     */
    void onRedirectedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException;
}
