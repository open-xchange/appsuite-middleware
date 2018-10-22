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

package com.openexchange.groupware.settings.tree.modules.mail.folder;

import javax.mail.Store;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.AbstractWarningAwareReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Reference;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailStoreAware;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link AbstractStandardFolderItemValue}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractStandardFolderItemValue extends AbstractWarningAwareReadOnlyValue {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractStandardFolderItemValue.class);

    private static volatile Boolean failOnError;

    private static boolean failOnError() {
        Boolean tmp = failOnError;
        if (null == tmp) {
            synchronized (AbstractStandardFolderItemValue.class) {
                tmp = failOnError;
                if (null == tmp) {
                    boolean defaultValue = false;
                    ConfigurationService service = ServerServiceRegistry.getServize(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.settings.mail.failOnError", defaultValue));
                    failOnError = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                failOnError = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.settings.mail.failOnError");
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link AbstractStandardFolderItemValue}.
     */
    protected AbstractStandardFolderItemValue() {
        super();
    }

    /**
     * Checks if associated folder is available
     *
     * @param userConfig The user configuration to check
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    @Override
    public boolean isAvailable(UserConfiguration userConfig) {
        return userConfig.hasWebMail();
    }

    private int trySetReadTimeout(int readTimeout, Store messageStore) {
        try {
            return messageStore.setAndGetReadTimeout(readTimeout);
        } catch (Exception e) {
            // Ignore
        }
        return -1;
    }

    @Override
    public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
        Reference<MailAccessAndStorage> mailAccessReference = new Reference<>();
        try {
            // Get setting
            getValue(setting, mailAccessReference, session);

            // Check for possible warnings
            MailAccessAndStorage accessAndStorage = mailAccessReference.getValue();
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null == accessAndStorage ? null : accessAndStorage.primaryMailAccess;
            if (null != mailAccess) {
                addWarnings(mailAccess.getWarnings());
            }
        } catch (OXException e) {
            if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.equals(e) || MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
                // Admin/user has no mail access
                setting.setSingleValue(null);
            } else if (MailExceptionCode.containsSocketError(e)) {
                // A socket error we cannot recover from
                LOGGER.warn("Could not connect to mail system due to a socket error", e);
                setting.setSingleValue(null);
            } else {
                if (failOnError()) {
                    throw e;
                }
                LOGGER.warn("Could not determine mail setting", e);
                setting.setSingleValue(null);
            }
        } catch (RuntimeException rte) {
            if (failOnError()) {
                throw rte;
            }
            LOGGER.warn("Could not determine mail setting", rte);
            setting.setSingleValue(null);
        } finally {
            MailAccessAndStorage accessAndStorage = mailAccessReference.getValue();
            if (null != accessAndStorage) {
                // Restore previous read timeout
                Store messageStore = accessAndStorage.messageStore;
                int prevReadTimeout = accessAndStorage.prevReadTimeout;
                if (null != messageStore) {
                    trySetReadTimeout(prevReadTimeout, messageStore);
                }

                // Close mail access
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = accessAndStorage.primaryMailAccess;
                if (null != mailAccess) {
                    try {
                        mailAccess.close(true);
                    } catch (Exception e) {
                        LOGGER.error("Failed to close MailAccess instance", e);
                    }
                }
            }
        }
    }

    /**
     * Checks if a connected instance of <code>MailAccess</code> is needed.
     *
     * @param session The session
     * @param mailAccessReference The mail access reference
     * @return The connected mail access
     * @throws OXException If connect attempt fails
     */
    protected MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getConnectedMailAccess(Session session, Reference<MailAccessAndStorage> mailAccessReference) throws OXException {
        MailAccessAndStorage accessAndStorage = mailAccessReference.getValue();
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null == accessAndStorage ? null : accessAndStorage.primaryMailAccess;
        if (null != mailAccess) {
            return mailAccess;
        }

        Store messageStore = null;
        int prevReadTimeout = -1;
        try {
            mailAccess = MailAccess.getInstance(session, MailAccount.DEFAULT_ID);
            mailAccess.connect();

            // Lower read timeout (if possible)
            IMailStoreAware storeAware = mailAccess.supports(IMailStoreAware.class);
            if (null != storeAware && storeAware.isStoreSupported()) {
                Store store = storeAware.getStore();
                if (store.isSetAndGetReadTimeoutSupported() && (prevReadTimeout = trySetReadTimeout(3500, store)) >= 0) {
                    messageStore = store;
                }
            }

            mailAccessReference.setValue(new MailAccessAndStorage(mailAccess, messageStore, prevReadTimeout));

            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> toReturn = mailAccess;
            mailAccess = null;
            messageStore = null;
            return toReturn;
        } finally {
            // Restore previous read timeout
            if (null != messageStore) {
                trySetReadTimeout(prevReadTimeout, messageStore);
            }

            // Close mail access
            if (null != mailAccess) {
                try {
                    mailAccess.close(true);
                } catch (Exception e) {
                    LOGGER.error("Failed to close MailAccess instance", e);
                }
            }
        }
    }

    /**
     * Determines the value and applies it to given setting.
     *
     * @param setting The setting to apply to
     * @param mailAccessReference The mail access reference
     * @param session The session
     * @throws OXException If operation fails
     */
    protected abstract void getValue(Setting setting, Reference<MailAccessAndStorage> mailAccessReference, Session session) throws OXException;

    // ------------------------------------------------------------------------------------------------------------------------------------

    static class MailAccessAndStorage {

        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> primaryMailAccess;
        final Store messageStore;
        final int prevReadTimeout;

        MailAccessAndStorage(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> primaryMailAccess, Store messageStore, int prevReadTimeout) {
            super();
            this.primaryMailAccess = primaryMailAccess;
            this.messageStore = messageStore;
            this.prevReadTimeout = prevReadTimeout;
        }

    }
}
