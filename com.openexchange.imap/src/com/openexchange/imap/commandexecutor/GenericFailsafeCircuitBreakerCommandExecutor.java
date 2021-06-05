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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.sun.mail.imap.ProtocolAccess;
import com.sun.mail.util.ProtocolInfo;
import net.jodah.failsafe.util.Ratio;

/**
 * {@link GenericFailsafeCircuitBreakerCommandExecutor} - The circuit breaker for all IMAP end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class GenericFailsafeCircuitBreakerCommandExecutor extends AbstractFailsafeCircuitBreakerCommandExecutor {

    private final AtomicReference<Optional<HostList>> optionalHostsToExclude;
    private final AtomicBoolean excludePrimaryAccount;

    /**
     * Initializes a new {@link GenericFailsafeCircuitBreakerCommandExecutor}.
     *
     * @param failureThreshold The ratio of successive failures that must occur in order to open the circuit
     * @param successThreshold The ratio of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param delegate The delegate executor
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public GenericFailsafeCircuitBreakerCommandExecutor(Ratio failureThreshold, Ratio successThreshold, long delayMillis, MonitoringCommandExecutor delegate) {
        super(Optional.empty(), null, failureThreshold, successThreshold, delayMillis, 10, delegate);
        optionalHostsToExclude = new AtomicReference<>(Optional.empty());
        excludePrimaryAccount = new AtomicBoolean(false);
    }

    @Override
    protected Key getKey(ProtocolInfo protocolInfo) {
        return Key.of("generic", protocolInfo.getHost(), false);
    }

    /**
     * Marks this generic IMAP circuit breaker to exclude primary account accesses.
     */
    public void excludePrimaryAccount() {
        excludePrimaryAccount.set(true);
    }

    /**
     * Adds hosts that are supposed to be excluded.
     *
     * @param hosts The hosts to exclude
     */
    public void addHostsToExclude(String hosts) {
        if (Strings.isEmpty(hosts)) {
            return;
        }

        Optional<HostList> current;
        Optional<HostList> newHostList;
        do {
            current = optionalHostsToExclude.get();
            String currentHostString = current.isPresent() ? current.get().getHostList() : "";
            newHostList = Optional.of(HostList.valueOf(Strings.isEmpty(currentHostString) ? hosts : currentHostString + ", " + hosts));
        } while (!optionalHostsToExclude.compareAndSet(current, newHostList));
    }

    @Override
    public boolean isApplicable(ProtocolAccess protocolAccess) {
        if (excludePrimaryAccount.get() && "true".equals(protocolAccess.getProps().getProperty(PROP_PRIMARY_ACCOUNT))) {
            return false;
        }

        Optional<HostList> optionalHostList = optionalHostsToExclude.get();
        return !optionalHostList.isPresent() || !optionalHostList.get().contains(protocolAccess.getHost());
    }

}
