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

package com.openexchange.imap.commandexecutor;

import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.sun.mail.util.ProtocolInfo;
import net.jodah.failsafe.util.Ratio;

/**
 * {@link FailsafeCircuitBreakerCommandExecutor} - A circuit breaker for denoted IMAP end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class FailsafeCircuitBreakerCommandExecutor extends AbstractFailsafeCircuitBreakerCommandExecutor {

    private static HostList checkHostList(HostList hostList) {
        if (null == hostList || hostList.isEmpty()) {
            throw new IllegalArgumentException("hostList must not be null or empty.");
        }
        return hostList;
    }

    private static final Set<String> RESERVED_NAMES = ImmutableSet.of("generic", "primary");

    private static String checkName(String name) {
        if (Strings.isEmpty(name)) {
            throw new IllegalArgumentException("name must not be null or empty.");
        }

        String toCheck = Strings.asciiLowerCase(name.trim());
        if (RESERVED_NAMES.contains(toCheck)) {
            throw new IllegalArgumentException("name must not be equal to either of: " + RESERVED_NAMES);
        }
        return toCheck;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String name;

    /**
     * Initializes a new {@link FailsafeCircuitBreakerCommandExecutor}.
     *
     * @param name The circuit breaker name as specified in configuration
     * @param hostList The hosts to consider
     * @param optPorts The optional ports to consider
     * @param failureThreshold The ratio of successive failures that must occur in order to open the circuit
     * @param successThreshold The ratio of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param ranking The ranking
     * @param delegate The delegate executor
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public FailsafeCircuitBreakerCommandExecutor(String name, HostList hostList, Set<Integer> optPorts, Ratio failureThreshold,
            Ratio successThreshold, long delayMillis, int ranking, MonitoringCommandExecutor delegate) {
        super(Optional.of(checkHostList(hostList)), optPorts, failureThreshold, successThreshold, delayMillis, ranking, delegate);
        this.name = checkName(name);
    }

    @Override
    protected Key getKey(ProtocolInfo protocolInfo) {
        return Key.of(name, getHostList().get().getHostList(), false);
    }

}
