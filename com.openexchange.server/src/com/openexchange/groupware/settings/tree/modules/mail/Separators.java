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

import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link Separators} - Requests configured default separator.
 * <p>
 * Path in config tree:<br>
 * <code>modules -&gt; mail -&gt; defaultseparator</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Separators implements PreferencesItemService {

    /** The primary-only flag */
    final boolean primaryOnly;

    /**
     * Default constructor.
     */
    public Separators() {
        super();
        primaryOnly = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "separators" };
    }

    static final String PROTOCOL_UNIFIED_INBOX = UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX;

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                try {
                    final JSONObject retval = new JSONObject(4);
                    if (primaryOnly || !userConfig.isMultipleMailAccounts()) {
                        final Character sep = getSeparator(MailAccount.DEFAULT_ID, session);
                        if (null != sep) {
                            retval.put(Integer.toString(MailAccount.DEFAULT_ID), sep.toString());
                        }
                    } else {
                        final MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                        if (null == mass) {
                            final Character sep = getSeparator(MailAccount.DEFAULT_ID, session);
                            if (null != sep) {
                                retval.put(Integer.toString(MailAccount.DEFAULT_ID), sep.toString());
                            }
                        } else {
                            final List<MailAccount> accounts = Arrays.asList(mass.getUserMailAccounts(user.getId(), ctx.getContextId()));
                            for (final MailAccount mailAccount : accounts) {
                                if (!PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                                    final Character sep = getSeparator(mailAccount.getId(), session);
                                    if (null != sep) {
                                        retval.put(Integer.toString(mailAccount.getId()), sep.toString());
                                    }
                                }
                            }
                        }
                    }

                    if (retval.isEmpty()) {
                        setting.setSingleValue(null);
                    } else {
                        setting.setSingleValue(retval.toString());
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }

            private Character getSeparator(final int accountId, final Session session) throws OXException {
                final MailSessionCache sessionCache = MailSessionCache.getInstance(session);
                Character sep = (Character) sessionCache.getParameter(accountId, MailSessionParameterNames.getParamSeparator());
                if (null == sep) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> ma = null;
                    try {
                        ma = MailAccess.getInstance(session, accountId);
                        ma.connect(false);
                        sep = Character.valueOf(ma.getFolderStorage().getFolder("INBOX").getSeparator());
                        sessionCache.putParameter(accountId, MailSessionParameterNames.getParamSeparator(), sep);
                    } catch (final Exception x) {
                        // Ignore
                        final Logger logger = org.slf4j.LoggerFactory.getLogger(Separators.class);
                        logger.debug("Failed determining separator character for mail account {} of user {} in context {}", accountId, session.getUserId(), session.getContextId(), x);
                    } finally {
                        if (null != ma) {
                            ma.close(true);
                        }
                    }
                }
                return sep;
            }
        };
    }

}
