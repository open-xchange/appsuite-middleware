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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.tools.dav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link DAVTools}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DAVTools {

    private DAVTools() {};

    private static final Logger LOGGER = LoggerFactory.getLogger(DAVTools.class);

    public final static String PREFIX_PATH_NAME = "com.openexchange.dav.pathPrefix";
    public final static String DEFAULT_PREFIX_PATH = "/servlet/dav/";

    /**
     * Get the DAV path prefix
     * 
     * @param configViewFactory The {@link ConfigViewFactory} to use
     * @return The configured value or {@value #DEFAULT_PREFIX_PATH} as default value
     */
    public static String getPathPrefix(ConfigViewFactory configViewFactory) {
        if (null == configViewFactory) {
            LOGGER.warn("LeanConfigurationService is not available.");
            return DEFAULT_PREFIX_PATH;
        }
        try {
            String prefix = configViewFactory.getView().get(PREFIX_PATH_NAME, String.class);
            if (Strings.isNotEmpty(prefix)) {
                return ensureQualifiedPath(prefix);
            }
        } catch (OXException e) {
            LOGGER.warn("ConfigView is not available.", e);
        }
        LOGGER.debug("\"{}\" not configured, using default value.", PREFIX_PATH_NAME);
        return DEFAULT_PREFIX_PATH;
    }

    public final static String PROXY_PREFIX_PATH_NAME = "com.openexchange.dav.proxyPrefixPath";
    public final static String DEFAULT_PROXY_PREFIX_PATH = "/servlet/dav/";

    /**
     * Get the DAV proxy path prefix
     * 
     * @param configViewFactory The {@link ConfigViewFactory} to use
     * @return The configured value or {@value #DEFAULT_PROXY_PREFIX_PATH} as default value
     */
    public static String getProxyPrefixPath(ConfigViewFactory configViewFactory) {
        if (null == configViewFactory) {
            return DEFAULT_PROXY_PREFIX_PATH;
        }

        try {
            String proxyPrefixPath = configViewFactory.getView().get(PROXY_PREFIX_PATH_NAME, String.class);
            if (Strings.isNotEmpty(proxyPrefixPath)) {
                return ensureQualifiedPath(proxyPrefixPath);
            }
        } catch (OXException e) {
            LOGGER.warn("ConfigView is not available.", e);
        }
        LOGGER.debug("\"{}\" not configured, using default value.", PROXY_PREFIX_PATH_NAME);
        return DEFAULT_PROXY_PREFIX_PATH;
    }

    /**
     * Adjusts the path according to the configuration.
     *
     * @param configViewFactory The configuration
     * @param path The path to adjust accordingly
     * @return The adjusted path
     */
    public static String insertPrefixPath(ConfigViewFactory configViewFactory, String path) {
        if (null == configViewFactory) {
            return adjustPath(path);
        }

        /*
         * If both path are equal, do nothing ..
         */
        String pathPrefix = getPathPrefix(configViewFactory);
        String proxyPrefixPath = getProxyPrefixPath(configViewFactory);
        if (pathPrefix.equals(proxyPrefixPath)) {
            return adjustPath(path);
        }

        /*
         * Avoid multiple slashes, prefix always ends with slash
         */
        StringBuilder sb = new StringBuilder(pathPrefix);
        if (path.startsWith("/")) {
            sb.append(path.substring(1));
        } else {
            sb.append(path);
        }

        /*
         * Add the prefix and remove the proxy prefix afterwards
         */
        return removeProxyPrefixPath(sb.toString(), proxyPrefixPath);
    }

    /**
     * Removes the prefix from the given path
     * 
     * @param path The path
     * @param prefix The prefix to remove
     * @return The path without the prefix
     */
    public static String removeProxyPrefixPath(String path, String prefix) {
        if (Strings.isEmpty(path) || Strings.isEmpty(prefix) || false == path.startsWith(prefix)) {
            return path;
        }
        return adjustPath(path.substring(prefix.length()));
    }

    /**
     * Ensures that the path begins and ends with <code>/</code>
     *
     * @param path The path to ensure a full qualified path on
     * @return The qualified path
     */
    private static String ensureQualifiedPath(String path) {
        if (Strings.isEmpty(path)) {
            return path;
        }
        String adjusted = adjustPath(path);
        if (false == adjusted.endsWith("/")) {
            adjusted = adjusted + "/";
        }
        return adjusted;
    }

    /**
     * Ensures that the path begins with a <code>/</code>
     *
     * @param path The path to ensure the slash on
     * @return The adjusted path
     */
    private static String adjustPath(String path) {
        if (Strings.isNotEmpty(path) && false == path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

}
