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

package com.openexchange.chronos;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.chronos.rmi.ChronosRMIService;
import com.openexchange.cli.AbstractRmiCLI;

/**
 * {@link SetEventOrganizerCLT} - Serves <code>seteventorganizer</code> command-line tool.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class SetEventOrganizerCLT extends AbstractRmiCLI<Void> {

    // @formatter:off
    private static final String SYNTAX = "seteventorganizer -c <contextId> -e <eventId> -u <userId> " + BASIC_CONTEXT_ADMIN_USAGE;
    private static final String FOOTER =
        "Sets a new organizer for the given event in the given context.\n" +
        "If this is performed on a recurring event (master or exception), all exceptions and the master are changed.\n" +
        "The new organizer must be an internal user and the old organizer must not be an external user.\n" +
        "If the organizer is no attendee, the organizer will automatically be added as attendee.\n" +
        "If the organizer is already set but not yet an attendee, the organizer will be added as attendee as well.\n" +
        "If the organizer is already set and also an attendee this is a no-op.\n" +
        "Bear in mind, that external users/clients may not support organizer changes, thus this operation is not propagated to external attendees.";
    // @formatter:on

    private Integer contextId, eventId, userId;

    private SetEventOrganizerCLT() {
        super();
    }

    public static void main(String[] args) {
        new SetEventOrganizerCLT().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password, contextId.intValue());
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "Required. The context identifier", true));
        options.addOption(createArgumentOption("e", "event", "eventId", "Required. The event identifier", true));
        options.addOption(createArgumentOption("u", "user", "userId", "Required. The user identifier", true));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean error = true;
        try {
            ChronosRMIService rmiService = getRmiStub(optRmiHostName, ChronosRMIService.RMI_NAME);
            rmiService.setEventOrganizer(contextId.intValue(), eventId.intValue(), userId.intValue());
            error = false;
        } catch (Exception e) {
            final String errMsg = e.getMessage();
            System.out.println(errMsg == null ? "An error occurred." : errMsg);
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        contextId = parseIntegerOptionValue(cmd.getOptionValue('c'), "context id");
        eventId = parseIntegerOptionValue(cmd.getOptionValue('e'), "event id");
        userId = parseIntegerOptionValue(cmd.getOptionValue('u'), "user id");
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }

    private Integer parseIntegerOptionValue(String value, String option) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            System.err.println("Cannot parse '" + value + "' as " + option);
            printHelp();
            System.exit(1);
        }
        return null;
    }

}
