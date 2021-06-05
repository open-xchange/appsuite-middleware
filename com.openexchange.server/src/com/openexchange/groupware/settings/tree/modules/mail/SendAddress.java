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

package com.openexchange.groupware.settings.tree.modules.mail;

import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MsisdnUtility;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class SendAddress implements PreferencesItemService {

    /**
     * Default constructor.
     */
    public SendAddress() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "sendaddress" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new IValueHandler() {

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(user.getId(), ctx);
                if (null != settings) {
                    setting.setSingleValue(settings.getSendAddr());
                }
            }

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            public boolean isWritable() {
                return true;
            }

            @Override
            public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                final UserSettingMailStorage storage = UserSettingMailStorage.getInstance();
                UserSettingMail settings = storage.getUserSettingMail(user.getId(), ctx);
                if (null == settings) {
                    return;
                }
                final String newAlias = setting.getSingleValue().toString();
                if (settings.getSendAddr().equals(newAlias)) {
                    return;
                }
                try {
                    // Add mail aliases
                    final Set<InternetAddress> allAliases;
                    {
                        final String[] aliases = user.getAliases();
                        if (aliases == null) {
                            allAliases = new HashSet<InternetAddress>(4);
                        } else {
                            allAliases = new HashSet<InternetAddress>(aliases.length + 3);
                            for (String alias: aliases) {
                                allAliases.add(new QuotedInternetAddress(alias, false));
                            }
                        }
                    }

                    // Add MSISDN addresses if supported
                    if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
                        MsisdnUtility.addMsisdnAddress(allAliases, session);
                    }

                    // Add primary address
                    allAliases.add(new QuotedInternetAddress(user.getMail(), false));

                    // Add default sender address
                    {
                        allAliases.add(new QuotedInternetAddress(settings.getSendAddr(), false));
                    }

                    // Determine the new mail address to set as default sender address
                    final InternetAddress aliasToCheck;
                    {
                        final int pos = newAlias.indexOf('/');
                        String checkAlias = newAlias;
                        if (pos > 0) {
                            checkAlias = checkAlias.substring(0, pos);
                        }
                        aliasToCheck = new QuotedInternetAddress(checkAlias, false);
                    }

                    // Check if covered by valid mail addresses
                    boolean found = allAliases.contains(aliasToCheck);
                    if (!found) {
                        throw SettingExceptionCodes.INVALID_VALUE.create(newAlias, setting.getName());
                    }

                    settings = storage.getUserSettingMail(user.getId(), ctx);
                    if (null != settings) {
                        settings.setSendAddr(newAlias);
                        storage.saveUserSettingMail(settings, user.getId(), ctx);
                    }
                } catch (AddressException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            }

            @Override
            public int getId() {
                return -1;
            }
        };
    }
}
