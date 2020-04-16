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
