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

package com.openexchange.imap.commandexecutor;

import static com.openexchange.exception.ExceptionUtils.isEitherOf;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.openexchange.java.Strings;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
import com.sun.mail.imap.CommandExecutor;
import com.sun.mail.imap.ResponseEvent.StatusResponse;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * {@link MonitoringCommandExecutor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MonitoringCommandExecutor implements CommandExecutor {

    private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(
        com.sun.mail.iap.ConnectionException.class,
        com.sun.mail.iap.ByeIOException.class,
        java.net.SocketTimeoutException.class,
        java.io.EOFException.class);

    private final Config config;

    /**
     * Initializes a new {@link MonitoringCommandExecutor}.
     *
     * @param config The monitoring config
     */
    public MonitoringCommandExecutor(Config config) {
        super();
        this.config = config;
    }

    @Override
    public boolean isApplicable(Protocol protocol) {
        return true;
    }

    @Override
    public Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol) {
       return executeCommandExtended(command, args, optionalInterceptor, protocol).responses;
    }

    /**
     * Executes the given command
     *
     * @param command The command to execute
     * @param args The arguments of the command
     * @param optionalInterceptor The optional {@link ResponseInterceptor}
     * @param protocol The protocol to use
     * @return The {@link ExecutedCommand}
     */
    public ExecutedCommand executeCommandExtended(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol) {
        if (config.isEnabled()) {
            long duration = -1;
            String status = "UNKNOWN";
            try {
                // Measure command execution
                long start = System.nanoTime();
                Response[] responses = protocol.executeCommand(command, args, optionalInterceptor);
                duration = System.nanoTime() - start;

                // Check responses if command failed
                StatusResponse statusResponse = StatusResponse.statusResponseFor(responses);
                if (statusResponse != null) {
                    Exception exception = statusResponse.getException();
                    if (isEitherOf(exception, NETWORK_COMMUNICATION_ERRORS)) {
                        status = "COMMUNICATION_ERROR";
                    } else if (exception != null) {
                        status = "UNKNOWN_ERROR";
                    } else {
                        status = statusResponse.getStatus().name();
                    }
                }

                return new ExecutedCommand(statusResponse, responses);
            } finally {
                if (duration >= 0) {
                    recordStatus(protocol, command, status, Duration.ofNanos(duration));
                }
            }
        }

        Response[] responses = protocol.executeCommand(command, args, optionalInterceptor);
        StatusResponse statusResponse = StatusResponse.statusResponseFor(responses);
        return new ExecutedCommand(statusResponse, responses);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    public static final class Config {

        public static final String DEFAULT_COMMAND_WHITELIST_STRING = "SELECT, EXAMINE, CREATE, DELETE, RENAME, SUBSCRIBE, UNSUBSCRIBE, LIST, LSUB,"
            + "STATUS, APPEND, EXPUNGE, CLOSE, SEARCH, FETCH, STORE, COPY, SORT";

        public static final List<String> DEFAULT_COMMAND_WHITELIST = Strings.splitAndTrim(DEFAULT_COMMAND_WHITELIST_STRING, ",");

        private boolean enabled = true;
        private boolean groupByPrimaryHosts = false;
        private boolean groupByPrimaryEndpoints = false;
        private boolean measureExternalAccounts = false;
        private boolean groupByExternalHosts = false;
        private boolean groupByCommands = false;
        private Set<String> commandWhitelist;

        public Config() {
            setCommandWhitelist(DEFAULT_COMMAND_WHITELIST);
        }

        /**
         * Enables/disables IMAP command monitoring.
         * <p>
         * Default: {@code true}
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * If {@code true}, commands against the primary mail account are tagged with their configured IMAP host name.
         * This is useful in case of a limited set of different mail backend clusters with distinguishable host names.
         * It might negatively affect resource consumption in case of many different host names though, as per each tag
         * value a metric instance is held in memory and an according time series is published.
         * <p>
         * If {@code false}, the {@code host} tag is always set to {@code primary}.
         * <p>
         * This setting is ignored in case of {@code groupByPrimaryEndpoints=true}.
         * <p>
         * Default: {@code false}
         */
        public boolean isGroupByPrimaryHosts() {
            return groupByPrimaryHosts;
        }

        /**
         * If {@code true}, commands against the primary mail account are tagged with their resolved IP/port combination.
         * This is useful to observe all primary mail backend IPs that are returned by DNS when resolving the IMAP host name.
         * It might negatively affect resource consumption in case of many different returned IPs or many different primary host names though, as per each tag
         * value a metric instance is held in memory and an according time series is published.
         * <p>
         * If {@code false}, {@code groupByPrimaryHosts} applies.
         * <p>
         * Default: {@code false}
         */
        public boolean isGroupByPrimaryEndpoints() {
            return groupByPrimaryEndpoints;
        }

        /**
         * Controls whether commands against external mail accounts are also monitored.
         * <p>
         * Default: {@code true}
         */
        public boolean isMeasureExternalAccounts() {
            return measureExternalAccounts;
        }

        /**
         * If {@code true}, commands against external mail accounts are tagged with their configured IMAP host name.
         * This can be useful when debugging latency or other issues with external email services.
         * Depending on the variety of external IMAP servers configured by users, this negatively affects resource consumption, as per each tag
         * value a metric instance is held in memory and an according time series is published.
         * <p>
         * If {@code false}, the {@code host} tag is always set to {@code external}.
         * <p>
         * This setting is ignored in case of {@code measureExternalAccounts=false}.
         * <p>
         * Default: {@code false}
         */
        public boolean isGroupByExternalHosts() {
            return groupByExternalHosts;
        }

        /**
         * If {@code true}, command latencies and response status are tagged with the respective command key, if that matches a certain
         * whitelist.
         * <p>
         * If {@code false}, the {@code cmd} tag is always set to {@code ALL}.
         */
        public boolean isGroupByCommands() {
            return groupByCommands;
        }

        /**
         * If {@code groupByCommands} it {@code true}, any command contained in this whitelist is measured as a separate value of tag {@code cmd}.
         * All commands that do not match the whitelist are aggregated as {@code cmd="OTHER"}.
         * <p>
         * Commands must be single words without whitespace. At runtime, {@code UID <cmd>} commands are matched without the UID prefix. I.e. {@code FETCH} and {@code UID FETCH} are both tagged with {@code cmd="FETCH}.
         * <p>
         * Value is a comma-separated list of commands.
         * <p>
         * Default: SELECT, EXAMINE, CREATE, DELETE, RENAME, SUBSCRIBE, UNSUBSCRIBE, LIST, LSUB, STATUS, APPEND, EXPUNGE, CLOSE, SEARCH, FETCH, STORE, COPY, SORT
         */
        public Set<String> getCommandWhitelist() {
            return commandWhitelist;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setGroupByPrimaryHosts(boolean groupByPrimaryHosts) {
            this.groupByPrimaryHosts = groupByPrimaryHosts;
        }

        public void setGroupByPrimaryEndpoints(boolean groupByPrimaryEndpoints) {
            this.groupByPrimaryEndpoints = groupByPrimaryEndpoints;
        }

        public void setMeasureExternalAccounts(boolean measureExternalAccounts) {
            this.measureExternalAccounts = measureExternalAccounts;
        }

        public void setGroupByExternalHosts(boolean groupByExternalHosts) {
            this.groupByExternalHosts = groupByExternalHosts;
        }

        public void setGroupByCommands(boolean groupByCommands) {
            this.groupByCommands = groupByCommands;
        }

        public void setCommandWhitelist(Collection<String> commandWhitelist) {
            this.commandWhitelist = commandWhitelist.stream().map(c -> c.toLowerCase().trim()).collect(Collectors.toSet());
        }
    }

    private String sanitizeCommand(final String command) {
        int startIndex;
        int endIndex;
        if (command.length() > 4 && command.substring(0, 4).equalsIgnoreCase("uid ")) {
            startIndex = 4;
            endIndex = command.indexOf(" ", startIndex);
        } else {
            startIndex = 0;
            endIndex = command.indexOf(" ");
        }

        String result = command;
        if (endIndex > 0) {
            result = command.substring(startIndex, endIndex);
        }

        if (config.getCommandWhitelist().contains(result.toLowerCase().trim())) {
            return result;
        }

        return "OTHER";
    }

    private void recordStatus(Protocol protocol, String command, String status, Duration duration) {
        boolean isPrimaryAccount = "true".equals(protocol.getProps().getProperty(PROP_PRIMARY_ACCOUNT));
        if (!config.isMeasureExternalAccounts() && !isPrimaryAccount) {
            return;
        }

        if (config.isGroupByCommands()) {
            command = sanitizeCommand(command);
        } else {
            command = "ALL";
        }

        String targetHost;
        if (isPrimaryAccount) {
            if (config.isGroupByPrimaryHosts()) {
                targetHost = new StringBuilder(protocol.getHost()).toString();
            } else if (config.isGroupByPrimaryEndpoints()) {
                InetAddress inetAddress = protocol.getInetAddress();
                targetHost = new StringBuilder(inetAddress.getHostAddress()).append(':').append(protocol.getPort()).toString();
            } else {
                targetHost = "primary";
            }
        } else if (config.isGroupByExternalHosts()) {
            targetHost = new StringBuilder(protocol.getHost()).append(protocol.getPort()).toString();
        } else {
            targetHost = "external";
        }

        Timer requestTimer = Timer.builder("appsuite.imap.commands")
            .description("IMAP commands per target server. Status can be OK, NO, BAD, BYE, UNKNOWN, COMMUNICATION_ERROR, UNKNOWN_ERROR.")
            .tags("cmd", command, "status", status, "host", targetHost)
            .register(Metrics.globalRegistry);
        requestTimer.record(duration);
    }

}
