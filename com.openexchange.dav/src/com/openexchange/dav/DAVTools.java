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

package com.openexchange.dav;

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

    private final static String PREFIX_PATH_NAME = "com.openexchange.dav.prefixPath";
    private final static String DEFAULT_PREFIX_PATH = "/servlet/dav";

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
        return DEFAULT_PREFIX_PATH;
    }

    private final static String PROXY_PREFIX_PATH_NAME = "com.openexchange.dav.proxyPrefixPath";
    private final static String DEFAULT_PROXY_PREFIX_PATH = "/servlet/dav";

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
        return DEFAULT_PROXY_PREFIX_PATH;
    }

    /**
     * Get the internal used DAV path considering the configured path prefix
     *
     * @param configViewFactory The {@link ConfigViewFactory} to get the path prefix from
     * @param path The path to add after the prefix
     * @return The concatenated path
     */
    public static String getInternalPath(ConfigViewFactory configViewFactory, String path) {
        String pathPrefix = getPathPrefix(configViewFactory);
        return concatPath(pathPrefix, path);
    }

    private static String concatPath(String pathPrefix, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(pathPrefix);

        if (isEmpty(path)) {
            return sb.toString();
        }

        /*
         * Avoid multiple prefix invocation & slashes
         */
        if (false == pathPrefix.endsWith("/") && false == path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(path);
        return sb.toString();
    }

    /**
     * Get the configured path for DAV servlets to propagate to the clients.
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
    public static String getExternalPath(ConfigViewFactory configViewFactory, String path) {
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
         * Add the prefix and remove the proxy prefix afterwards
         */
        return removePrefixFromPath(proxyPrefixPath, concatPath(pathPrefix, path));
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
        String formattedPath = formatPath(path);
        if (false == formattedPath.startsWith(formattedPrefix)) {
            return formatPath(path);
        }

        return formatPath(formattedPath.substring(formattedPrefix.length()));
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
        String formattedPrefix = formatPath(formatPrefix(prefix));
        String formattedPath = formatPath(formatPrefix(path));
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
        String formatted = path;
        if (false == formatted.startsWith("/")) {
            formatted = "/" + path;
        }
        return formatted.trim();
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
