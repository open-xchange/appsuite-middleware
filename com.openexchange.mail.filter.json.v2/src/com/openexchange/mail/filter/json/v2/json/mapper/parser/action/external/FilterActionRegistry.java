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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.action.external;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.test.IActionCommand;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.external.SieveExecuteTest;
import com.openexchange.mailfilter.MailFilterService;

/**
 * {@link FilterActionRegistry} Contains a registry of Filter actions that utilize
 * the sieve extPrograms plugin
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class FilterActionRegistry implements IActionCommand {

    /**
     * Map of available actions
     */
    private final Map<String, SieveFilterAction> actions;

    public FilterActionRegistry() {
        actions = Collections.synchronizedMap(new HashMap<String, SieveFilterAction>());
    }

    /**
     * Register a SieveExecuteTest to the registry
     *
     * @param test The {@link SieveFilterAction} to register
     */
    public void registerService(SieveFilterAction test) {
        actions.put(test.getJsonName(), test);
    }

    /**
     * Remove a SieveExecuteTest from the registry
     *
     * @param test The {@link SieveFilterAction} to unregister
     */
    public void unRegisterService(SieveFilterAction test) {
        actions.remove(test.getJsonName());
    }

    /**
     * Checks whether this command is supported
     *
     * @param capabilities The capabilities previously obtained from the {@link MailFilterService} service
     * @param id The id of the test
     * @return <code>true</code> if it is supported, <code>false</code> otherwise
     * @throws OXException
     */
    public boolean isCommandSupported(Set<String> capabilities, String id) throws OXException {
        if (actions.get(id) != null) {
            return actions.get(id).isCommandSupported(capabilities);
        }
        return false;
    }

    /**
     * Gets the optional {@link SieveExecuteTest} for the given command
     *
     * @param command The command to get the {@link SieveExecuteTest} for
     * @return The optional {@link SieveExecuteTest} if found
     * @throws OXException
     */
    public Optional<SieveFilterAction> getApplicableParser(ActionCommand command) throws OXException {
        for (SieveFilterAction test : actions.values()) {
            if (test.isApplicable(command)) {
                return Optional.ofNullable(test);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the optional {@link SieveExecuteTest} for the given {@link JSONObject}
     *
     * @param json The json object containing the execute test
     * @return The optional {@link SieveExecuteTest} if found
     * @throws OXException
     */
    public Optional<SieveFilterAction> getApplicableParser(JSONObject json) throws OXException {
        for (SieveFilterAction test : actions.values()) {
            if (test.isApplicable(json)) {
                return Optional.ofNullable(test);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getCommandName() {
        return "filter";
    }

    @Override
    public List<String> getRequired() {
        return Arrays.asList("vnd.dovecot.filter");
    }

    @Override
    public int getMinNumberOfArguments() {
        return 0;
    }

    @Override
    public Hashtable<String, Integer> getTagArgs() {
        Hashtable<String, Integer> args = new Hashtable<String, Integer>();
        for (SieveFilterAction test : actions.values()) {
            args.putAll(test.getTagArgs());
        }
        return args;
    }


}
