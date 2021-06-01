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

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
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
import com.openexchange.user.User;

/**
 * {@link Separators} - Requests configured default separator.
 * <p>
 * Path in config tree:<br>
 * <code>modules -&gt; mail -&gt; defaultseparator</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Separators implements PreferencesItemService, ConfigTreeEquivalent {

    /** The primary-only flag */
    final boolean primaryOnly;

    /**
     * Default constructor.
     */
    public Separators() {
        super();
        primaryOnly = true;
    }

    @Override
    public String getConfigTreePath() {
        return "modules/mail/separators";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/mail//separators";
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
                } catch (JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }

            private Character getSeparator(final int accountId, final Session session) {
                final MailSessionCache sessionCache = MailSessionCache.getInstance(session);
                Character sep = (Character) sessionCache.getParameter(accountId, MailSessionParameterNames.getParamSeparator());
                if (null == sep) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> ma = null;
                    try {
                        ma = MailAccess.getInstance(session, accountId);
                        ma.connect(false);
                        sep = Character.valueOf(ma.getFolderStorage().getFolder("INBOX").getSeparator());
                        sessionCache.putParameter(accountId, MailSessionParameterNames.getParamSeparator(), sep);
                    } catch (Exception x) {
                        // Ignore
                        final Logger logger = org.slf4j.LoggerFactory.getLogger(Separators.class);
                        logger.debug("Failed determining separator character for mail account {} of user {} in context {}", I(accountId), I(session.getUserId()), I(session.getContextId()), x);
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
