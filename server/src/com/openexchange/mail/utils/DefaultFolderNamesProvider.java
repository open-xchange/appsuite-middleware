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

package com.openexchange.mail.utils;

import static com.openexchange.mail.usersetting.UserSettingMail.STD_CONFIRMED_HAM;
import static com.openexchange.mail.usersetting.UserSettingMail.STD_CONFIRMED_SPAM;
import static com.openexchange.mail.usersetting.UserSettingMail.STD_DRAFTS;
import static com.openexchange.mail.usersetting.UserSettingMail.STD_SENT;
import static com.openexchange.mail.usersetting.UserSettingMail.STD_SPAM;
import static com.openexchange.mail.usersetting.UserSettingMail.STD_TRASH;
import static com.openexchange.mail.utils.StorageUtility.INDEX_CONFIRMED_HAM;
import static com.openexchange.mail.utils.StorageUtility.INDEX_CONFIRMED_SPAM;
import static com.openexchange.mail.utils.StorageUtility.INDEX_DRAFTS;
import static com.openexchange.mail.utils.StorageUtility.INDEX_SENT;
import static com.openexchange.mail.utils.StorageUtility.INDEX_SPAM;
import static com.openexchange.mail.utils.StorageUtility.INDEX_TRASH;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link DefaultFolderNamesProvider} - Provides the default folder names for a certain mail account.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultFolderNamesProvider {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(DefaultFolderNamesProvider.class);

    private static final String SWITCH_DEFAULT_FOLDER = "Switching to default value %s";

    /*
     * Member
     */

    private final FallbackProvider fallbackProvider;

    /**
     * Initializes a new {@link DefaultFolderNamesProvider}.
     * 
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @throws MailException If initialization fails
     */
    public DefaultFolderNamesProvider(final int accountId, final int user, final int cid) throws MailException {
        super();
        if (MailAccount.DEFAULT_ID == accountId) {
            fallbackProvider = DEFAULT_PROVIDER;
        } else {
            try {
                final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                    MailAccountStorageService.class,
                    true);
                fallbackProvider = new DefaultAccountProvider(storageService.getDefaultMailAccount(user, cid));
            } catch (final ServiceException e) {
                throw new MailException(e);
            } catch (final MailAccountException e) {
                throw new MailException(e);
            }
        }
    }

    /**
     * Determines the default folder names (<b>not</b> fullnames). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     * 
     * @param mailAccount The mail account providing the names
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder names as an array of {@link String}
     */
    public String[] getDefaultFolderNames(final MailAccount mailAccount, final boolean isSpamEnabled) {
        return getDefaultFolderNames(
            mailAccount.getTrash(),
            mailAccount.getSent(),
            mailAccount.getDrafts(),
            mailAccount.getSpam(),
            mailAccount.getConfirmedSpam(),
            mailAccount.getConfirmedHam(),
            isSpamEnabled);
    }

    /**
     * Determines the default folder names (<b>not</b> fullnames). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     * 
     * @param trash The trash name
     * @param sent The sent name
     * @param drafts The drafts name
     * @param spam The spam name
     * @param confirmedSpam The confirmed-spam name
     * @param confirmedHam The confirmed-ham name
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder names as an array of {@link String}
     */
    public String[] getDefaultFolderNames(final String trash, final String sent, final String drafts, final String spam, final String confirmedSpam, final String confirmedHam, final boolean isSpamEnabled) {
        final String[] names = new String[isSpamEnabled ? 6 : 4];
        if ((drafts == null) || (drafts.length() == 0)) {
            if (LOG.isWarnEnabled()) {
                final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME, STD_DRAFTS);
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, STD_DRAFTS), e);
            }
            names[INDEX_DRAFTS] = fallbackProvider.getDrafts();
        } else {
            names[INDEX_DRAFTS] = drafts;
        }
        if ((sent == null) || (sent.length() == 0)) {
            if (LOG.isWarnEnabled()) {
                final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME, STD_SENT);
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, STD_SENT), e);
            }
            names[INDEX_SENT] = fallbackProvider.getSent();
        } else {
            names[INDEX_SENT] = sent;
        }
        if ((spam == null) || (spam.length() == 0)) {
            if (LOG.isWarnEnabled()) {
                final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME, STD_SPAM);
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, STD_SPAM), e);
            }
            names[INDEX_SPAM] = fallbackProvider.getSpam();
        } else {
            names[INDEX_SPAM] = spam;
        }
        if ((trash == null) || (trash.length() == 0)) {
            if (LOG.isWarnEnabled()) {
                final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME, STD_TRASH);
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, STD_TRASH), e);
            }
            names[INDEX_TRASH] = fallbackProvider.getTrash();
        } else {
            names[INDEX_TRASH] = trash;
        }
        if (isSpamEnabled) {
            if ((confirmedSpam == null) || (confirmedSpam.length() == 0)) {
                if (LOG.isWarnEnabled()) {
                    final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME, STD_CONFIRMED_SPAM);
                    LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, STD_CONFIRMED_SPAM), e);
                }
                names[INDEX_CONFIRMED_SPAM] = fallbackProvider.getConfirmedSpam();
            } else {
                names[INDEX_CONFIRMED_SPAM] = confirmedSpam;
            }
            if ((confirmedHam == null) || (confirmedHam.length() == 0)) {
                if (LOG.isWarnEnabled()) {
                    final MailException e = new MailException(MailException.Code.MISSING_DEFAULT_FOLDER_NAME, STD_CONFIRMED_HAM);
                    LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, STD_CONFIRMED_HAM), e);
                }
                names[INDEX_CONFIRMED_HAM] = fallbackProvider.getConfirmeHam();
            } else {
                names[INDEX_CONFIRMED_HAM] = confirmedHam;
            }
        }
        return names;
    }

    /**
     * Determines the default folder fullnames (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     * 
     * @param mailAccount The mail account providing the fullnames
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder fullnames as an array of {@link String}
     */
    public String[] getDefaultFolderFullnames(final MailAccount mailAccount, final boolean isSpamEnabled) {
        return getDefaultFolderNames(
            mailAccount.getTrashFullname(),
            mailAccount.getSentFullname(),
            mailAccount.getDraftsFullname(),
            mailAccount.getSpamFullname(),
            mailAccount.getConfirmedSpamFullname(),
            mailAccount.getConfirmedHamFullname(),
            isSpamEnabled);
    }

    /**
     * Determines the default folder fullnames (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     * 
     * @param trashFullname The trash fullname
     * @param sentFullname The sent fullname
     * @param draftsFullname The drafts fullname
     * @param spamFullname The spam fullname
     * @param confirmedSpamFullname The confirmed-spam fullname
     * @param confirmedHamFullname The confirmed-ham fullname
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder fullnames as an array of {@link String}
     */
    public String[] getDefaultFolderFullnames(final String trashFullname, final String sentFullname, final String draftsFullname, final String spamFullname, final String confirmedSpamFullname, final String confirmedHamFullname, final boolean isSpamEnabled) throws MailConfigException {
        final String[] fullnames = new String[isSpamEnabled ? 6 : 4];
        if ((draftsFullname != null) && (draftsFullname.length() != 0)) {
            fullnames[INDEX_DRAFTS] = draftsFullname;
        } else {
            fullnames[INDEX_DRAFTS] = null;
        }

        if ((sentFullname != null) && (sentFullname.length() != 0)) {
            fullnames[INDEX_SENT] = sentFullname;
        } else {
            fullnames[INDEX_SENT] = null;
        }

        if ((spamFullname != null) && (spamFullname.length() != 0)) {
            fullnames[INDEX_SPAM] = spamFullname;
        } else {
            fullnames[INDEX_SPAM] = null;
        }

        if ((trashFullname != null) && (trashFullname.length() != 0)) {
            fullnames[INDEX_TRASH] = trashFullname;
        } else {
            fullnames[INDEX_TRASH] = null;
        }

        if (isSpamEnabled) {
            if ((confirmedSpamFullname != null) && (confirmedSpamFullname.length() != 0)) {
                fullnames[INDEX_CONFIRMED_SPAM] = confirmedSpamFullname;
            } else {
                fullnames[INDEX_CONFIRMED_SPAM] = null;
            }

            if ((confirmedHamFullname != null) && (confirmedHamFullname.length() != 0)) {
                fullnames[INDEX_CONFIRMED_HAM] = confirmedHamFullname;
            } else {
                fullnames[INDEX_CONFIRMED_HAM] = null;
            }
        }
        return fullnames;
    }

    private static interface FallbackProvider {

        String getTrash();

        String getSent();

        String getDrafts();

        String getSpam();

        String getConfirmedSpam();

        String getConfirmeHam();
    }

    private static final class DefaultAccountProvider implements FallbackProvider {

        private final MailAccount defaultAccount;

        public DefaultAccountProvider(final MailAccount defaultAccount) {
            super();
            this.defaultAccount = defaultAccount;
        }

        public String getConfirmeHam() {
            final String ret = defaultAccount.getConfirmedHam();
            if (ret == null || ret.length() == 0) {
                return DEFAULT_PROVIDER.getConfirmeHam();
            }
            return ret;
        }

        public String getConfirmedSpam() {
            final String ret = defaultAccount.getConfirmedSpam();
            if (ret == null || ret.length() == 0) {
                return DEFAULT_PROVIDER.getConfirmedSpam();
            }
            return ret;
        }

        public String getDrafts() {
            final String ret = defaultAccount.getDrafts();
            if (ret == null || ret.length() == 0) {
                return DEFAULT_PROVIDER.getDrafts();
            }
            return ret;
        }

        public String getSent() {
            final String ret = defaultAccount.getSent();
            if (ret == null || ret.length() == 0) {
                return DEFAULT_PROVIDER.getSent();
            }
            return ret;
        }

        public String getSpam() {
            final String ret = defaultAccount.getSpam();
            if (ret == null || ret.length() == 0) {
                return DEFAULT_PROVIDER.getSpam();
            }
            return ret;
        }

        public String getTrash() {
            final String ret = defaultAccount.getTrash();
            if (ret == null || ret.length() == 0) {
                return DEFAULT_PROVIDER.getTrash();
            }
            return ret;
        }

    }

    static final FallbackProvider DEFAULT_PROVIDER = new FallbackProvider() {

        public String getConfirmeHam() {
            return STD_CONFIRMED_HAM;
        }

        public String getConfirmedSpam() {
            return STD_CONFIRMED_HAM;
        }

        public String getDrafts() {
            return STD_DRAFTS;
        }

        public String getSent() {
            return STD_SENT;
        }

        public String getSpam() {
            return STD_SPAM;
        }

        public String getTrash() {
            return STD_TRASH;
        }
    };
}
