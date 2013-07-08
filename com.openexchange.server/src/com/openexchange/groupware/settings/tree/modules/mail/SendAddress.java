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

package com.openexchange.groupware.settings.tree.modules.mail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                final String newAlias = setting.getSingleValue().toString();
                boolean found = false;
                final String[] aliases = user.getAliases();
                final List<String> allAliases = new ArrayList<String>(aliases.length + 3);
                if (aliases != null) {
                    for(String alias: aliases) {
                        allAliases.add(alias);
                    }
                }
                final boolean supportMsisdnAddresses = MailProperties.getInstance().isSupportMsisdnAddresses();
                if (supportMsisdnAddresses) {
                    Set<InternetAddress> msisdnAddresses = new HashSet<InternetAddress>();
                    MsisdnUtility.addMsisdnAddress(msisdnAddresses, session);
                    for (InternetAddress internetAddress : msisdnAddresses) {
                        allAliases.add(internetAddress.getAddress());
                    }
                }
                
                final int pos = newAlias.indexOf('/');
                String checkAlias = newAlias;
                if (pos > 0) {
                    checkAlias = checkAlias.substring(0, pos);
                }
                
                for (String alias: allAliases) {
                   
                    found = alias.equals(checkAlias);
                }
        
                if (user.getMail().equals(newAlias)) {
                    found = true;
                }
                if (!found) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(newAlias, setting.getName());
                }
                final UserSettingMailStorage storage = UserSettingMailStorage.getInstance();
                final UserSettingMail settings = storage.getUserSettingMail(user.getId(), ctx);
                if (null != settings) {
                    settings.setSendAddr(newAlias);
                    try {
                        storage.saveUserSettingMail(settings, user.getId(), ctx);
                    } catch (final OXException e) {
                        throw e;
                    }
                }
            }

            @Override
            public int getId() {
                return -1;
            }
        };
    }
}
