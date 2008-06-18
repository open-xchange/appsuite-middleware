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
//import com.openexchange.groupware.settings.SettingException;
//import com.openexchange.groupware.userconfiguration.UserConfiguration;
//import com.openexchange.jsieve.SieveHandler;
//import com.openexchange.jsieve.commands.TestCommand;
//import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException;
//import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException.Code;
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
//            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws SettingException {
//                SieveHandler shandler = null;
//                try {
//                    final ConfigurationService config = MailFilterServletServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
//                    final int sieve_port;
//                    final String sieve_server = config.getProperty(MailFilterProperties.Values.SIEVE_SERVER.property);
//                    if (null == sieve_server) {
//                        throw new OXMailfilterException(Code.PROPERTY_ERROR, MailFilterProperties.Values.SIEVE_SERVER.property);
//                    }
//                    try {
//                        sieve_port = Integer.parseInt(config.getProperty(MailFilterProperties.Values.SIEVE_PORT.property));
//                    } catch (final RuntimeException e) {
//                        throw new OXMailfilterException(Code.PROPERTY_ERROR, e, MailFilterProperties.Values.SIEVE_PORT.property);
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
//                } catch (final OXMailfilterException e) {
//                    throw new SettingException(e);
//                } catch (final UnknownHostException e) {
//                    throw new SettingException(com.openexchange.groupware.settings.SettingException.Code.INIT, e);
//                } catch (final IOException e) {
//                    throw new SettingException(com.openexchange.groupware.settings.SettingException.Code.INIT, e);
//                } catch (final JSONException e) {
//                    throw new SettingException(com.openexchange.groupware.settings.SettingException.Code.JSON_WRITE_ERROR, e);
//                } finally {
//                    if (null != shandler) {
//                        try {
//                            shandler.close();
//                        } catch (final UnsupportedEncodingException e) {
//                            throw new SettingException(com.openexchange.groupware.settings.SettingException.Code.INIT, e);
//                        } catch (final IOException e) {
//                            throw new SettingException(com.openexchange.groupware.settings.SettingException.Code.INIT, e);
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
