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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.parser.generated.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.jsieve.SieveHandler;
import com.openexchange.jsieve.SieveTextFilter;
import com.openexchange.jsieve.SieveHandler.Capabilities;
import com.openexchange.jsieve.SieveTextFilter.RuleListAndNextUid;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.exceptions.OXSieveHandlerException;
import com.openexchange.jsieve.exceptions.OXSieveHandlerInvalidCredentialsException;
import com.openexchange.mailfilter.ajax.Parameter;
import com.openexchange.mailfilter.ajax.SessionWrapper.Credentials;
import com.openexchange.mailfilter.ajax.actions.AbstractRequest.Parameters;
import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException;
import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException.Code;
import com.openexchange.mailfilter.ajax.json.AbstractObject2JSON2Object;
import com.openexchange.mailfilter.ajax.json.Rule2JSON2Rule;
import com.openexchange.mailfilter.internal.MailFilterProperties;
import com.openexchange.mailfilter.services.MailFilterServletServiceRegistry;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public class MailfilterAction extends AbstractAction<Rule, MailfilterRequest> {

    private class RuleAndPosition {
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
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private static final AbstractObject2JSON2Object<Rule> CONVERTER = new Rule2JSON2Rule();
    
    private final Pattern p = Pattern.compile("^(?:([^:]*)://)?([^:]*)(.*)$");
    
    private final String scriptname;
    
    /**
     * Default constructor.
     */
    public MailfilterAction() {
        super();
        final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        scriptname = config.getProperty(MailFilterProperties.Values.SCRIPT_NAME.property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject actionConfig(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveHandler sieveHandler = connectRight(credentials);
        // First fetch configuration:
        JSONObject tests = null;
        try {
            sieveHandler.initializeConnection();
            final Capabilities capabilities = sieveHandler.getCapabilities();
            final ArrayList<String> sieve = capabilities.getSieve();
            tests = getTestAndActionObjects(sieve);
        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } catch (final JSONException e) {
            throw new OXMailfilterException(Code.JSON_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
        return tests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void actionDelete(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final String activeScript = sieveHandler.getActiveScript();
            final byte[] script = sieveHandler.getScript(activeScript);
            final RuleListAndNextUid rulesandid = sieveTextFilter.readScriptFromString(new String(script, "UTF-8"));
            final String body = request.getBody();
            final JSONObject json = new JSONObject(body);
            
            final ArrayList<Rule> rules = rulesandid.getRulelist();
            final RuleAndPosition deletedrule = getRightRuleForUniqueId(rules, getUniqueId(json), getUserPrefix(credentials));
            rules.remove(deletedrule.getPosition());
            final String writeback = sieveTextFilter.writeback(rules);
            writeScript(sieveHandler, activeScript, writeback);
        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } catch (final ParseException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final SieveException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONArray actionList(final MailfilterRequest request) throws AbstractOXException {
        final Parameters parameters = request.getParameters();
        final Credentials credentials = request.getCredentials();
        final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final byte[] script = sieveHandler.getScript(sieveHandler.getActiveScript());
            if (log.isDebugEnabled()) {
                log.debug("The following sieve script will be parsed:\n" + new String(script, "UTF-8"));
            }
            final RuleListAndNextUid readScriptFromString = sieveTextFilter.readScriptFromString(new String(script, "UTF-8"));
            final ArrayList<Rule> clientrules = getClientRules(readScriptFromString.getRulelist(), parameters.getParameter(Parameter.FLAG));
            return CONVERTER.write(clientrules.toArray(new Rule[clientrules.size()]));
        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials), e.getMessage());
        } catch (final ParseException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final SieveException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int actionNew(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final String activeScript = sieveHandler.getActiveScript();
            final byte[] script = sieveHandler.getScript(activeScript);
            final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(new String(script, "UTF-8"));
            
            final ArrayList<Rule> clientrules = getClientRules(rules.getRulelist(), null);
            
            final String body = request.getBody();
            final JSONObject json = new JSONObject(body);
            final Rule newrule = CONVERTER.parse(json);
            // Now find the right position inside the array
            int position = newrule.getPosition();
            if (position >= clientrules.size()) {
                throw new OXMailfilterException(Code.POSITION_TOO_BIG);
            }
            final int nextuid = rules.getNextuid();
            setUidInRule(newrule, nextuid);
            if (-1 != position) {
                clientrules.add(position, newrule);
            } else {
                clientrules.add(newrule);
                position = clientrules.size() - 1;
            }
            final String writeback = sieveTextFilter.writeback(clientrules);
            if (log.isDebugEnabled()) {
                log.debug("The following sieve script will be written:\n" + writeback);
            }
            writeScript(sieveHandler, activeScript, writeback);

            return nextuid;
        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } catch (final ParseException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final SieveException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_READ_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
    }

    @Override
	protected void actionReorder(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final String activeScript = sieveHandler.getActiveScript();
            final byte[] script = sieveHandler.getScript(activeScript);
            final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(new String(script, "UTF-8"));
            
            final ArrayList<Rule> clientrules = getClientRules(rules.getRulelist(), null);
            
            final String body = request.getBody();
            final JSONArray json = new JSONArray(body);
            
            for (int i = 0; i < json.length(); i++) {
                final int uniqueid = json.getInt(i);
                final RuleAndPosition rightRule = getRightRuleForUniqueId(clientrules, uniqueid, getUserPrefix(credentials));
                final int position = rightRule.getPosition();
                clientrules.remove(position);
                clientrules.add(i, rightRule.getRule());
            }
            
            final String writeback = sieveTextFilter.writeback(clientrules);
            writeScript(sieveHandler, activeScript, writeback);

        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } catch (final ParseException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final SieveException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void actionUpdate(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveTextFilter sieveTextFilter = new SieveTextFilter(credentials);
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final String activeScript = sieveHandler.getActiveScript();
            final byte[] script = sieveHandler.getScript(activeScript);
            final RuleListAndNextUid rules = sieveTextFilter.readScriptFromString(new String(script, "UTF-8"));
            
            final ArrayList<Rule> clientrules = getClientRules(rules.getRulelist(), null);
            
            final String body = request.getBody();
            final JSONObject json = new JSONObject(body);
            final Integer uniqueid = getUniqueId(json);
            
            if (null != uniqueid) {
                // First get the right rule which should be modified...
                final RuleAndPosition rightRule = getRightRuleForUniqueId(clientrules, uniqueid, getUserPrefix(credentials));
                CONVERTER.parse(rightRule.getRule(), json);
            } else {
                throw new OXMailfilterException(Code.ID_MISSING);
            }
            
            final String writeback = sieveTextFilter.writeback(clientrules);
            if (log.isDebugEnabled()) {
                log.debug("The following sieve script will be written:\n" + writeback);
            }
            writeScript(sieveHandler, activeScript, writeback);

        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } catch (final ParseException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final SieveException e) {
            throw new OXMailfilterException(Code.SIEVE_ERROR, getUserPrefix(credentials) + e.getMessage());
        } catch (final JSONException e) {
            throw new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
    }
    
    @Override
    protected void actionDeleteScript(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final String activeScript = sieveHandler.getActiveScript();
            
            writeScript(sieveHandler, activeScript, "");

        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
                }
            }
        }
    }
    
    @Override
    protected String actionGetScript(final MailfilterRequest request) throws AbstractOXException {
        final Credentials credentials = request.getCredentials();
        final SieveHandler sieveHandler = connectRight(credentials);
        try {
            sieveHandler.initializeConnection();
            final String activeScript = sieveHandler.getActiveScript();
            final byte[] script = sieveHandler.getScript(activeScript);
            
            if (null != script) {
                return new String(script, "UTF-8");
            } else {
                return "";
            }
        } catch (final UnsupportedEncodingException e) {
            throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final IOException e) {
            throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerException e) {
            throw new OXMailfilterException(Code.SIEVE_COMMUNICATION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
        } catch (final OXSieveHandlerInvalidCredentialsException e) {
            throw new OXMailfilterException(Code.INVALID_CREDENTIALS, getUserPrefix(credentials) + e.getMessage());
        } finally {
            if (null != sieveHandler) {
                try {
                    sieveHandler.close();
                } catch (final UnsupportedEncodingException e) {
                    throw new OXMailfilterException(Code.UNSUPPORTED_ENCODING, e, getUserPrefix(credentials) + e.getMessage());
                } catch (final IOException e) {
                    throw new OXMailfilterException(Code.IO_CONNECTION_ERROR, e, getUserPrefix(credentials) + e.getMessage());
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

    private SieveHandler connectRight(final Credentials creds) throws OXMailfilterException {
        final SieveHandler sieveHandler;
        final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        
        final String logintype = config.getProperty(MailFilterProperties.Values.SIEVE_LOGIN_TYPE.property);
        final int sieve_port;
        final String sieve_server;
        User storageUser = null;
        if (MailFilterProperties.LoginTypes.GLOBAL.name.equals(logintype)) {
            sieve_server = config.getProperty(MailFilterProperties.Values.SIEVE_SERVER.property);
            if (null == sieve_server) {
                throw new OXMailfilterException(Code.PROPERTY_ERROR, MailFilterProperties.Values.SIEVE_SERVER.property);
            }
            try {
                sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
            } catch (final RuntimeException e) {
                throw new OXMailfilterException(Code.PROPERTY_ERROR, e, MailFilterProperties.Values.SIEVE_PORT.property);
            }
        } else if (MailFilterProperties.LoginTypes.USER.name.equals(logintype)) {
            storageUser = UserStorage.getStorageUser(creds.getUserid(), creds.getContextid());
            final String mailServerURL = storageUser.getImapServer();
            
            final Matcher m = p.matcher(mailServerURL);
            if (m.matches()) {
                sieve_server = m.group(2);
            } else {
                throw new OXMailfilterException(Code.NO_SERVERNAME_IN_SERVERURL);
            }
            try {
                sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
            } catch (final RuntimeException e) {
                throw new OXMailfilterException(Code.PROPERTY_ERROR, e, MailFilterProperties.Values.SIEVE_PORT.property);
            }
        } else {
            throw new OXMailfilterException(Code.NO_VALID_LOGIN_TYPE);
        }
        final String credsrc = config.getProperty(MailFilterProperties.Values.SIEVE_CREDSRC.property);
        if (MailFilterProperties.CredSrc.SESSION.name.equals(credsrc)) {
            final String username = creds.getUsername();
            final String authname = creds.getAuthname();
            final String password = creds.getPassword();
            if (null != username) {
                sieveHandler = new SieveHandler(username, authname, password, sieve_server, sieve_port);
            } else {
                sieveHandler = new SieveHandler(authname, password, sieve_server, sieve_port);
            }
        } else if (MailFilterProperties.CredSrc.IMAP_LOGIN.name.equals(credsrc)) {
            final String authname;
            if (null != storageUser) {
                authname = storageUser.getImapLogin();
            } else {
                storageUser = UserStorage.getStorageUser(creds.getUserid(), creds.getContextid());
                authname = storageUser.getImapLogin();
            }
            final String username = creds.getUsername();
            final String password = creds.getPassword();
            if (null != username) {
                sieveHandler = new SieveHandler(username, authname, password, sieve_server, sieve_port);
            } else {
                sieveHandler = new SieveHandler(authname, password, sieve_server, sieve_port);
            }
        } else {
            throw new OXMailfilterException(Code.NO_VALID_CREDSRC);
        }
        return sieveHandler;
    }

    private JSONArray getActionArray(final ArrayList<String> sieve) {
        final JSONArray actionarray = new JSONArray();
        for (final ActionCommand.Commands command : ActionCommand.Commands.values()) {
            if (null == command.getRequired() || sieve.contains(command.getRequired())) {
                actionarray.put(command.getJsonname());
            }
        }
        return actionarray;
    }

    /**
     * Here we have to strip off the require line because this line should not be edited by the client
     * 
     * @param rules
     * @return
     */
    private ArrayList<Rule> getClientRules(final ArrayList<Rule> rules, final String flag) {
        final ArrayList<Rule> retval = new ArrayList<Rule>();
        // The flag is checked here because if no flag is given we can omit some checks which increases performance
        if (null != flag) {
            for (final Rule rule : rules) {
                final RuleComment ruleComment = rule.getRuleComment();
                if (null == rule.getRequireCommand() && null != ruleComment && null != ruleComment.getFlags() && ruleComment.getFlags().contains(flag)) {
                    retval.add(rule);
                }
            }
        } else {
            for (final Rule rule : rules) {
                if (null == rule.getRequireCommand()) {
                    retval.add(rule);
                }
            }
        }
        return retval;
    }

    private RuleAndPosition getRightRuleForUniqueId(final ArrayList<Rule> clientrules, final Integer uniqueid, final String prefix) throws OXMailfilterException {
        for (int i = 0; i < clientrules.size(); i++) {
            final Rule rule = clientrules.get(i);
            if (uniqueid == rule.getUniqueId()) {
                return new RuleAndPosition(rule, i);
            }
        }
        throw new OXMailfilterException(Code.NO_SUCH_ID, prefix, String.valueOf(uniqueid));
    }

//    private int getIndexOfRightRuleForUniqueId(final ArrayList<Rule> clientrules, Integer uniqueid) throws OXMailfilterException {
//        for (int i = 0; i < clientrules.size(); i++) {
//            final Rule rule = clientrules.get(i);
//            if (uniqueid == rule.getUniqueId()) {
//                return i;
//            }
//        }
//        throw new OXMailfilterException(Code.NO_SUCH_ID);
//    }

    /**
     * Fills up the config object
     * 
     * @param sieve A list of sieve capabilities
     * @return
     * @throws JSONException
     */
    private JSONObject getTestAndActionObjects(final ArrayList<String> sieve) throws JSONException {
        final JSONObject retval = new JSONObject();
        retval.put("tests", getTestArray(sieve));
        retval.put("actioncommands", getActionArray(sieve));
        return retval;
    }

    private JSONArray getTestArray(final ArrayList<String> sieve) throws JSONException {
        final JSONArray testarray = new JSONArray();
        for (final TestCommand.Commands command : TestCommand.Commands.values()) {
            final JSONObject object = new JSONObject();
            if (null == command.getRequired() || sieve.contains(command.getRequired())) {
                final JSONArray comparison = new JSONArray();
                object.put("test", command.getCommandname());
                final Hashtable<String, String> matchtypes = command.getMatchtypes();
                if (null != matchtypes) {
                    for (final Map.Entry<String, String> matchtype : matchtypes.entrySet()) {
                        final String value = matchtype.getValue();
                        if ("".equals(value) || sieve.contains(value)) {
                            comparison.put(matchtype.getKey().substring(1));
                        }
                    }
                }
                object.put("comparison", comparison);
                testarray.put(object);
            }
        }
        return testarray;
    }

    private Integer getUniqueId(final JSONObject json) throws OXMailfilterException {
        Integer uniqueid = null;
        if (json.has("id")) {
            try {
                uniqueid = json.getInt("id");
            } catch (final JSONException e) {
                throw new OXMailfilterException(Code.ID_MISSING);
            }
        }
        return uniqueid;
    }

    /**
     * If you don't want the username to be logged then let this method return an empty string
     * @param credentials
     * @return
     */
    private String getUserPrefix(final Credentials credentials) {
        return "Error for user " + credentials.getRightUsername() + ": ";
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
    private void writeScript(final SieveHandler sieveHandler, final String activeScript, final String writeback) throws OXSieveHandlerException, IOException, UnsupportedEncodingException {
        if (null != activeScript && activeScript.equals(this.scriptname)) {
            sieveHandler.setScript(activeScript, writeback.getBytes("UTF-8"));
            sieveHandler.setScriptStatus(activeScript, true);
        } else {
            sieveHandler.setScript(this.scriptname, writeback.getBytes("UTF-8"));
            sieveHandler.setScriptStatus(this.scriptname, true);
        }
    }

}
