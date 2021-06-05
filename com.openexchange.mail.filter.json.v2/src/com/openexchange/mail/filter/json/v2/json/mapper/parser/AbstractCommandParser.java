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

package com.openexchange.mail.filter.json.v2.json.mapper.parser;

import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.test.ICommand;
import com.openexchange.jsieve.registry.CommandRegistry;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractCommandParser<I extends ICommand> {

    protected final ServiceLookup services;
    private final I command;

    /**
     * Initialises a new {@link AbstractCommandParser}.
     */
    public AbstractCommandParser(ServiceLookup services, I command) {
        super();
        this.services = services;
        this.command = command;
    }

    /**
     * Checks whether this command is supported
     *
     * @param capabilities The capabilities previously obtained from the {@link MailFilterService} service
     * @return true if it is supported, false otherwise
     * @throws OXException
     */
    public boolean isCommandSupported(Set<String> capabilities) throws OXException {
        CommandRegistry<I> commandRegistry = getCommandRegistry();
        I c = commandRegistry.get(command.getCommandName());
        List<String> required = c.getRequired();

        if (null == required || required.isEmpty()) {
            return true;
        }
        boolean result = capabilities.containsAll(required);
        // Check if at least one imapflags or imap4flags cap isAvailable
        if (!result && required.size() == 2 && required.contains("imap4flags") && (capabilities.contains("imapflags") || capabilities.contains("imap4flags"))) {
            return true;
        }
        return result;
    }

    /**
     * Retrieves the corresponding {@link ICommand}
     *
     * @return The {@link ICommand}
     * @throws OXException if an error is occurred
     */
    public I getCommand() throws OXException {
        CommandRegistry<I> commandRegistry = getCommandRegistry();
        return commandRegistry.get(getCommandName());
    }

    /**
     * The corresponding {@link ICommand} name
     *
     * @return The command name
     */
    protected String getCommandName() {
        return command.getCommandName();
    }

    /**
     * Returns the corresponding {@link CommandRegistry}
     *
     * @return the corresponding {@link CommandRegistry}
     */
    protected abstract <T> CommandRegistry<T> getCommandRegistry();
}
