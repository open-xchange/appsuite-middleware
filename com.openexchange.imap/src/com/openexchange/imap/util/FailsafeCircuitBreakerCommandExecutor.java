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
 *    trademarks of the OX Software GmbH group of companies.
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
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.net.HostList;

/**
 * {@link FailsafeCircuitBreakerCommandExecutor} - A circuit breaker for denoted IMAP end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class FailsafeCircuitBreakerCommandExecutor extends AbstractFailsafeCircuitBreakerCommandExecutor {

    /** The logger constant */
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FailsafeCircuitBreakerCommandExecutor.class);

    private static HostList checkHostList(HostList hostList) {
        if (null == hostList || hostList.isEmpty()) {
            throw new IllegalArgumentException("hostList must not be null or empty.");
        }
        return hostList;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link FailsafeCircuitBreakerCommandExecutor}.
     *
     * @param hostList The hosts to consider
     * @param optPorts The optional ports to consider
     * @param failureThreshold The number of successive failures that must occur in order to open the circuit
     * @param successThreshold The number of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param ranking The ranking
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public FailsafeCircuitBreakerCommandExecutor(HostList hostList, Set<Integer> optPorts, int failureThreshold, int successThreshold, long delayMillis, int ranking) {
        super(Optional.of(checkHostList(hostList)), optPorts, failureThreshold, successThreshold, delayMillis, ranking);
    }

    @Override
    protected void onClose() throws Exception {
        if (optionalHostList.isPresent()) {
            LOG.info("IMAP circuit breaker closed for: {}", optionalHostList.get().getHostString());
        } else {
            LOG.info("Generic IMAP circuit breaker closed");
        }
    }

    @Override
    protected void onHalfOpen() throws Exception {
        if (optionalHostList.isPresent()) {
            LOG.info("IMAP circuit breaker half-opened for: {}", optionalHostList.get().getHostString());
        } else {
            LOG.info("Generic IMAP circuit breaker half-opened");
        }
    }

    @Override
    protected void onOpen() throws Exception {
        if (optionalHostList.isPresent()) {
            LOG.info("IMAP circuit breaker opened for: {}", optionalHostList.get().getHostString());
        } else {
            LOG.info("Generic IMAP circuit breaker opened");
        }
    }

    @Override
    public String getDescription() {
        return optionalHostList.isPresent() ? optionalHostList.get().getHostString() : "";
    }

}
