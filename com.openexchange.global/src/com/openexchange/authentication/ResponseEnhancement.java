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

package com.openexchange.authentication;

/**
 * An interface which can be used to add further response details to the {@link Authenticated} return object of a authentication bundle.
 * This information is used to redirect the client or add additional headers and/or cookies to the response for the client.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public interface ResponseEnhancement {

    /**
     * Indicates how the response to the client should look like. See {@link ResultCode} for possible options.
     *
     * @return how to modify the response to the client.
     */
    ResultCode getCode();

    /**
     * Headers that should be part of the response to the client.
     *
     * @return headers that should be added to the client response. Never return <code>null</code> but an empty array.
     */
    Header[] getHeaders();

    /**
     * Cookies that should be part of the response to the client.
     *
     * @return cookies that should be added to the client response. Never return <code>null</code> but an empty array.
     */
    Cookie[] getCookies();

    /**
     * Contains the URL the client should be redirected to if {@link #getCode()} indicates {@link ResultCode#REDIRECT}. If {@link #getCode()}
     * indicates another result, this value can be <code>null</code>.
     *
     * @return URL to that the client should be redirected.
     */
    String getRedirect();

}
