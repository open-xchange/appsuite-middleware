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

package com.openexchange.mail.filter.json.v2.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.BasicGroup;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.Field;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailFilterBlacklistService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MailFilterBlacklistService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFilterBlacklistService.class);
    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailFilterBlacklistService}.
     */
    public MailFilterBlacklistService(ServiceLookup serviceLookup) {
        super();
        this.services = serviceLookup;
    }

    /**
     * Returns a blacklist, which contains all blacklisted element for the given user
     *
     * @param userId The user id
     * @param contextId The context id
     * @return The blacklist
     */
    public Blacklist getBlacklists(int userId, int contextId) {
        Map<String, Set<String>> result = new HashMap<>();
        LeanConfigurationService config = services.getService(LeanConfigurationService.class);


        for(BasicGroup base : BasicGroup.values()){
            // Create Basic MailFilterBlacklistType
            MailFilterBlacklistProperty property = new MailFilterBlacklistProperty(base);
            String blacklistString = config.getProperty(userId, contextId, property);
            if(blacklistString!=null){
                addBlacklist(property, blacklistString, result);
            }
        }

        // Check for test specific blacklists
        TestCommandParserRegistry testRegistry = services.getService(TestCommandParserRegistry.class);
        for(String key: testRegistry.getCommandParsers().keySet()){
            for(Field element: Field.values()){
                MailFilterBlacklistProperty property = new MailFilterBlacklistProperty(BasicGroup.tests, key, element);
                String blacklistString = config.getProperty(userId, contextId, property);
                if(blacklistString!=null){
                    addBlacklist(property, blacklistString, result);
                }
            }
        }

        return new Blacklist(result);
    }

    private void addBlacklist(MailFilterBlacklistProperty key, String blacklist, Map<String, Set<String>> result) {
        if (!Strings.isEmpty(blacklist)) {
            String[] blacklists = Strings.splitByComma(blacklist);
            if (key.getField() != null) {
                result.put(Blacklist.key(key.getBase(), key.getSub(), key.getField()), validateBlacklist(key, blacklists));
            } else {
                result.put(key.getBase().name(), validateBlacklist(key, blacklists));
            }
        }
    }

    private Set<String> validateBlacklist(MailFilterBlacklistProperty type, String[] list){
        if(BasicGroup.actions.equals(type.getBase()) && type.getSub()==null){
            return validateActionBlacklist(list);
        }
        if(BasicGroup.tests.equals(type.getBase()) && type.getSub()==null){
            return validateTestBlacklist(list);
        }
        if(BasicGroup.comparisons.equals(type.getBase()) && type.getSub()==null){
            return validateComparatorBlacklist(list);
        }
        return new HashSet<String>(Arrays.asList(list));
    }

    private Set<String> validateActionBlacklist(String[] actions) {
        Set<String> result = new HashSet<String>(actions.length);
        ActionCommandParserRegistry actionRegistry = services.getService(ActionCommandParserRegistry.class);
        for (String action : actions) {
            if (actionRegistry.getCommandParsers().containsKey(action)) {
                result.add(action);
            } else {
                LOGGER.warn("'{}' is not a valid sieve action command and will be ignored!", action);
            }
        }
        return result;
    }

    private Set<String> validateTestBlacklist(String[] tests) {
        TestCommandParserRegistry testRegistry = services.getService(TestCommandParserRegistry.class);
        Set<String> result = new HashSet<String>(tests.length);
        for (String test : tests) {
            if (testRegistry.getCommandParsers().containsKey(test)) {
                result.add(test);
            } else {
                LOGGER.warn("'{}' is not a valid sieve test command and will be ignored!", test);
            }
        }
        return result;
    }

    private Set<String> validateComparatorBlacklist(String[] comps) {
        Set<String> result = new HashSet<String>(comps.length);
        for (String comp : comps) {
            if (MatchType.containsMatchType(comp)) {
                result.add(comp);
            } else {
                LOGGER.warn("'{}' is not a valid sieve comparator and will be ignored!", comp);
            }
        }
        return result;
    }

}
