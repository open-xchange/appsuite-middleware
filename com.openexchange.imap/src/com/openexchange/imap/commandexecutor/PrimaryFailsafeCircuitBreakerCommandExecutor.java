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
