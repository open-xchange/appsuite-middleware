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

package com.openexchange.mailfilter.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.security.auth.Subject;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.ParseException;
import org.apache.jsieve.parser.generated.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.export.Capabilities;
import com.openexchange.jsieve.export.SieveHandler;
import com.openexchange.jsieve.export.SieveHandlerFactory;
import com.openexchange.jsieve.export.SieveTextFilter;
import com.openexchange.jsieve.export.SieveTextFilter.ClientRulesAndRequire;
import com.openexchange.jsieve.export.SieveTextFilter.RuleListAndNextUid;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerException;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerInvalidCredentialsException;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.services.Services;

/**
 * {@link MailFilterServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class MailFilterServiceImpl implements MailFilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFilterServiceImpl.class);

    private static final class RuleAndPosition {

        final int position;
        final Rule rule;

        RuleAndPosition(Rule rule, int position) {
            super();
            this.rule = rule;
            this.position = position;
        }

    }

    private static final ConcurrentMap<Key, Object> LOCKS = new ConcurrentHashMap<Key, Object>(256, 0.9f, 1);

    /**
     * Gets the lock instance for specified session.
     *
     * @param creds The credentials
     * @return The lock instance
     */
    private static Object lockFor(Credentials creds) {
        if (null == creds) {
            // Any...
            return new Object();
        }
        Key key = new Key(creds.getUserid(), creds.getContextid());
        Object lock = LOCKS.get(key);
        if (null == lock) {
            Object newLock = new Object();
            lock = LOCKS.putIfAbsent(key, newLock);
            if (null == lock) {
                lock = newLock;
            }
        }
        return lock;
    }

    /**
     * Removes the lock instance associated with specified session.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void removeFor(int userId, int contextId) {
        LOCKS.remove(new Key(userId, contextId));
    }

    // ---------------------------------------------------------------------------------------------------------------- //

    private final String scriptname;
    private final boolean useSIEVEResponseCodes;

    /**
     * Initializes a new {@link MailFilterServiceImpl}.
     */
    public MailFilterServiceImpl() {
        super();
        ConfigurationService config = Services.getService(ConfigurationService.class);
        scriptname = config.getProperty(MailFilterProperties.Values.SCRIPT_NAME.property);
        useSIEVEResponseCodes = Boolean.parseBoolean(config.getProperty(MailFilterProperties.Values.USE_SIEVE_RESPONSE_CODES.property));
    }

    @Override
    public final int createFilterRule(Credentials credentials, Rule rule) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                String activeScript = sieveHandler.getActiveScript();
                String script = (activeScript != null) ? sieveHandler.getScript(activeScript) : "";

                RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);

                ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());

                if (isVacationRule(rule)) {
                    // A vacation rule...
                    List<Rule> clientrules = clientrulesandrequire.getRules();
                    for (Rule r : clientrules) {
                        if (isVacationRule(r)) {
                            throw MailFilterExceptionCode.DUPLICATE_VACATION_RULE.create();
                        }
                    }
                }

                changeIncomingVacationRule(rule);

                int nextuid = insertIntoPosition(rule, rules, clientrulesandrequire);

                String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                LOGGER.debug("The following sieve script will be written:\n{}", writeback);
                writeScript(sieveHandler, activeScript, writeback);

                return nextuid;
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (SieveException e) {
                throw MailFilterExceptionCode.handleSieveException(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public final void updateFilterRule(Credentials credentials, Rule rule, int uid) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                String activeScript = sieveHandler.getActiveScript();
                if (null == activeScript) {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }

                String script = fixParsingError(sieveHandler.getScript(activeScript));
                RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                ClientRulesAndRequire clientRulesAndReq = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());
                RuleAndPosition rightRule = getRightRuleForUniqueId(clientRulesAndReq.getRules(), uid);
                changeIncomingVacationRule(rightRule.rule);
                if (rightRule.position == rule.getPosition()) {
                    clientRulesAndReq.getRules().set(rightRule.position, rule);
                } else {
                    clientRulesAndReq.getRules().remove(rightRule.position);
                    clientRulesAndReq.getRules().add(rule.getPosition(), rule);
                }
                String writeback = sieveTextFilter.writeback(clientRulesAndReq, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                LOGGER.debug("The following sieve script will be written:\n{}", writeback);

                writeScript(sieveHandler, activeScript, writeback);
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (SieveException e) {
                throw MailFilterExceptionCode.handleSieveException(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public void deleteFilterRule(Credentials credentials, int uid) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);

            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                String activeScript = sieveHandler.getActiveScript();
                if (null == activeScript) {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }

                String script = sieveHandler.getScript(activeScript);
                RuleListAndNextUid rulesandid = sieveTextFilter.readScriptFromString(script);
                ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rulesandid.getRulelist(), null, rulesandid.isError());

                List<Rule> rules = clientrulesandrequire.getRules();
                RuleAndPosition deletedrule = getRightRuleForUniqueId(rules, uid);
                if(deletedrule == null) {
                    throw MailFilterExceptionCode.BAD_POSITION.create(uid);
                }
                rules.remove(deletedrule.position);
                String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                writeScript(sieveHandler, activeScript, writeback);
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (SieveException e) {
                throw MailFilterExceptionCode.handleSieveException(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public final void purgeFilters(Credentials credentials) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                String activeScript = sieveHandler.getActiveScript();
                writeScript(sieveHandler, activeScript, "");
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            }
        }
    }

    @Override
    public final String getActiveScript(Credentials credentials) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                String activeScript = sieveHandler.getActiveScript();
                return (null != activeScript) ? sieveHandler.getScript(activeScript) : "";
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (NumberFormatException nfe) {
                throw MailFilterExceptionCode.NAN.create(nfe, MailFilterExceptionCode.getNANString(nfe));
            } catch (RuntimeException re) {
                throw MailFilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mailfilter.MailFilterService#listRules(com.openexchange.mailfilter.Credentials)
     */
    @Override
    public List<Rule> listRules(Credentials credentials, FilterType flag) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                String activeScript = sieveHandler.getActiveScript();
                String script = null != activeScript ? sieveHandler.getScript(activeScript) : "";
                LOGGER.debug("The following sieve script will be parsed:\n{}", script);
                SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
                RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), flag.getFlag(), rules.isError());
                List<Rule> clientRules = clientrulesandrequire.getRules();
                changeOutgoingVacationRule(clientRules);
                return clientRules;
            } catch (SieveException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public List<Rule> listRules(Credentials credentials, List<FilterType> exclusionFlags) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                String activeScript = sieveHandler.getActiveScript();
                String script = null != activeScript ? sieveHandler.getScript(activeScript) : "";
                LOGGER.debug("The following sieve script will be parsed:\n{}", script);
                SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
                RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                ClientRulesAndRequire splittedRules = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());

                if(splittedRules.getFlaggedRules() != null) {
                    return exclude(splittedRules.getFlaggedRules(), exclusionFlags);
                }
                return splittedRules.getRules();
            } catch (SieveException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public void reorderRules(Credentials credentials, int[] uids) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                String activeScript = sieveHandler.getActiveScript();
                if (null != activeScript) {
                    String script = sieveHandler.getScript(activeScript);
                    RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);

                    ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire( rules.getRulelist(), null, rules.isError());

                    List<Rule> clientrules = clientrulesandrequire.getRules();
                    for (int i = 0; i < uids.length; i++) {
                        int uniqueid = uids[i];
                        RuleAndPosition rightRule = getRightRuleForUniqueId(clientrules, uniqueid);
                        int position = rightRule.position;
                        clientrules.remove(position);
                        clientrules.add(i, rightRule.rule);
                    }

                    String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                    writeScript(sieveHandler, activeScript, writeback);
                } else {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }
            } catch (SieveException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public final Rule getFilterRule(Credentials credentials, int uid) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);

            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                String activeScript = sieveHandler.getActiveScript();
                if (activeScript == null) {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }

                String script = fixParsingError(sieveHandler.getScript(activeScript));
                RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());
                List<Rule> clientrules = clientrulesandrequire.getRules();
                RuleAndPosition rightRule = getRightRuleForUniqueId(clientrules, uid);

                // no rule found
                if(rightRule == null) {
                    return null;
                }

                return rightRule.rule;
            } catch (UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (SieveException e) {
                throw MailFilterExceptionCode.handleSieveException(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getCapabilities(Credentials credentials) throws OXException {
        Object lock = lockFor(credentials);
        synchronized (lock) {
            SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                Capabilities capabilities = sieveHandler.getCapabilities();
                return new HashSet<String>(capabilities.getSieve());
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private List<Rule> exclude(Map<String, List<Rule>> flagged, List<FilterType> exclusionFlags) {
        List<Rule> ret = new ArrayList<Rule>();
        for(FilterType flag : exclusionFlags) {
            flagged.remove(flag);
        }
        for(List<Rule> l : flagged.values()) {
            ret.addAll(l);
        }
        return ret;
    }

    /**
     * Change a vacation rule
     *
     * @param rule the rule to be changed
     * @throws SieveException
     */
    private void changeIncomingVacationRule(Rule rule) throws SieveException {
        ConfigurationService config = Services.getService(ConfigurationService.class);
        String vacationdomains = config.getProperty(MailFilterProperties.Values.VACATION_DOMAINS.property);

        if (null != vacationdomains && 0 != vacationdomains.length()) {
            IfCommand ifCommand = rule.getIfCommand();
            if (isVacationRule(rule)) {
                List<Object> argList = new ArrayList<Object>();
                argList.add(createTagArg("is"));
                argList.add(createTagArg("domain"));

                List<String> header = new ArrayList<String>();
                header.add("From");

                String[] split = Strings.splitByComma(vacationdomains);

                argList.add(header);
                argList.add(Arrays.asList(split));
                TestCommand testcommand = ifCommand.getTestcommand();
                Commands command = testcommand.getCommand();
                TestCommand newTestCommand = new TestCommand(Commands.ADDRESS, argList, new ArrayList<TestCommand>());
                if (Commands.TRUE.equals(command)) {
                    // No test until now
                    ifCommand.setTestcommand(newTestCommand);
                } else {
                    // Found other tests
                    List<TestCommand> arrayList = new ArrayList<TestCommand>();
                    arrayList.add(newTestCommand);
                    arrayList.add(testcommand);
                    ifCommand.setTestcommand(new TestCommand(Commands.ALLOF, new ArrayList<Object>(), arrayList));
                }
            }
        }
    }

    /**
     * Change the outgoing vacation rule
     *
     * @param clientrules
     * @throws SieveException
     */
    private void changeOutgoingVacationRule(List<Rule> clientrules) throws SieveException {
        ConfigurationService config = Services.getService(ConfigurationService.class);
        String vacationdomains = config.getProperty(MailFilterProperties.Values.VACATION_DOMAINS.property);

        if (null != vacationdomains && 0 != vacationdomains.length()) {
            for (Rule rule : clientrules) {
                IfCommand ifCommand = rule.getIfCommand();
                if (isVacationRule(rule)) {
                    TestCommand testcommand = ifCommand.getTestcommand();
                    if (Commands.ADDRESS.equals(testcommand.getCommand())) {
                        // Test command found now check if it's the right one...
                        if (checkOwnVacation(testcommand.getArguments())) {
                            ifCommand.setTestcommand(new TestCommand(TestCommand.Commands.TRUE, new ArrayList<Object>(), new ArrayList<TestCommand>()));
                        }
                    } else if (Commands.ALLOF.equals(testcommand.getCommand())) {
                        // In this case we find "our" rule at the first place
                        List<TestCommand> testcommands = testcommand.getTestcommands();
                        if (null != testcommands && testcommands.size() > 1) {
                            TestCommand testCommand2 = testcommands.get(0);
                            if (checkOwnVacation(testCommand2.getArguments())) {
                                // now remove...
                                if (2 == testcommands.size()) {
                                    // If this is one of two convert the rule
                                    ifCommand.setTestcommand(testcommands.get(1));
                                } else if (testcommands.size() > 2) {
                                    // If we have more than one just remove it...
                                    testcommands.remove(0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check own vacation
     * @param arguments
     * @return
     */
    private boolean checkOwnVacation(List<Object> arguments) {
        return null != arguments
            && null != arguments.get(0) && arguments.get(0) instanceof TagArgument && ":is".equals(((TagArgument)arguments.get(0)).getTag())
            && null != arguments.get(1) && arguments.get(1) instanceof TagArgument && ":domain".equals(((TagArgument)arguments.get(1)).getTag())
            && null != arguments.get(2) && arguments.get(2) instanceof List<?> && "From".equals(((List<?>)arguments.get(2)).get(0));
    }

    /**
     * Set the specified UID to the specified Rule
     *
     * @param rule the rule
     * @param uid the UID
     */
    private void setUIDInRule(Rule rule, int uid) {
        RuleComment name = rule.getRuleComment();
        if (null != name) {
            name.setUniqueid(uid);
        } else {
            rule.setRuleComments(new RuleComment(uid));
        }
    }

    /**
     * Determine whether the specified rule is a vacation rule
     *
     * @param rule
     * @return true if the specified rule is a vacation rule; false otherwise
     */
    private boolean isVacationRule(Rule rule) {
        RuleComment ruleComment = rule.getRuleComment();
        return (null != ruleComment) && (null != ruleComment.getFlags()) && ruleComment.getFlags().contains("vacation") && rule.getIfCommand() != null && ActionCommand.Commands.VACATION.equals(rule.getIfCommand().getFirstCommand());
    }

    /**
     * Used to perform checks to set the right script name when writing
     *
     * @param sieveHandler the sieveHandler to use
     * @param activeScript the activeScript
     * @param writeback the write-back String
     * @throws OXSieveHandlerException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private void writeScript(SieveHandler sieveHandler, String activeScript, String writeback) throws OXSieveHandlerException, IOException, UnsupportedEncodingException {
        StringBuilder commandBuilder = new StringBuilder(64);

        if (null != activeScript && activeScript.equals(this.scriptname)) {
            sieveHandler.setScript(activeScript, writeback.getBytes(com.openexchange.java.Charsets.UTF_8), commandBuilder);
            sieveHandler.setScriptStatus(activeScript, true, commandBuilder);
        } else {
            sieveHandler.setScript(this.scriptname, writeback.getBytes(com.openexchange.java.Charsets.UTF_8), commandBuilder);
            sieveHandler.setScriptStatus(this.scriptname, true, commandBuilder);
        }
    }

    /**
     * Fix parsing
     *
     * @param script
     * @return
     */
    private String fixParsingError(String script) {
        String pattern = ":addresses\\s+:";
        return script.replaceAll(pattern, ":addresses \"\" :");
    }

    /**
     * Search within the given List of Rules for the one matching the specified UID
     */
    private RuleAndPosition getRightRuleForUniqueId(List<Rule> clientrules, int uniqueid) throws OXException {
        for (int i = 0; i < clientrules.size(); i++) {
            Rule rule = clientrules.get(i);
            if (uniqueid == rule.getUniqueId()) {
                return new RuleAndPosition(rule, i);
            }
        }
        return null;
    }

    private TagArgument createTagArg(String string) {
        Token token = new Token();
        token.image = ":" + string;
        return new TagArgument(token);
    }

    /**
     *
     * @param sieveHandler
     * @throws OXException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws OXSieveHandlerException
     * @throws OXSieveHandlerInvalidCredentialsException
     * @throws PrivilegedActionException
     */
    private void handlerConnect(final SieveHandler sieveHandler, Subject subject) throws OXException, OXSieveHandlerException {
        try {
            if (subject != null) {
                Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {

                    @Override
                    public Object run() throws Exception {
                        sieveHandler.initializeConnection();
                        return null;
                    }
                });
            } else {
                sieveHandler.initializeConnection();
            }
        } catch (OXSieveHandlerInvalidCredentialsException e) {
            throw MailFilterExceptionCode.INVALID_CREDENTIALS.create(e);
        } catch (PrivilegedActionException e) {
            throw MailFilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
        } catch (UnsupportedEncodingException e) {
            throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
        } catch (IOException e) {
            throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
        }
    }

    /**
     * Find the correct position into the array
     *
     * @param rule the rule to add
     * @param rules the rules list
     * @param clientRulesAndRequire
     * @return the UID of rule and hence the position
     * @throws OXException
     */
    private int insertIntoPosition(Rule rule, RuleListAndNextUid rules, ClientRulesAndRequire clientRulesAndRequire) throws OXException {
        int position = rule.getPosition();
        List<Rule> clientRules = clientRulesAndRequire.getRules();
        if (position > clientRules.size()) {
            throw MailFilterExceptionCode.BAD_POSITION.create(Integer.valueOf(position));
        }
        int nextUid = rules.getNextuid();
        setUIDInRule(rule, nextUid);
        if (position != -1) {
            clientRules.add(position, rule);
        } else {
            clientRules.add(rule);
            position = clientRules.size() - 1;
        }
        return nextUid;
    }

    // --------------------------------------------------------------------------------------------------------------------- //

    private static final class Key {

        private final int cid;
        private final int user;
        private final int hash;

        Key(int user, int cid) {
            super();
            this.user = user;
            this.cid = cid;
            int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    } // End of class Key

}
