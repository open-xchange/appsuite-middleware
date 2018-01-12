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
