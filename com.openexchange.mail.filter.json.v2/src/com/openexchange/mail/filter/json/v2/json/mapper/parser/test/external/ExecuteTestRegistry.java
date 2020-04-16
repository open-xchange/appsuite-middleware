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
