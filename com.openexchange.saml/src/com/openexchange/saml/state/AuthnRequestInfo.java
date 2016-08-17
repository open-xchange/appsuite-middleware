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

package com.openexchange.saml.state;


/**
 * Contains the available information about an already sent authentication request.
 * This is for example used to assign responses to their according requests, i.e.
 * to validate InResponseTo attributes of response objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see DefaultAuthnRequestInfo
 */
public interface AuthnRequestInfo {

    /**
     * Gets the unique ID of the AuthnRequest.
     *
     * @return The ID
     */
    String getRequestId();

    /**
     * Gets the domain name via which the HTTP request initiating the AuthnRequest was
     * received. This domain is later on used to redirect to the <code>redeemReservation</code>
     * login action.
     *
     * @return The domain name
     */
    String getDomainName();

    /**
     * Gets the path that is later on passed as <code>uiWebPath</code> parameter to the
     * <code>redeemReservation</code>. This action will redirect the client to this path
     * and append the session ID and other user-specific parameters as fragment parameters.
     *
     * @return The path or <code>null</code> to omit the <code>uiWebPath</code> parameter
     * and force the default. This will be the configured <code>com.openexchange.UIWebPath</code>.
     */
    String getLoginPath();

    /**
     * Gets the client identifier that shall be assigned with the session to be created.
     *
     * @return The client ID or <code>null</code> if none was provided. In this case the
     * configured default will be used, i.e. the value of <code>com.openexchange.ajax.login.http-auth.client</code>
     * in <code>login.properties</code>.
     */
    String getClientID();

}
