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

package com.openexchange.imap.util;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.sun.mail.iap.Protocol;

/**
 * {@link GenericFailsafeCircuitBreakerCommandExecutor} - The circuit breaker for all IMAP end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class GenericFailsafeCircuitBreakerCommandExecutor extends AbstractFailsafeCircuitBreakerCommandExecutor {

    /** The logger constant */
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GenericFailsafeCircuitBreakerCommandExecutor.class);

    private static final String PROP_PRIMARY_ACCOUNT = "mail.imap.primary";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<Optional<HostList>> optionalHostsToExclude;
    private final AtomicBoolean excludePrimaryAccount;

    /**
     * Initializes a new {@link GenericFailsafeCircuitBreakerCommandExecutor}.
     *
     * @param failureThreshold The number of successive failures that must occur in order to open the circuit
     * @param successThreshold The number of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public GenericFailsafeCircuitBreakerCommandExecutor(int failureThreshold, int successThreshold, long delayMillis) {
        super(Optional.empty(), null, failureThreshold, successThreshold, delayMillis, 0);
        optionalHostsToExclude = new AtomicReference<>(Optional.empty());
        excludePrimaryAccount = new AtomicBoolean(false);
    }

    @Override
    protected void onClose() throws Exception {
        LOG.info("Generic IMAP circuit breaker closed");
    }

    @Override
    protected void onHalfOpen() throws Exception {
        LOG.info("Generic IMAP circuit breaker half-opened");
    }

    @Override
    protected void onOpen() throws Exception {
        LOG.info("Generic IMAP circuit breaker opened");
    }

    @Override
    public String getDescription() {
        return "generic";
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
            String currentHostString = current.isPresent() ? current.get().getHostString() : "";
            newHostList = Optional.of(HostList.valueOf(Strings.isEmpty(currentHostString) ? hosts : currentHostString + ", " + hosts));
        } while (!optionalHostsToExclude.compareAndSet(current, newHostList));
    }

    @Override
    public boolean isApplicable(Protocol protocol) {
        if (excludePrimaryAccount.get() && "true".equals(protocol.getProps().getProperty(PROP_PRIMARY_ACCOUNT))) {
            return false;
        }

        Optional<HostList> optionalHostList = optionalHostsToExclude.get();
        return !optionalHostList.isPresent() || !optionalHostList.get().contains(protocol.getHost());
    }

}
