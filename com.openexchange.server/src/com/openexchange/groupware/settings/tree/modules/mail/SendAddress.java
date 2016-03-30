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

package com.openexchange.groupware.settings.tree.modules.mail;

import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
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
                } catch (final AddressException e) {
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
