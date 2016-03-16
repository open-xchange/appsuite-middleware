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

package com.openexchange.uadetector.internal;

import java.util.concurrent.TimeUnit;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.java.Strings;
import com.openexchange.uadetector.UserAgentParser;


/**
 * {@link CachingUserAgentParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CachingUserAgentParser implements UserAgentStringParser, UserAgentParser {

    private final UserAgentStringParser parser;
    private final Cache<String, ReadableUserAgent> cache;

    /**
     * Initializes a new {@link CachingUserAgentParser}.
     *
     * @param onlineUpdate <code>true</code> to enable periodic online updates, <code>false</code>, otherwise
     */
    public CachingUserAgentParser(boolean onlineUpdate) {
        super();
        if (onlineUpdate) {
            parser = UADetectorServiceFactory.getCachingAndUpdatingParser();
        } else {
            parser = UADetectorServiceFactory.getResourceModuleParser();
        }
        cache = CacheBuilder.newBuilder().maximumSize(250).expireAfterWrite(2, TimeUnit.HOURS).build();
    }

    @Override
    public String getDataVersion() {
        return parser.getDataVersion();
    }

    @Override
    public ReadableUserAgent parse(String userAgent) {
        if (Strings.isEmpty(userAgent)) {
            return UserAgent.EMPTY;
        }
        ReadableUserAgent readableAgent = cache.getIfPresent(userAgent);
        if (null == readableAgent) {
            readableAgent = parser.parse(userAgent);
            cache.put(userAgent, readableAgent);
        }
        return readableAgent;
    }

    @Override
    public void shutdown() {
        parser.shutdown();
    }

    @Override
    public boolean matches(String userAgent, UserAgentFamily family) {
        return family == parse(userAgent).getFamily();
    }

    @Override
    public boolean matches(String userAgent, UserAgentFamily family, int majorVersion) {
        ReadableUserAgent readableAgent = parse(userAgent);
        if (family == readableAgent.getFamily() && null != readableAgent.getVersionNumber()) {
            try {
                return majorVersion == Integer.parseInt(readableAgent.getVersionNumber().getMajor());
            } catch (NumberFormatException e) {
                // no or no numerical major version
            }
        }
        return false;
    }

}
