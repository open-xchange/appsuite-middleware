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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.ajax.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.internet.idn.IDNA;
import javax.security.auth.Subject;
import org.apache.commons.logging.Log;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.ParseException;
import org.apache.jsieve.parser.generated.TokenMgrError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.export.Capabilities;
import com.openexchange.jsieve.export.SIEVEResponse;
import com.openexchange.jsieve.export.SieveHandler;
import com.openexchange.jsieve.export.SieveTextFilter;
import com.openexchange.jsieve.export.SieveTextFilter.ClientRulesAndRequire;
import com.openexchange.jsieve.export.SieveTextFilter.RuleListAndNextUid;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerException;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerInvalidCredentialsException;
import com.openexchange.log.LogFactory;
import com.openexchange.mailfilter.ajax.Credentials;
import com.openexchange.mailfilter.ajax.Parameter;
import com.openexchange.mailfilter.ajax.actions.AbstractRequest.Parameters;
import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterExceptionCode;
import com.openexchange.mailfilter.ajax.json.AbstractObject2JSON2Object;
import com.openexchange.mailfilter.ajax.json.Rule2JSON2Rule;
import com.openexchange.mailfilter.internal.MailFilterProperties;
import com.openexchange.mailfilter.services.MailFilterServletServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public class MailfilterAction extends AbstractAction<Rule, MailfilterRequest> {

    private static final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(MailfilterAction.class));

    private static final ConcurrentMap<Key, MailfilterAction> INSTANCES = new ConcurrentHashMap<Key, MailfilterAction>();

    private static final String KERBEROS_SESSION_SUBJECT = "kerberosSubject";

    /**
     * Gets the {@link MailfilterAction} instance for specified session.
     *
     * @param session The session
     * @return The appropriate {@link MailfilterAction} instance
     */
    public static MailfilterAction valueFor(final Session session) {
        final Key key = new Key(session.getUserId(), session.getContextId());
        MailfilterAction action = INSTANCES.get(key);
        if (null == action) {
            final Subject subject = (Subject)session.getParameter(KERBEROS_SESSION_SUBJECT);
            final MailfilterAction newaction = new MailfilterAction(subject);
            action = INSTANCES.putIfAbsent(key, newaction);
            if (null == action) {
                action = newaction;
            }
        }
        return action;
    }

    /**
     * Removes the {@link MailfilterAction} instance associated with specified session.
     *
     * @param session The session
     */
    public static void removeFor(final Session session) {
        INSTANCES.remove(new Key(session.getUserId(), session.getContextId()));
    }

    private static final Object[] EMPTY_ARGS = new Object[0];

    private static final class RuleAndPosition {
        private final int position;

        private final Rule rule;

        /**
         * @param rule
         * @param position
         */
        public RuleAndPosition(final Rule rule, final int position) {
            super();
            this.rule = rule;
            this.position = position;
        }

        /**
         * @return the position
         */
        public final int getPosition() {
            return position;
        }

        /**
         * @return the rule
         */
        public final Rule getRule() {
            return rule;
        }

    }

    private static final AbstractObject2JSON2Object<Rule> CONVERTER = new Rule2JSON2Rule();

    private final Object mutex;

    private final String scriptname;

    private boolean useSIEVEResponseCodes = false;

    private final Subject krbSubject;

    /**
     * Default constructor.
     */
    public MailfilterAction(final Subject krbSubject) {
        super();
        this.krbSubject = krbSubject;
        final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(
                ConfigurationService.class);
        scriptname = config.getProperty(MailFilterProperties.Values.SCRIPT_NAME.property);
        useSIEVEResponseCodes = Boolean.parseBoolean(config.getProperty(MailFilterProperties.Values.USE_SIEVE_RESPONSE_CODES.property));
        mutex = new Object();
    }

    private void handlerConnect(final SieveHandler sieveHandler) throws UnsupportedEncodingException, IOException, OXSieveHandlerException, OXSieveHandlerInvalidCredentialsException, PrivilegedActionException {
        if (null != krbSubject) {
            Subject.doAs(krbSubject, new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    sieveHandler.initializeConnection();
                    return null;
                }
            });
        } else {
            sieveHandler.initializeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject actionConfig(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveHandler sieveHandler = connectRight(credentials);
            // First fetch configuration:
            JSONObject tests = null;
            try {
                handlerConnect(sieveHandler);
                final Capabilities capabilities = sieveHandler.getCapabilities();
                final ArrayList<String> sieve = capabilities.getSieve();
                tests = getTestAndActionObjects(new HashSet<String>(capabilities.getSieve()));
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
            	throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final JSONException e) {
                throw OXMailfilterExceptionCode.JSON_ERROR.create(e, e.getMessage());
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
            return tests;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void actionDelete(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();
                if (null != activeScript) {
                    final String script = sieveHandler.getScript(activeScript);
                    final RuleListAndNextUid rulesandid = sieveTextFilter.readScriptFromString(script);
                    final ClientRulesAndRequire clientrulesandrequire =
                        sieveTextFilter.splitClientRulesAndRequire(rulesandid.getRulelist(), null, rulesandid.isError());
                    final String body = request.getBody();
                    final JSONObject json = new JSONObject(body);

                    final ArrayList<Rule> rules = clientrulesandrequire.getRules();
                    final RuleAndPosition deletedrule =
                        getRightRuleForUniqueId(rules, getUniqueId(json), credentials.getRightUsername(), credentials.getContextString());
                    rules.remove(deletedrule.getPosition());
                    final String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                    writeScript(sieveHandler, activeScript, writeback);
                } else {
                    throw OXMailfilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
            	throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final ParseException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final SieveException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e, EMPTY_ARGS);
            } catch (final TokenMgrError error) {
                throw OXMailfilterExceptionCode.LEXICAL_ERROR.create(error, error.getMessage());
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONArray actionList(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Parameters parameters = request.getParameters();
            final Credentials credentials = request.getCredentials();
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();
                final String script;
                if (null != activeScript) {
                    script = sieveHandler.getScript(activeScript);
                } else {
                    script = "";
                }
                if (log.isDebugEnabled()) {
                    log.debug("The following sieve script will be parsed:\n"
                        + script);
                }
                final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
                final RuleListAndNextUid readScriptFromString = sieveTextFilter.readScriptFromString(script);
                final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(
                    readScriptFromString.getRulelist(),
                    parameters.getParameter(Parameter.FLAG),
                    readScriptFromString.isError());
                final ArrayList<Rule> clientrules = clientrulesandrequire.getRules();
                changeOutgoingVacationRule(clientrules);
                return CONVERTER.write(clientrules.toArray(new Rule[clientrules.size()]));
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(
                    e,
                    sieveHandler.getSieveHost(),
                    Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
                throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final ParseException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final SieveException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e, EMPTY_ARGS);
            } catch (final TokenMgrError error) {
                throw OXMailfilterExceptionCode.LEXICAL_ERROR.create(error, error.getMessage());
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int actionNew(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();
                final String script;
                if (null != activeScript) {
                    script = sieveHandler.getScript(activeScript);
                } else {
                    script = "";
                }
                final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);

                final ClientRulesAndRequire clientrulesandrequire =
                    sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());

                final String body = request.getBody();
                final JSONObject json = new JSONObject(body);
                final Rule newrule = CONVERTER.parse(json);

                if (isVacationRule(newrule)) {
                    // A vacation rule...
                    final ArrayList<Rule> clientrules = clientrulesandrequire.getRules();
                    for (final Rule rule : clientrules) {
                        if (isVacationRule(rule)) {
                            throw OXMailfilterExceptionCode.DUPLICATE_VACATION_RULE.create();
                        }
                    }
                }

                changeIncomingVacationRule(newrule);

                // Now find the right position inside the array
                int position = newrule.getPosition();
                final ArrayList<Rule> clientrules = clientrulesandrequire.getRules();
                if (position >= clientrules.size()) {
                    throw OXMailfilterExceptionCode.BAD_POSITION.create(Integer.valueOf(position));
                }
                final int nextuid = rules.getNextuid();
                setUidInRule(newrule, nextuid);
                if (-1 != position) {
                    clientrules.add(position, newrule);
                } else {
                    clientrules.add(newrule);
                    position = clientrules.size() - 1;
                }
                final String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                if (log.isDebugEnabled()) {
                    log.debug("The following sieve script will be written:\n" + writeback);
                }
                writeScript(sieveHandler, activeScript, writeback);

                return nextuid;
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
                throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final ParseException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final SieveException e) {
                throw handleSieveException(e);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
            } catch (final TokenMgrError error) {
                throw OXMailfilterExceptionCode.LEXICAL_ERROR.create(error, error.getMessage());
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    @Override
    protected void actionReorder(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();
                if (null != activeScript) {
                    final String script = sieveHandler.getScript(activeScript);
                    final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(script);

                    final ClientRulesAndRequire clientrulesandrequire =
                        sieveTextFilter.splitClientRulesAndRequire(rules.getRulelist(), null, rules.isError());

                    final String body = request.getBody();
                    final JSONArray json = new JSONArray(body);

                    final ArrayList<Rule> clientrules = clientrulesandrequire.getRules();
                    for (int i = 0; i < json.length(); i++) {
                        final int uniqueid = json.getInt(i);
                        final RuleAndPosition rightRule =
                            getRightRuleForUniqueId(
                                clientrules,
                                Integer.valueOf(uniqueid),
                                credentials.getRightUsername(),
                                credentials.getContextString());
                        final int position = rightRule.getPosition();
                        clientrules.remove(position);
                        clientrules.add(i, rightRule.getRule());
                    }

                    final String writeback = sieveTextFilter.writeback(clientrulesandrequire, new HashSet<String>(sieveHandler.getCapabilities().getSieve()));
                    writeScript(sieveHandler, activeScript, writeback);
                } else {
                    throw OXMailfilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
            	throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final ParseException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final SieveException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e, EMPTY_ARGS);
            } catch (final TokenMgrError error) {
                throw OXMailfilterExceptionCode.LEXICAL_ERROR.create(error, error.getMessage());
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void actionUpdate(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();
                if (null != activeScript) {
                    final String script = fixParsingError(sieveHandler
                        .getScript(activeScript));
                    final RuleListAndNextUid rules = sieveTextFilter
                        .readScriptFromString(script);
                    final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter
                        .splitClientRulesAndRequire(rules.getRulelist(),
                            null, rules.isError());
                    final String body = request.getBody();
                    final JSONObject json = new JSONObject(body);
                    final Integer uniqueid = getUniqueId(json);
                    final ArrayList<Rule> clientrules = clientrulesandrequire
                        .getRules();
                    if (null == uniqueid) {
                        throw OXMailfilterExceptionCode.ID_MISSING.create();
                    }
                    // First get the right rule which should be modified...
                    final RuleAndPosition rightRule = getRightRuleForUniqueId(
                        clientrules, uniqueid,
                        credentials.getRightUsername(),
                        credentials.getContextString());
                    CONVERTER.parse(rightRule.getRule(), json);
                    changeIncomingVacationRule(rightRule.getRule());
                    final String writeback = sieveTextFilter.writeback(
                        clientrulesandrequire, new HashSet<String>(
                            sieveHandler.getCapabilities().getSieve()));
                    if (log.isDebugEnabled()) {
                        log.debug("The following sieve script will be written:\n"
                            + writeback);
                    }
                    writeScript(sieveHandler, activeScript, writeback);
                } else {
                    throw OXMailfilterExceptionCode.NO_ACTIVE_SCRIPT.create();
                }
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
                throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final ParseException e) {
                throw OXMailfilterExceptionCode.SIEVE_ERROR.create(e, e.getMessage());
            } catch (final SieveException e) {
                throw handleSieveException(e);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e, EMPTY_ARGS);
            } catch (final TokenMgrError error) {
                throw OXMailfilterExceptionCode.LEXICAL_ERROR.create(error, error.getMessage());
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    @Override
    protected void actionDeleteScript(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();

                writeScript(sieveHandler, activeScript, "");

            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
            	throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    @Override
    protected String actionGetScript(final MailfilterRequest request) throws OXException {
        synchronized (mutex) {
            final Credentials credentials = request.getCredentials();
            final SieveHandler sieveHandler = connectRight(credentials);
            try {
                handlerConnect(sieveHandler);
                final String activeScript = sieveHandler.getActiveScript();
                if (null != activeScript) {
                    return sieveHandler.getScript(activeScript);
                } else {
                    return "";
                }
            } catch (final UnsupportedEncodingException e) {
                throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
            } catch (final IOException e) {
                throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
            } catch (final OXSieveHandlerException e) {
            	throw handleParsingException(e, credentials);
            } catch (final OXSieveHandlerInvalidCredentialsException e) {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(e, EMPTY_ARGS);
            } catch (final NumberFormatException nfe) {
                throw OXMailfilterExceptionCode.NAN.create(nfe, getNANString(nfe));
            } catch (final RuntimeException re) {
                throw OXMailfilterExceptionCode.PROBLEM.create(re, re.getMessage());
            } catch (final PrivilegedActionException e) {
                throw OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e);
            } finally {
                if (null != sieveHandler) {
                    try {
                        sieveHandler.close();
                    } catch (final UnsupportedEncodingException e) {
                        throw OXMailfilterExceptionCode.UNSUPPORTED_ENCODING.create(e, EMPTY_ARGS);
                    } catch (final IOException e) {
                        throw OXMailfilterExceptionCode.IO_CONNECTION_ERROR.create(e, EMPTY_ARGS);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rule2JSON2Rule getConverter() {
        return new Rule2JSON2Rule();
    }

    // protected so that we can test this
    protected String getRightPassword(final ConfigurationService config, final Credentials creds) throws OXException {
        final String passwordsrc = config.getProperty(MailFilterProperties.Values.SIEVE_PASSWORDSRC.property);
        if (MailFilterProperties.PasswordSource.SESSION.name.equals(passwordsrc)) {
            return creds.getPassword();
        } else if (MailFilterProperties.PasswordSource.GLOBAL.name.equals(passwordsrc)) {
            final String masterpassword = config.getProperty(MailFilterProperties.Values.SIEVE_MASTERPASSWORD.property);
            if (null == masterpassword || masterpassword.length() == 0) {
                throw OXMailfilterExceptionCode.NO_MASTERPASSWORD_SET.create();
            }
            return masterpassword;
        } else {
            throw OXMailfilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }
    }

    private SieveHandler connectRight(final Credentials creds) throws OXException {
        final SieveHandler sieveHandler;
        final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(
                ConfigurationService.class);

        final String logintype = config.getProperty(MailFilterProperties.Values.SIEVE_LOGIN_TYPE.property);
        final int sieve_port;
        final String sieve_server;
        User storageUser = null;
        if (MailFilterProperties.LoginTypes.GLOBAL.name.equals(logintype)) {
            sieve_server = config.getProperty(MailFilterProperties.Values.SIEVE_SERVER.property);
            if (null == sieve_server) {
                throw OXMailfilterExceptionCode.PROPERTY_ERROR.create(MailFilterProperties.Values.SIEVE_SERVER.property);
            }
            try {
                sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
            } catch (final RuntimeException e) {
                throw OXMailfilterExceptionCode.PROPERTY_ERROR.create(e, MailFilterProperties.Values.SIEVE_PORT.property);
            }
        } else if (MailFilterProperties.LoginTypes.USER.name.equals(logintype)) {
            storageUser = UserStorage.getStorageUser(creds.getUserid(), creds.getContextid());
            if (null != storageUser) {
                final String mailServerURL = storageUser.getImapServer();
                final URI uri;
                try {
                    uri = URIParser.parse(IDNA.toASCII(mailServerURL), URIDefaults.IMAP);
                } catch (final URISyntaxException e) {
                    throw OXMailfilterExceptionCode.NO_SERVERNAME_IN_SERVERURL.create(e, mailServerURL);
                }
                sieve_server = uri.getHost();
                try {
                    sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
                } catch (final RuntimeException e) {
                    throw OXMailfilterExceptionCode.PROPERTY_ERROR.create(e,
                            MailFilterProperties.Values.SIEVE_PORT.property);
                }
            } else {
                throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create("Could not get a valid user object for uid "
                        + creds.getUserid() + " and contextid " + creds.getContextid());
            }
        } else {
            throw OXMailfilterExceptionCode.NO_VALID_LOGIN_TYPE.create();
        }
        /*
         * Get SIEVE_AUTH_ENC property
         */
        final String authEnc = config.getProperty(MailFilterProperties.Values.SIEVE_AUTH_ENC.property, MailFilterProperties.Values.SIEVE_AUTH_ENC.def);
        /*
         * Establish SieveHandler
         */
        final String credsrc = config.getProperty(MailFilterProperties.Values.SIEVE_CREDSRC.property);
        if (MailFilterProperties.CredSrc.SESSION.name.equals(credsrc) || MailFilterProperties.CredSrc.SESSION_FULL_LOGIN.name.equals(credsrc)) {
            final String username = creds.getUsername();
            final String authname = creds.getAuthname();
            final String password = getRightPassword(config, creds);
            if (null != username) {
                sieveHandler = new SieveHandler(username, authname, password, sieve_server, sieve_port, authEnc);
            } else {
                sieveHandler = new SieveHandler(authname, password, sieve_server, sieve_port, authEnc);
            }
        } else if (MailFilterProperties.CredSrc.IMAP_LOGIN.name.equals(credsrc)) {
            final String authname;
            if (null != storageUser) {
                authname = storageUser.getImapLogin();
            } else {
                storageUser = UserStorage.getStorageUser(creds.getUserid(), creds.getContextid());
                if (null != storageUser) {
                    authname = storageUser.getImapLogin();
                } else {
                    throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create(
                            "Could not get a valid user object for uid " + creds.getUserid() + " and contextid "
                                    + creds.getContextid());
                }
            }
            final String username = creds.getUsername();
            final String password = getRightPassword(config, creds);
            if (null != username) {
                sieveHandler = new SieveHandler(username, authname, password, sieve_server, sieve_port, authEnc);
            } else {
                sieveHandler = new SieveHandler(authname, password, sieve_server, sieve_port, authEnc);
            }
            } else if (MailFilterProperties.CredSrc.MAIL.name.equals(credsrc)) {
                final String authname;
                if (null != storageUser) {
                    authname = storageUser.getMail();
                } else {
                    storageUser = UserStorage.getStorageUser(creds.getUserid(), creds.getContextid());
                    if (null != storageUser) {
                        authname = storageUser.getMail();
                    } else {
                        throw OXMailfilterExceptionCode.INVALID_CREDENTIALS.create("Could not get a valid user object for uid " + creds.getUserid() + " and contextid " + creds.getContextid());
                    }
                }
                final String username = creds.getUsername();
                final String password = getRightPassword(config, creds);
                if (null != username) {
                    sieveHandler = new SieveHandler(username, authname, password, sieve_server, sieve_port, authEnc);
                } else {
                    sieveHandler = new SieveHandler(authname, password, sieve_server, sieve_port, authEnc);
                }
        } else {
            throw OXMailfilterExceptionCode.NO_VALID_CREDSRC.create();
        }
        return sieveHandler;
    }

    private boolean isVacationRule(final Rule newrule) {
        final RuleComment ruleComment = newrule.getRuleComment();
        return (null != ruleComment) && (null != ruleComment.getFlags()) && ruleComment.getFlags().contains("vacation") && ActionCommand.Commands.VACATION.equals(newrule.getIfCommand().getFirstCommand());
    }

    private void changeIncomingVacationRule(final Rule newrule) throws SieveException {
        final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        final String vacationdomains = config.getProperty(MailFilterProperties.Values.VACATION_DOMAINS.property);

        if (null != vacationdomains && 0 != vacationdomains.length()) {
            final IfCommand ifCommand = newrule.getIfCommand();
            final RuleComment ruleComment = newrule.getRuleComment();
            if (null != ruleComment && null != ruleComment.getFlags() && ruleComment.getFlags().contains("vacation") && ActionCommand.Commands.VACATION.equals(ifCommand.getFirstCommand())) {
                final List<Object> argList = new ArrayList<Object>();
                argList.add(Rule2JSON2Rule.createTagArg("is"));
                argList.add(Rule2JSON2Rule.createTagArg("domain"));

                final ArrayList<String> header = new ArrayList<String>();
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
                    final ArrayList<TestCommand> arrayList = new ArrayList<TestCommand>();
                    arrayList.add(newTestCommand);
                    arrayList.add(testcommand);
                    ifCommand.setTestcommand(new TestCommand(Commands.ALLOF, new ArrayList<Object>(), arrayList));
                }
            }
        }
    }

    private void changeOutgoingVacationRule(final ArrayList<Rule> clientrules) throws SieveException {
        final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        final String vacationdomains = config.getProperty(MailFilterProperties.Values.VACATION_DOMAINS.property);

        if (null != vacationdomains && 0 != vacationdomains.length()) {
            for (final Rule rule : clientrules) {
                final IfCommand ifCommand = rule.getIfCommand();
                final RuleComment ruleComment = rule.getRuleComment();
                if (null != ruleComment && null != ruleComment.getFlags() && ruleComment.getFlags().contains("vacation") && ActionCommand.Commands.VACATION.equals(ifCommand.getFirstCommand())) {
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

    private boolean checkOwnVacation(final List<Object> arguments) {
        return null != arguments
            && null != arguments.get(0) && arguments.get(0) instanceof TagArgument && ":is".equals(((TagArgument)arguments.get(0)).getTag())
            && null != arguments.get(1) && arguments.get(1) instanceof TagArgument && ":domain".equals(((TagArgument)arguments.get(1)).getTag())
            && null != arguments.get(2) && arguments.get(2) instanceof List<?> && "From".equals(((List<?>)arguments.get(2)).get(0));
    }

    private JSONArray getActionArray(final Set<String> capabilities) {
        final JSONArray actionarray = new JSONArray();
        for (final ActionCommand.Commands command : ActionCommand.Commands.values()) {
            final List<String> required = command.getRequired();
            if (required.isEmpty()) {
                actionarray.put(command.getJsonname());
            } else {
                for (final String req : required) {
                    if (capabilities.contains(req)) {
                        actionarray.put(command.getJsonname());
                        break;
                    }
                }
            }
        }
        return actionarray;
    }

    private RuleAndPosition getRightRuleForUniqueId(final ArrayList<Rule> clientrules, final Integer uniqueid,
            final String userName, final String contextStr) throws OXException {
        for (int i = 0; i < clientrules.size(); i++) {
            final Rule rule = clientrules.get(i);
            if (uniqueid.intValue() == rule.getUniqueId()) {
                return new RuleAndPosition(rule, i);
            }
        }
        throw OXMailfilterExceptionCode.NO_SUCH_ID.create(uniqueid, userName, contextStr);
    }

    private static OXException handleSieveException(final SieveException e) {
        final String msg = e.getMessage();
        final OXException ret = OXMailfilterExceptionCode.SIEVE_ERROR.create(e, msg);
        if (null != msg) {
            if (msg.startsWith(OXMailfilterExceptionCode.ERR_PREFIX_REJECTED_ADDRESS)) {
                ret.setCategory(Category.CATEGORY_USER_INPUT);
                ret.setLogLevel(LogLevel.ERROR);
            } else if (msg.startsWith(OXMailfilterExceptionCode.ERR_PREFIX_INVALID_ADDRESS)) {
                ret.setCategory(Category.CATEGORY_USER_INPUT);
                ret.setLogLevel(LogLevel.ERROR);
            }
        }
        return ret;
    }

    // private int getIndexOfRightRuleForUniqueId(final ArrayList<Rule>
    // clientrules, Integer uniqueid) throws OXException {
    // for (int i = 0; i < clientrules.size(); i++) {
    // final Rule rule = clientrules.get(i);
    // if (uniqueid == rule.getUniqueId()) {
    // return i;
    // }
    // }
    // throw new OXException(Code.NO_SUCH_ID);
    // }

    /**
     * Fills up the config object
     *
     * @param hashSet A set of sieve capabilities
     * @return
     * @throws JSONException
     */
    private JSONObject getTestAndActionObjects(final Set<String> capabilities) throws JSONException {
        final JSONObject retval = new JSONObject();
        retval.put("tests", getTestArray(capabilities));
        retval.put("actioncommands", getActionArray(capabilities));
        return retval;
    }

    private JSONArray getTestArray(final Set<String> capabilities) throws JSONException {
        final JSONArray testarray = new JSONArray();
        for (final TestCommand.Commands command : TestCommand.Commands.values()) {
            final JSONObject object = new JSONObject();
            if (null == command.getRequired() || capabilities.contains(command.getRequired())) {
                final JSONArray comparison = new JSONArray();
                object.put("test", command.getCommandname());
                final List<String[]> jsonMatchTypes = command.getJsonMatchTypes();
                if (null != jsonMatchTypes) {
                    for (final String[] matchtype : jsonMatchTypes) {
                        final String value = matchtype[0];
                        if ("".equals(value) || capabilities.contains(value)) {
                            comparison.put(matchtype[1]);
                        }
                    }
                }
                object.put("comparison", comparison);
                testarray.put(object);
            }
        }
        return testarray;
    }

    private Integer getUniqueId(final JSONObject json) throws OXException {
        if (json.has("id") && !json.isNull("id")) {
            try {
                return Integer.valueOf(json.getInt("id"));
            } catch (final JSONException e) {
                throw OXMailfilterExceptionCode.ID_MISSING.create();
            }
        }
        throw OXMailfilterExceptionCode.MISSING_PARAMETER.create("id");
    }

    private String fixParsingError(final String script) {
        final String pattern = ":addresses\\s+:";
        return script.replaceAll(pattern, ":addresses \"\" :");
    }

    private void setUidInRule(final Rule newrule, final int uid) {
        final RuleComment name = newrule.getRuleComment();
        if (null != name) {
            name.setUniqueid(uid);
        } else {
            newrule.setRuleComments(new RuleComment(uid));
        }
    }

    /**
     * Used to perform checks to set the right script name when writing
     *
     * @param sieveHandler
     * @param activeScript
     * @param writeback
     * @throws OXSieveHandlerException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private void writeScript(final SieveHandler sieveHandler, final String activeScript, final String writeback)
            throws OXSieveHandlerException, IOException, UnsupportedEncodingException {
        final StringBuilder commandBuilder = new StringBuilder(64);

        if (null != activeScript && activeScript.equals(this.scriptname)) {
            sieveHandler.setScript(activeScript, writeback.getBytes(com.openexchange.java.Charsets.UTF_8), commandBuilder);
            sieveHandler.setScriptStatus(activeScript, true, commandBuilder);
        } else {
            sieveHandler.setScript(this.scriptname, writeback.getBytes(com.openexchange.java.Charsets.UTF_8), commandBuilder);
            sieveHandler.setScriptStatus(this.scriptname, true, commandBuilder);
        }
    }

    private static String getNANString(final NumberFormatException nfe) {
        final String msg = nfe.getMessage();
        if (msg != null && msg.startsWith("For input string: \"")) {
            return msg.substring(19, msg.length() - 1);
        }
        return msg;
    }

    private Category sieveResponse2OXCategory(final SIEVEResponse.Code code) {
        switch (code) {
        case ENCRYPT_NEEDED:
        case QUOTA:
        case REFERRAL:
        case SASL:
        case TRANSITION_NEEDED:
        case TRYLATER:
        case ACTIVE:
        case ALREADYEXISTS:
        case NONEXISTENT:
        case TAG:
            break;
        case WARNINGS:
            return Category.CATEGORY_USER_INPUT;

        default:
            break;
        }
        return Category.CATEGORY_ERROR;
    }

    /**
     * The SIEVE parser is not very expressive when it comes to exceptions.
     * This method analyses an exception message and throws a more detailed
     * one if possible.
     */
    private OXException handleParsingException(final OXSieveHandlerException e, final Credentials credentials) {
        final String message = e.toString();

        if(message.contains("unexpected SUBJECT")) {
            return OXMailfilterExceptionCode.EMPTY_MANDATORY_FIELD.create(e, "ADDRESS (probably)");
        }
        if(message.contains("address ''")) {
            return OXMailfilterExceptionCode.EMPTY_MANDATORY_FIELD.create(e, "ADDRESS");
        }

        if( useSIEVEResponseCodes ) {
            final SIEVEResponse.Code code = e.getSieveResponseCode();
            if( null != code ) {
                return new OXException(code.getDetailnumber(), code.getMessage(), e.getSieveHost(), Integer.valueOf(e
                    .getSieveHostPort()), credentials.getRightUsername(), credentials.getContextString()).addCategory(sieveResponse2OXCategory(code)).setPrefix("MAIL_FILTER");
            } else {
                return OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e, e.getSieveHost(), Integer.valueOf(e
                    .getSieveHostPort()), credentials.getRightUsername(), credentials.getContextString());
            }
        } else {
            return OXMailfilterExceptionCode.SIEVE_COMMUNICATION_ERROR.create(e, e.getSieveHost(), Integer.valueOf(e
                .getSieveHostPort()), credentials.getRightUsername(), credentials.getContextString());
        }
    }

    private static final class Key {

        private final int cid;

        private final int user;

        private final int hash;

        public Key(final int user, final int cid) {
            super();
            this.user = user;
            this.cid = cid;
            final int prime = 31;
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
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
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
