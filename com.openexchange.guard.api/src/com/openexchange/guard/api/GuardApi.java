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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.guard.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link GuardApi} - Provides access to the OX Guard end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
@SingletonService
public interface GuardApi {

    /**
     * Performs the GET using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @throws OXException If GET fails
     * @see GuardApis#mapFor(String...)
     */
    <R> R doCallGet(Map<String, String> parameters, Class<? extends R> clazz) throws OXException;

    /**
     * Performs the PUT using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @throws OXException If PUT fails
     * @see GuardApis#mapFor(String...)
     */
    <R> R doCallPut(Map<String, String> parameters, JSONValue jsonBody, Class<? extends R> clazz) throws OXException;

    /**
     * Requests a resource from Guard end-point.
     *
     * @param parameters The request parameters
     * @return The resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     */
    InputStream requestResource(Map<String, String> parameters) throws OXException;

    /**
     * Performs the GET using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @throws OXException If GET fails
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    <R> R doCallSessionSensitiveGet(Map<String, String> parameters, Class<? extends R> clazz, Session session, List<Cookie> cookies) throws OXException;

    /**
     * Performs the PUT using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @throws OXException If PUT fails
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    <R> R doCallSessionSensitivePut(Map<String, String> parameters, JSONValue jsonBody, Class<? extends R> clazz, Session session, List<Cookie> cookies) throws OXException;

    /**
     * Requests a resource from Guard end-point.
     *
     * @param parameters The request parameters
     * @param session The associated session
     * @param cookies The needed cookies for user authentication; typically the secret and public cookie
     * @return The resource data
     * @throws OXException If resource data cannot be returned
     * @see GuardApis#mapFor(String...)
     * @see GuardApis#extractCookiesFrom(javax.servlet.http.HttpServletRequest, Session)
     */
    InputStream requestSessionSensitiveResource(Map<String, String> parameters, Session session, List<Cookie> cookies) throws OXException;

}
