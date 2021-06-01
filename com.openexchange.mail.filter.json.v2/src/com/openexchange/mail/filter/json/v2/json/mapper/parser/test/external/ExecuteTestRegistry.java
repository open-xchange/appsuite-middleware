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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.JSONMatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.test.ITestCommand;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ExecuteTestRegistry} Handles the extPrograms sieve plugin execute tests
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ExecuteTestRegistry implements ITestCommand {

    /**
     * Map of available tests
     */
    private final Map<String, SieveExecuteTest> tests;

    /**
     * Initializes a new {@link ExecuteTestRegistry}.
     *
     * @param services The {@link ServiceLookup}
     */
    public ExecuteTestRegistry(ServiceLookup services) {
        tests = Collections.synchronizedMap(new HashMap<String, SieveExecuteTest>());
    }

    /**
     * Register a SieveExecuteTest to the registry
     *
     * @param test The {@link SieveExecuteTest} to register
     */
    public void registerService(SieveExecuteTest test) {
        tests.put(test.getJsonName(), test);
    }

    /**
     * Remove a SieveExecuteTest from the registry
     *
     * @param test The {@link SieveExecuteTest} to unregister
     */
    public void unRegisterService(SieveExecuteTest test) {
        tests.remove(test.getJsonName());
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
        if (tests.get(id) != null) {
            return tests.get(id).isCommandSupported(capabilities);
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
    public Optional<SieveExecuteTest> getApplicableParser(TestCommand command) throws OXException {
        for (SieveExecuteTest test : tests.values()) {
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
    public Optional<SieveExecuteTest> getApplicableParser(JSONObject json) throws OXException {
        for (SieveExecuteTest test : tests.values()) {
            if (test.isApplicable(json)) {
                return Optional.ofNullable(test);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getCommandName() {
        return "execute";
    }

    @Override
    public List<String> getRequired() {
        return Arrays.asList("vnd.dovecot.execute");
    }

    @Override
    public Map<String, String> getMatchTypes() {
        Map<String, String> result = new HashMap<>();
        tests.forEach((key, test) -> result.putAll(test.getMatchTypes()));
        return result;
    }

    @Override
    public Map<String, String> getAddress() {
        return null;
    }

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public int getMaxNumberOfArguments() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Map<String, String> getComparator() {
        return null;
    }

    @Override
    public List<JSONMatchType> getJsonMatchTypes() {
        List<JSONMatchType> types = new ArrayList<JSONMatchType>();
        tests.forEach((key, test) -> types.addAll(test.getJsonMatchTypes()));
        return types;
    }

    @Override
    public Map<String, String> getOtherArguments() {
        Map<String, String> result = new HashMap<>();
        tests.forEach((key, test) -> result.putAll(test.getOtherArguments()));
        return result;
    }

}
