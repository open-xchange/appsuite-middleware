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

import java.net.InetAddress;
import java.util.Optional;
import com.sun.mail.imap.ProtocolAccess;
import com.sun.mail.util.ProtocolInfo;
import net.jodah.failsafe.util.Ratio;

/**
 * {@link PrimaryFailsafeCircuitBreakerCommandExecutor} - The special IMAP circuit breaker form primary account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class PrimaryFailsafeCircuitBreakerCommandExecutor extends AbstractFailsafeCircuitBreakerCommandExecutor {

    private final boolean applyPerEndpoint;

    /**
     * Initializes a new {@link PrimaryFailsafeCircuitBreakerCommandExecutor}.
     *
     * @param failureThreshold The ratio of successive failures that must occur in order to open the circuit
     * @param successThreshold The ratio of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param applyPerEndpoint Whether the circuit breaker shall be applied per end-point (IP/port combination). Otherwise it applies per primary account host name.
     * @param delegate The delegate executor
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public PrimaryFailsafeCircuitBreakerCommandExecutor(Ratio failureThreshold, Ratio successThreshold, long delayMillis, boolean applyPerEndpoint, MonitoringCommandExecutor delegate) {
        super(Optional.empty(), null, failureThreshold, successThreshold, delayMillis, 100, delegate);
        this.applyPerEndpoint = applyPerEndpoint;
    }

    @Override
    protected Key getKey(ProtocolInfo protocolInfo) {
        if (applyPerEndpoint) {
            InetAddress inetAddress = protocolInfo.getInetAddress();
            return Key.of("primary", inetAddress.getHostAddress() + ':' + protocolInfo.getPort(), true);
        }

        return Key.of("primary", protocolInfo.getHost(), false);
    }

    @Override
    public boolean isApplicable(ProtocolAccess protocolAccess) {
        return "true".equals(protocolAccess.getProps().getProperty(PROP_PRIMARY_ACCOUNT));
    }

}
