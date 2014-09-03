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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

        RuleAndPosition(final Rule rule, final int position) {
            super();
            this.rule = rule;
            this.position = position;
        }

    }

    // ---------------------------------------------------------------------------------------------------------------- //

    private final Object lock;
    private final String scriptname;
    private final boolean useSIEVEResponseCodes;

    /**
     * Initializes a new {@link MailFilterServiceImpl}.
     */
    public MailFilterServiceImpl() {
        super();
        lock = new Object();
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        scriptname = config.getProperty(MailFilterProperties.Values.SCRIPT_NAME.property);
        useSIEVEResponseCodes = Boolean.parseBoolean(config.getProperty(MailFilterProperties.Values.USE_SIEVE_RESPONSE_CODES.property));
    }

    @Override
    public final int createFilterRule(final Credentials credentials, final Rule rule) throws OXException {
        synchronized (lock) {
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                final String activeScript = sieveHandler.getActiveScript();
                final String script = (activeScript != null) ? sieveHandler.getScript(activeScript) : "";

                final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);

                final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());

                if (isVacationRule(rule)) {
                    // A vacation rule...
                    final List<Rule> clientrules = clientrulesandrequire.getRules();
                    for (final Rule r : clientrules) {
                        if (isVacationRule(r)) {
                            throw MailFilterExceptionCode.DUPLICATE_VACATION_RULE.create();
                        }
                    }
                }

                changeIncomingVacationRule(rule);

                final int nextuid = insertIntoPosition(rule, rules, clientrulesandrequire);

                final String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
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
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public final void updateFilterRule(final Credentials credentials, final Rule rule, int uid) throws OXException {
        synchronized (lock) {
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                final String activeScript = sieveHandler.getActiveScript();
                if (null == activeScript) {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }

                final String script = fixParsingError(sieveHandler.getScript(activeScript));
                final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                final ClientRulesAndRequire clientRulesAndReq = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());
                final RuleAndPosition rightRule = getRightRuleForUniqueId(clientRulesAndReq.getRules(), uid);
                changeIncomingVacationRule(rightRule.rule);
                clientRulesAndReq.getRules().set(rightRule.position, rule);

                final String writeback = sieveTextFilter.writeback(clientRulesAndReq, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                LOGGER.debug("The following sieve script will be written:\n{}", writeback);

                writeScript(sieveHandler, activeScript, writeback);
            } catch (ParseException e) {
                throw MailFilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (SieveException e) {
                throw MailFilterExceptionCode.handleSieveException(e);
            } catch (final OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (final IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public void deleteFilterRule(final Credentials credentials, final int uid) throws OXException {
        synchronized (lock) {
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);

            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                final String activeScript = sieveHandler.getActiveScript();
                if (null == activeScript) {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }

                final String script = sieveHandler.getScript(activeScript);
                final RuleListAndNextUid rulesandid = sieveTextFilter.readScriptFromString(script);
                final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rulesandid.getRulelist(), null, rulesandid.isError());

                final List<Rule> rules = clientrulesandrequire.getRules();
                final RuleAndPosition deletedrule = getRightRuleForUniqueId(rules, uid);
                rules.remove(deletedrule.position);
                final String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
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
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public final void purgeFilters(final Credentials credentials) throws OXException {
        synchronized (lock) {
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                final String activeScript = sieveHandler.getActiveScript();
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
    public final String getActiveScript(final Credentials credentials) throws OXException {
        synchronized (lock) {
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                final String activeScript = sieveHandler.getActiveScript();
                return (null != activeScript) ? sieveHandler.getScript(activeScript) : "";
            } catch (final UnsupportedEncodingException e) {
                throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
            } catch (final IOException e) {
                throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } catch (final NumberFormatException nfe) {
                throw MailFilterExceptionCode.NAN.create(nfe, MailFilterExceptionCode.getNANString(nfe));
            } catch (final RuntimeException re) {
                throw MailFilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
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
    public List<Rule> listRules(final Credentials credentials, final FilterType flag) throws OXException {
        synchronized (lock) {
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                final String activeScript = sieveHandler.getActiveScript();
                final String script = null != activeScript ? sieveHandler.getScript(activeScript) : "";
                LOGGER.debug("The following sieve script will be parsed:\n{}", script);
                final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
                final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), flag.getFlag(), rules.isError());
                final List<Rule> clientRules = clientrulesandrequire.getRules();
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
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public List<Rule> listRules(Credentials credentials, List<FilterType> exclusionFlags) throws OXException {
        synchronized (lock) {
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                final String activeScript = sieveHandler.getActiveScript();
                final String script = null != activeScript ? sieveHandler.getScript(activeScript) : "";
                LOGGER.debug("The following sieve script will be parsed:\n{}", script);
                final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
                final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                final ClientRulesAndRequire splittedRules = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());

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
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public void reorderRules(final Credentials credentials, final int[] uids) throws OXException {
        synchronized (lock) {
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                final String activeScript = sieveHandler.getActiveScript();
                if (null != activeScript) {
                    final String script = sieveHandler.getScript(activeScript);
                    final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);

                    final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire( rules.getRulelist(), null, rules.isError());

                    final List<Rule> clientrules = clientrulesandrequire.getRules();
                    for (int i = 0; i < uids.length; i++) {
                        int uniqueid = uids[i];
                        final RuleAndPosition rightRule = getRightRuleForUniqueId(clientrules, uniqueid);
                        final int position = rightRule.position;
                        clientrules.remove(position);
                        clientrules.add(i, rightRule.rule);
                    }

                    final String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
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
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public final Rule getFilterRule(final Credentials credentials, final int uid) throws OXException {
        synchronized (lock) {
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);

            try {
                handlerConnect(sieveHandler, credentials.getSubject());

                final String activeScript = sieveHandler.getActiveScript();
                if (activeScript == null) {
                    throw MailFilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }

                final String script = fixParsingError(sieveHandler.getScript(activeScript));
                final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);
                final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());
                final List<Rule> clientrules = clientrulesandrequire.getRules();
                final RuleAndPosition rightRule = getRightRuleForUniqueId(clientrules, uid);

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
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getCapabilities(final Credentials credentials) throws OXException {
        synchronized (lock) {
            final SieveHandler sieveHandler = SieveHandlerFactory.getSieveHandler(credentials);
            try {
                handlerConnect(sieveHandler, credentials.getSubject());
                final Capabilities capabilities = sieveHandler.getCapabilities();
                return new HashSet<String>(capabilities.getSieve());
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw MailFilterExceptionCode.UNSUPPORTED_ENCODING.create(e);
                    } catch (final IOException e) {
                        throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e);
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private List<Rule> exclude(final Map<String, List<Rule>> flagged, List<FilterType> exclusionFlags) {
        final List<Rule> ret = new ArrayList<Rule>();
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
    private void changeIncomingVacationRule(final Rule rule) throws SieveException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String vacationdomains = config.getProperty(MailFilterProperties.Values.VACATION_DOMAINS.property);

        if (null != vacationdomains && 0 != vacationdomains.length()) {
            final IfCommand ifCommand = rule.getIfCommand();
            if (isVacationRule(rule)) {
                final List<Object> argList = new ArrayList<Object>();
                argList.add(createTagArg("is"));
                argList.add(createTagArg("domain"));

                final List<String> header = new ArrayList<String>();
                header.add("From");

                final String[] split = Strings.splitByComma(vacationdomains);

                argList.add(header);
                argList.add(Arrays.asList(split));
                final TestCommand testcommand = ifCommand.getTestcommand();
                final Commands command = testcommand.getCommand();
                final TestCommand newTestCommand = new TestCommand(Commands.ADDRESS, argList, new ArrayList<TestCommand>());
                if (Commands.TRUE.equals(command)) {
                    // No test until now
                    ifCommand.setTestcommand(newTestCommand);
                } else {
                    // Found other tests
                    final List<TestCommand> arrayList = new ArrayList<TestCommand>();
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
    private void changeOutgoingVacationRule(final List<Rule> clientrules) throws SieveException {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String vacationdomains = config.getProperty(MailFilterProperties.Values.VACATION_DOMAINS.property);

        if (null != vacationdomains && 0 != vacationdomains.length()) {
            for (final Rule rule : clientrules) {
                final IfCommand ifCommand = rule.getIfCommand();
                if (isVacationRule(rule)) {
                    final TestCommand testcommand = ifCommand.getTestcommand();
                    if (Commands.ADDRESS.equals(testcommand.getCommand())) {
                        // Test command found now check if it's the right one...
                        if (checkOwnVacation(testcommand.getArguments())) {
                            ifCommand.setTestcommand(new TestCommand(TestCommand.Commands.TRUE, new ArrayList<Object>(), new ArrayList<TestCommand>()));
                        }
                    } else if (Commands.ALLOF.equals(testcommand.getCommand())) {
                        // In this case we find "our" rule at the first place
                        final List<TestCommand> testcommands = testcommand.getTestcommands();
                        if (null != testcommands && testcommands.size() > 1) {
                            final TestCommand testCommand2 = testcommands.get(0);
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
    private boolean checkOwnVacation(final List<Object> arguments) {
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
    private void setUIDInRule(final Rule rule, final int uid) {
        final RuleComment name = rule.getRuleComment();
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
    private boolean isVacationRule(final Rule rule) {
        final RuleComment ruleComment = rule.getRuleComment();
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
    private void writeScript(final SieveHandler sieveHandler, final String activeScript, final String writeback) throws OXSieveHandlerException, IOException, UnsupportedEncodingException {
        final StringBuilder commandBuilder = new StringBuilder(64);

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
    private String fixParsingError(final String script) {
        final String pattern = ":addresses\\s+:";
        return script.replaceAll(pattern, ":addresses \"\" :");
    }

    /**
     * Search within the given List of Rules for the one matching the specified UID
     */
    private RuleAndPosition getRightRuleForUniqueId(final List<Rule> clientrules, final int uniqueid) throws OXException {
        for (int i = 0; i < clientrules.size(); i++) {
            final Rule rule = clientrules.get(i);
            if (uniqueid == rule.getUniqueId()) {
                return new RuleAndPosition(rule, i);
            }
        }
        return null;
    }

    private TagArgument createTagArg(final String string) {
        final Token token = new Token();
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
    private void handlerConnect(final SieveHandler sieveHandler, final Subject subject) throws OXException, OXSieveHandlerException {
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
     * @param clientrulesandrequire
     * @return the UID of rule and hence the position
     * @throws OXException
     */
    private int insertIntoPosition(final Rule rule, final RuleListAndNextUid rules, final ClientRulesAndRequire clientrulesandrequire) throws OXException {
        int position = rule.getPosition();
        final List<Rule> clientrules = clientrulesandrequire.getRules();
        if (position >= clientrules.size()) {
            throw MailFilterExceptionCode.BAD_POSITION.create(Integer.valueOf(position));
        }
        final int nextuid = rules.getNextuid();
        setUIDInRule(rule, nextuid);
        if (position != -1) {
            clientrules.add(position, rule);
        } else {
            clientrules.add(rule);
            position = clientrules.size() - 1;
        }
        return nextuid;
    }

}
