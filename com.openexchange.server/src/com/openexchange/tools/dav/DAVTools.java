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

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.isNotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;

/**
 * {@link DAVTools}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DAVTools {

    private DAVTools() {};

    private static final Logger LOGGER = LoggerFactory.getLogger(DAVTools.class);

    public final static String PREFIX_PATH_NAME = "com.openexchange.dav.prefixPath";
    public final static String DEFAULT_PREFIX_PATH = "/servlet/dav";

    /**
     * Get the DAV path prefix
     * 
     * @param configViewFactory The {@link ConfigViewFactory} to use
     * @return The configured value or {@value #DEFAULT_PREFIX_PATH} as default value
     */
    private static String getPathPrefix(ConfigViewFactory configViewFactory) {
        if (null == configViewFactory) {
            LOGGER.warn("ConfigViewFactory is not available.");
            return DEFAULT_PREFIX_PATH;
        }
        try {
            String prefix = configViewFactory.getView().get(PREFIX_PATH_NAME, String.class);
            if (isNotEmpty(prefix)) {
                return formatPrefix(prefix);
            }
        } catch (OXException e) {
            LOGGER.warn("ConfigView is not available.", e);
        }
        LOGGER.debug("\"{}\" not configured, using default value.", PREFIX_PATH_NAME);
        return DEFAULT_PREFIX_PATH;
    }

    public final static String PROXY_PREFIX_PATH_NAME = "com.openexchange.dav.proxyPrefixPath";
    public final static String DEFAULT_PROXY_PREFIX_PATH = "/servlet/dav";

    /**
     * Get the DAV proxy path prefix
     * 
     * @param configViewFactory The {@link ConfigViewFactory} to use
     * @return The configured value or {@value #DEFAULT_PROXY_PREFIX_PATH} as default value
     */
    private static String getProxyPrefixPath(ConfigViewFactory configViewFactory) {
        if (null == configViewFactory) {
            LOGGER.warn("ConfigViewFactory is not available.");
            return DEFAULT_PROXY_PREFIX_PATH;
        }

        try {
            String proxyPrefixPath = configViewFactory.getView().get(PROXY_PREFIX_PATH_NAME, String.class);
            if (isNotEmpty(proxyPrefixPath)) {
                return formatPrefix(proxyPrefixPath);
            }
        } catch (OXException e) {
            LOGGER.warn("ConfigView is not available.", e);
        }
        LOGGER.debug("\"{}\" not configured, using default value.", PROXY_PREFIX_PATH_NAME);
        return DEFAULT_PROXY_PREFIX_PATH;
    }

    /**
     * 
     * Concatenates the given path with the path prefix for DAV servlets as per {@link #getPathPrefix(ConfigViewFactory)}
     *
     * @param configViewFactory The {@link ConfigViewFactory} to get the path prefix from
     * @param path The path to add after the prefix
     * @return The concatenated path
     */
    public static String concatPath(ConfigViewFactory configViewFactory, String path) {
        StringBuilder sb = new StringBuilder();
        String pathPrefix = getPathPrefix(configViewFactory);
        sb.append(pathPrefix);

        if (isEmpty(path)) {
            return sb.toString();
        }
        if (false == pathPrefix.endsWith("/") && false == path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(path);
        return sb.toString();
    }

    /**
     * Adjusts the path according to the configured path prefix for DAV servlets <b>and</b> the proxy path prefix.
     * <p>
     * If the path prefix as per {@link #getPathPrefix(ConfigViewFactory)} is {@value #DEFAULT_PREFIX_PATH} and the
     * proxy path prefix as per {@link #getProxyPrefixPath(ConfigViewFactory)} is {@value #DEFAULT_PROXY_PREFIX_PATH}
     * this method only ensures that the given path begins with a <code>/</code>
     * <p>
     * If the path prefix as per {@link #getPathPrefix(ConfigViewFactory)} is e.g. <code>/dav</code> and the
     * proxy path prefix as per {@link #getProxyPrefixPath(ConfigViewFactory)} is e.g. <code>/</code>
     * this method will add <code>/dav</code> to the path, e.g. resulting in <code>/dav/your/path</code> where
     * <code>your/path</code> or <code>/your/path</code> was the input
     * <p>
     *
     * @param configViewFactory The configuration
     * @param path The path to adjust accordingly
     * @return The adjusted path
     */
    public static String adjustPath(ConfigViewFactory configViewFactory, String path) {
        if (null == configViewFactory || isEmpty(path)) {
            return formatPath(path);
        }

        /*
         * If both path are equal, do nothing ..
         */
        String pathPrefix = getPathPrefix(configViewFactory);
        String proxyPrefixPath = getProxyPrefixPath(configViewFactory);
        if (pathPrefix.equals(proxyPrefixPath)) {
            return formatPath(path);
        }

        /*
         * Avoid multiple prefix invocation & slashes
         */
        StringBuilder sb = new StringBuilder();
        if (false == path.startsWith(pathPrefix)) {
            sb.append(pathPrefix);
            if (false == pathPrefix.endsWith("/") && false == path.startsWith("/")) {
                sb.append("/");
            }
        }
        sb.append(path);
        
        /*
         * Add the prefix and remove the proxy prefix afterwards
         */
        return removePrefixFromPath(proxyPrefixPath, sb.toString());
    }

    /**
     * Removes the path prefix as per {@link #getPathPrefix(ConfigViewFactory)} from the given path
     *
     * @param configViewFactory The {@link ConfigViewFactory}
     * @param path The path
     * @return The path without the prefix
     */
    public static String removePathPrefixFromPath(ConfigViewFactory configViewFactory, String path) {
        String prefix = getPathPrefix(configViewFactory);
        return removePrefixFromPath(prefix, path);
    }

    /**
     * Removes the prefix from the given path
     * 
     * @param prefix The prefix to remove
     * @param path The path
     * @return The path without the prefix
     */
    public static String removePrefixFromPath(String prefix, String path) {
        if (isEmpty(path) || isEmpty(prefix)) {
            return formatPath(path);
        }

        String formattedPrefix = formatPath(formatPrefix(prefix));
        String formattedPath = formatPath(formatPrefix(path));
        if (false == formattedPath.startsWith(formattedPrefix)) {
            return formatPath(path);
        }

        return formatPath(formattedPath.substring(formattedPrefix.length()));
    }
    
    /**
     * Get a value whether the given URL is exactly the root path or not 
     *
     * @param configViewFactory The {@link ConfigViewFactory}
     * @param path The path to check
     * @return <code>true</code> if the provided path equals the root folder as per {@link #getPathPrefix(ConfigViewFactory)}
     */
    public static boolean isRoot(ConfigViewFactory configViewFactory, String path) {
        String pathPrefix = getPathPrefix(configViewFactory);
        if (isNotEmpty(pathPrefix)) {
            if (isEmpty(path)) {
                return false;
            }
            return path.endsWith("/") ? pathPrefix.equals(path.substring(0, path.length() - 1)) : pathPrefix.equals(path);
        }
        return isEmpty(path);
    }
    
    
    /**
     * Asserts that the given path starts with the prefix, ignoring ending <code>/</code> 
     *  
     * @param path The one path
     * @param prefix The prefix path
     * @return <code>true</code> if the path begins with the prefix, <code>false</code> otherwise
     */
    public static boolean startsWithPrefix(String path, String prefix) {
        if (isEmpty(path)) {
            return isEmpty(prefix);
        }
        String formattedPath = formatPath(formatPrefix(path));
        String formattedPrefix = formatPath(formatPrefix(prefix));
        return formattedPath.startsWith(formattedPrefix);
    }

    /**
     * Ensures that the path begins with a <code>/</code>
     *
     * @param path The path to ensure the slash on
     * @return The adjusted path
     */
    private static String formatPath(String path) {
        if (isEmpty(path)) {
            return "/";
        }
        if (false == path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    /**
     * Ensures that the path begins with a <code>/</code>
     *
     * @param path The path to ensure the slash on
     * @return The adjusted path
     */
    private static String formatPrefix(String prefix) {
        String formattedPrefix = formatPath(prefix);
        if (isNotEmpty(formattedPrefix) && formattedPrefix.endsWith("/")) {
            formattedPrefix = formattedPrefix.substring(0, formattedPrefix.length() - 1);
        }
        return formattedPrefix;
    }
}
