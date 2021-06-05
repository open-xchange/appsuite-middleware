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

package com.openexchange.uadetector.internal;

import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.java.Strings;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;


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
