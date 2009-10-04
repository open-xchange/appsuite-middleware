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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.twitter.internal;

import java.util.regex.Pattern;
import twitter4j.Configuration;
import com.openexchange.config.ConfigurationService;

/**
 * {@link TwitterConfiguration} - Configuration of twitter bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterConfiguration {

    /**
     * Initializes a new {@link TwitterConfiguration}.
     */
    private TwitterConfiguration() {
        super();
    }

    /**
     * Configures twitter bundle.
     * 
     * @param configurationService The configuration service needed to read properties
     */
    public static void configure(final ConfigurationService configurationService) {
        {
            // twitter4j.clientVersion
            String property = configurationService.getProperty("com.openexchange.twitter.clientVersion");
            if (null == property) {
                Configuration.setProperty("twitter4j.clientVersion", "2.0.10");
            } else {
                property = property.trim();
                if (isValidVersionString(property)) {
                    Configuration.setProperty("twitter4j.clientVersion", property);
                } else {
                    // Not a valid version
                    Configuration.setProperty("twitter4j.clientVersion", "2.0.10");
                }
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.useSSL");
            if (null != property) {
                Configuration.setProperty("twitter4j.http.useSSL", Boolean.valueOf(property.trim()).toString());
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.proxyHost.fallback");
            if (null != property) {
                Configuration.setProperty("twitter4j.http.proxyHost.fallback", property.trim());
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.proxyPort.fallback");
            if (null != property) {
                Configuration.setProperty("twitter4j.http.proxyPort.fallback", property.trim());
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.connectionTimeout");
            if (null != property) {
                try {
                    Configuration.setProperty("twitter4j.http.connectionTimeout", Integer.valueOf(property.trim()).toString());
                } catch (final NumberFormatException e) {
                    // NAN
                    Configuration.setProperty("twitter4j.http.connectionTimeout", "20000");
                }
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.readTimeout");
            if (null != property) {
                try {
                    Configuration.setProperty("twitter4j.http.readTimeout", Integer.valueOf(property.trim()).toString());
                } catch (final NumberFormatException e) {
                    // NAN
                    Configuration.setProperty("twitter4j.http.readTimeout", "120000");
                }
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.retryCount");
            if (null != property) {
                try {
                    Configuration.setProperty("twitter4j.http.retryCount", Integer.valueOf(property.trim()).toString());
                } catch (final NumberFormatException e) {
                    // NAN
                    Configuration.setProperty("twitter4j.http.retryCount", "3");
                }
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.twitter.http.retryIntervalSecs");
            if (null != property) {
                try {
                    Configuration.setProperty("twitter4j.http.retryIntervalSecs", Integer.valueOf(property.trim()).toString());
                } catch (final NumberFormatException e) {
                    // NAN
                    Configuration.setProperty("twitter4j.http.retryIntervalSecs", "10");
                }
            }
        }

    }

    private static boolean isValidVersionString(final String version) {
        final StringBuilder sb = new StringBuilder(24);
        sb.append("[\\d\\w]+");
        sb.append('(');
        sb.append("[\\.,-]");
        sb.append("[\\d\\w]+");
        sb.append(")*");
        return version != null && Pattern.compile(sb.toString()).matcher(version).matches();
    }

}
