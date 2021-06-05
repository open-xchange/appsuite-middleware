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

package com.openexchange.monitoring.sockets;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.logging.LogConfigurationService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SocketLoggerUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public final class SocketLoggerUtil {

    /**
     * Returns a socket logger for a plain socket
     *
     * @param services The {@link ServiceLookup} instance
     * @return The optional Logger if registered
     */
    public static final Optional<Logger> getLoggerForPlainSocket(ServiceLookup services) {
        return getLogger(services, true);
    }

    /**
     * Returns a socket logger for an SSL socket
     *
     * @param services The {@link ServiceLookup} instance
     * @return The optional Logger if registered
     */
    public static final Optional<Logger> getLoggerForSSLSocket(ServiceLookup services) {
        return getLogger(services, false);
    }

    /**
     * Scans the current thread's stack and tries to find an appropriate logger.
     * If the current stack contains an invocation from the {@link DelegatingSSLSocket}
     * then nothing is logged (returns <code>null</code>) as the buffer was already
     * logged on the previous mentioned class.
     *
     * @param services The {@link ServiceLookup} instance
     * @param excludeSSLSocket Flag to exclude the {@link DelegatingSSLSocket}
     * @return The Logger if registered, <code>null</code> otherwise
     */
    private static Optional<Logger> getLogger(ServiceLookup services, boolean excludeSSLSocket) {
        SocketLoggerRegistryService registry = services.getOptionalService(SocketLoggerRegistryService.class);
        if (registry == null || registry.isEmpty()) {
            return Optional.empty();
        }

        // Check if there is any logging filter activated for the current context/user/session
        LogConfigurationService logConfigurationService = services.getOptionalService(LogConfigurationService.class);
        int contextId = getIntMDCProperty(LogProperties.Name.SESSION_CONTEXT_ID, -1);
        int userId = getIntMDCProperty(LogProperties.Name.SESSION_USER_ID, -1);
        String sessionId = LogProperties.get(LogProperties.Name.SESSION_SESSION_ID);
        if (null != logConfigurationService && false == logConfigurationService.anyFilterExists(contextId, userId, sessionId)) {
            return Optional.empty();
        }

        // Iterate caller's stack
        for (StackTraceElement c : new Throwable().getStackTrace()) {
            // The black list check is outside the registry service simply because:
            //   a) we want to abort the stack iteration immediately after a blacklisted item is found and
            //   b) to avoid the overhead of creating and throwing a new BlackListException exception with every get
            if (isBlackListed(registry, c.getClassName(), excludeSSLSocket)) {
                return Optional.empty();
            }
            Optional<Logger> optionalLogger = registry.getLoggerFor(c.getClassName());
            if (optionalLogger.isPresent()) {
                return optionalLogger;
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if the logger with the specified name is black-listed and thus excluded
     * from any socket logging
     *
     * @param registry The registry
     * @param name The name of the logger; e.g. <code>"com.openexchange.database"</code>
     * @param excludeSSLSocket Flag to exclude the {@link DelegatingSSLSocket}
     * @return <code>true</code> if the logger is black-listed, <code>false</code> otherwise
     */
    private static boolean isBlackListed(SocketLoggerRegistryService registry, String name, boolean excludeSSLSocket) {
        if (excludeSSLSocket && name.contains("DelegatingSSLSocket")) {
            return true;
        }

        Set<String> blackListedLoggerNames = registry.getBlackListedLoggerNames();
        if (blackListedLoggerNames.contains(name)) {
            return true;
        }

        int nameLength = name.length();
        for (String blackListedLoggerName : blackListedLoggerNames) {
            if (blackListedLoggerName.length() > nameLength ? blackListedLoggerName.contains(name) : name.contains(blackListedLoggerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prepare string for logging. If the string cannot be prepared due to
     * an {@link UnsupportedEncodingException}, then an appropriate error message is
     * prepared instead.
     *
     * @param buffer The buffer to prepare
     * @param off The offset to start from
     * @param len The length
     * @return The prepared message
     */
    public static String prepareForLogging(byte[] buffer, int off, int len) {
        return new String(buffer, off, len, Charsets.ISO_8859_1);
    }

    /**
     * Returns the positive integer value of the specified MDC property
     *
     * @param name The name of the MDC property
     * @param defaultValue The default value to return in case no such MDC property exists or if it is not an integer
     * @return The integer value of the property or <code>-1</code> if no value is assigned.
     */
    private static int getIntMDCProperty(Name name, int defaultValue) {
        String value = LogProperties.get(name);
        if (Strings.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
