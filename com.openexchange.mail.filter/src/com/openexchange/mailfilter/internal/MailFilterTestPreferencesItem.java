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
//package com.openexchange.mailfilter.internal;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.Map;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.openexchange.config.ConfigurationService;
//import com.openexchange.groupware.contexts.Context;
//import com.openexchange.groupware.ldap.User;
//import com.openexchange.groupware.settings.IValueHandler;
//import com.openexchange.groupware.settings.ReadOnlyValue;
//import com.openexchange.groupware.settings.Setting;
//import com.openexchange.exception.OXException;
//import com.openexchange.groupware.userconfiguration.UserConfiguration;
//import com.openexchange.jsieve.SieveHandler;
//import com.openexchange.jsieve.commands.TestCommand;
//import com.openexchange.exception.OXException;;
//import com.openexchange.exception.OXException;.Code;
//import com.openexchange.mailfilter.services.MailFilterServletServiceRegistry;
//import com.openexchange.session.Session;
//
//public class MailFilterTestPreferencesItem extends MailFilterPreferencesItem {
//
//    /**
//     *
//     */
//    public MailFilterTestPreferencesItem() {
//        super();
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String[] getPath() {
//        final String[] superpath = super.getPath();
//        final String[] path = new String[superpath.length + 1];
//        System.arraycopy(superpath, 0, path, 0, superpath.length);
//        path[superpath.length] = "tests";
//        return path;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public IValueHandler getSharedValue() {
//        return new ReadOnlyValue() {
//
//            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
//                SieveHandler shandler = null;
//                try {
//                    final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
//                    final int sieve_port;
//                    final String sieve_server = config.getProperty(MailFilterProperties.Values.SIEVE_SERVER.property);
//                    if (null == sieve_server) {
//                        throw new OXException(Code.PROPERTY_ERROR, MailFilterProperties.Values.SIEVE_SERVER.property);
//                    }
//                    try {
//                        sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
//                    } catch (final RuntimeException e) {
//                        throw new OXException(Code.PROPERTY_ERROR, e, MailFilterProperties.Values.SIEVE_PORT.property);
//                    }
//                    shandler = new SieveHandler(null, null, sieve_server, sieve_port);
//                    shandler.getServerWelcome();
//                    final ArrayList<String> sieve = shandler.getCapabilities().getSieve();
//                    for (final TestCommand.Commands command : TestCommand.Commands.values()) {
//                        final JSONObject object = new JSONObject();
//                        if (null == command.getRequired() || sieve.contains(command.getRequired())) {
//                            final JSONArray comparison = new JSONArray();
//                            object.put("test", command.getCommandname());
//                            final Hashtable<String, String> matchtypes = command.getMatchtypes();
//                            if (null != matchtypes) {
//                                for (final Map.Entry<String, String> matchtype : matchtypes.entrySet()) {
//                                    final String value = matchtype.getValue();
//                                    if ("".equals(value) || sieve.contains(value)) {
//                                        comparison.put(matchtype.getKey());
//                                    }
//                                }
//                            }
//                            object.put("comparison", comparison);
//                            setting.addMultiValue(object);
//                        }
//                    }
//                } catch (final OXException e) {
//                    throw new OXException(e);
//                } catch (final UnknownHostException e) {
//                    throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                } catch (final IOException e) {
//                    throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                } catch (final JSONException e) {
//                    throw new OXException(com.openexchange.groupware.settings.OXException.Code.JSON_WRITE_ERROR, e);
//                } finally {
//                    if (null != shandler) {
//                        try {
//                            shandler.close();
//                        } catch (final UnsupportedEncodingException e) {
//                            throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                        } catch (final IOException e) {
//                            throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                        }
//                    }
//                }
//            }
//
//            public boolean isAvailable(UserConfiguration userConfig) {
//                return true;
//            }
//
//        };
//    }
//
//}
