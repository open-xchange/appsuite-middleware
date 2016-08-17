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

package com.openexchange.uadetector;

import com.openexchange.osgi.annotation.SingletonService;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;


/**
 * {@link UserAgentParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
@SingletonService
public interface UserAgentParser {

    /**
     * Parses and analyzes a <code>User-Agent</code> string sent along with an <code>HttpServletRequest</code>, wrapping the result into
     * a readable user agent structure.
     *
     * @param userAgent The user agent string as sent by the client in the <code>User-Agent</code>-header
     * @return The readable user agent, or a placeholder instance if unknown
     * @see UserAgentStringParser#parse(String)
     */
    ReadableUserAgent parse(String userAgent);

    /**
     * Gets a value indicating whether a user agent string matches a specific user agent family.
     *
     * @param userAgent The user agent string as sent by the client in the <code>User-Agent</code>-header
     * @param family The user agent family to match
     * @return <code>true</code> if the user agent family matches, <code>false</code>, otherwise
     */
    boolean matches(String userAgent, UserAgentFamily family);

    /**
     * Gets a value indicating whether a user agent string matches the supplied user agent family in a specific major version.
     *
     * @param userAgent The user agent string as sent by the client in the <code>User-Agent</code>-header
     * @param family The user agent family to match
     * @param majorVersion The major (first) version number to match
     * @return <code>true</code> if the user agent family and major version matches, <code>false</code>, otherwise
     */
    boolean matches(String userAgent, UserAgentFamily family, int majorVersion);

}
