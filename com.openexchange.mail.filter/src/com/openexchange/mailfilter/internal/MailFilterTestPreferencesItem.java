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
//import com.openexchange.exception.OXException;
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
//                    } catch (RuntimeException e) {
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
//                } catch (OXException e) {
//                    throw new OXException(e);
//                } catch (UnknownHostException e) {
//                    throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                } catch (IOException e) {
//                    throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                } catch (JSONException e) {
//                    throw new OXException(com.openexchange.groupware.settings.OXException.Code.JSON_WRITE_ERROR, e);
//                } finally {
//                    if (null != shandler) {
//                        try {
//                            shandler.close();
//                        } catch (UnsupportedEncodingException e) {
//                            throw new OXException(com.openexchange.groupware.settings.OXException.Code.INIT, e);
//                        } catch (IOException e) {
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
