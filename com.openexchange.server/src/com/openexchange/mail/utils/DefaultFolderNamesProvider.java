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

package com.openexchange.mail.utils;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.mail.utils.StorageUtility.INDEX_CONFIRMED_HAM;
import static com.openexchange.mail.utils.StorageUtility.INDEX_CONFIRMED_SPAM;
import static com.openexchange.mail.utils.StorageUtility.INDEX_DRAFTS;
import static com.openexchange.mail.utils.StorageUtility.INDEX_SENT;
import static com.openexchange.mail.utils.StorageUtility.INDEX_SPAM;
import static com.openexchange.mail.utils.StorageUtility.INDEX_TRASH;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link DefaultFolderNamesProvider} - Provides the default folder (full-)names for a certain mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultFolderNamesProvider {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(DefaultFolderNamesProvider.class);

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
     * @throws OXException If initialization fails
     */
    public DefaultFolderNamesProvider(final int accountId, final int user, final int cid) throws OXException {
        super();
        if (MailAccount.DEFAULT_ID == accountId) {
            fallbackProvider = DEFAULT_PROVIDER;
        } else {
            final MailAccountFacade mailAccountFacade = ServerServiceRegistry.getServize(MailAccountFacade.class, true);
            fallbackProvider = new DefaultAccountFallbackProvider(mailAccountFacade.getDefaultMailAccount(user, cid));
        }
    }

    /**
     * Determines the default folder names (<b>not</b> full names). The returned array of {@link String} indexes the names as given through
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
     * Determines the default folder names (<b>not</b> full names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param mailAccount The mail account providing the names
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder names as an array of {@link String}
     */
    public String[] getDefaultFolderNames(final MailAccountDescription mailAccount, final boolean isSpamEnabled) {
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
     * Determines the default folder names (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param mailConfig The mail configuration providing the names
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder names as an array of {@link String}
     */
    public String[] getDefaultFolderNames(final MailConfig mailConfig, final boolean isSpamEnabled) {
        final String[] standardNames = mailConfig.getStandardNames();
        return getDefaultFolderNames(
            standardNames[StorageUtility.INDEX_TRASH],
            standardNames[StorageUtility.INDEX_SENT],
            standardNames[StorageUtility.INDEX_DRAFTS],
            standardNames[StorageUtility.INDEX_SPAM],
            standardNames[StorageUtility.INDEX_CONFIRMED_SPAM],
            standardNames[StorageUtility.INDEX_CONFIRMED_HAM],
            isSpamEnabled);
    }

    /**
     * Determines the default folder names (<b>not</b> full names). The returned array of {@link String} indexes the names as given through
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
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, fallbackProvider.getDrafts()));
            names[INDEX_DRAFTS] = fallbackProvider.getDrafts();
        } else {
            names[INDEX_DRAFTS] = drafts;
        }
        if ((sent == null) || (sent.length() == 0)) {
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, fallbackProvider.getSent()));
            names[INDEX_SENT] = fallbackProvider.getSent();
        } else {
            names[INDEX_SENT] = sent;
        }
        if ((spam == null) || (spam.length() == 0)) {
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, fallbackProvider.getSpam()));
            names[INDEX_SPAM] = fallbackProvider.getSpam();
        } else {
            names[INDEX_SPAM] = spam;
        }
        if ((trash == null) || (trash.length() == 0)) {
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, fallbackProvider.getTrash()));
            names[INDEX_TRASH] = fallbackProvider.getTrash();
        } else {
            names[INDEX_TRASH] = trash;
        }
        if (isSpamEnabled) {
            if ((confirmedSpam == null) || (confirmedSpam.length() == 0)) {
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, fallbackProvider.getConfirmedSpam()));
                names[INDEX_CONFIRMED_SPAM] = fallbackProvider.getConfirmedSpam();
            } else {
                names[INDEX_CONFIRMED_SPAM] = confirmedSpam;
            }
            if ((confirmedHam == null) || (confirmedHam.length() == 0)) {
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, fallbackProvider.getConfirmedHam()));
                names[INDEX_CONFIRMED_HAM] = fallbackProvider.getConfirmedHam();
            } else {
                names[INDEX_CONFIRMED_HAM] = confirmedHam;
            }
        }
        return names;
    }

    /**
     * Determines the default folder full names (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param mailAccount The mail account providing the full names
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder full names as an array of {@link String}
     */
    public String[] getDefaultFolderFullnames(final MailAccount mailAccount, final boolean isSpamEnabled) {
        return getDefaultFolderFullnames(
            extractFullname(mailAccount.getTrashFullname()),
            extractFullname(mailAccount.getSentFullname()),
            extractFullname(mailAccount.getDraftsFullname()),
            extractFullname(mailAccount.getSpamFullname()),
            extractFullname(mailAccount.getConfirmedSpamFullname()),
            extractFullname(mailAccount.getConfirmedHamFullname()),
            isSpamEnabled);
    }

    /**
     * Determines the default folder full names (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param mailAccount The mail account providing the full names
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder full names as an array of {@link String}
     */
    public String[] getDefaultFolderFullnames(final MailAccountDescription mailAccount, final boolean isSpamEnabled) {
        return getDefaultFolderFullnames(
            extractFullname(mailAccount.getTrashFullname()),
            extractFullname(mailAccount.getSentFullname()),
            extractFullname(mailAccount.getDraftsFullname()),
            extractFullname(mailAccount.getSpamFullname()),
            extractFullname(mailAccount.getConfirmedSpamFullname()),
            extractFullname(mailAccount.getConfirmedHamFullname()),
            isSpamEnabled);
    }

    /**
     * Extracts full name from passed full name parameter.
     *
     * @param fullnameParameter The full name parameter
     * @return The extracted full name
     */
    public static String extractFullname(final String fullnameParameter) {
        return null == fullnameParameter ? null : MailFolderUtility.prepareMailFolderParam(fullnameParameter).getFullname();
    }

    /**
     * Determines the default folder full names (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param mailConfig The mail configuration providing the full names
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder full names as an array of {@link String}
     */
    public String[] getDefaultFolderFullnames(final MailConfig mailConfig, final boolean isSpamEnabled) {
        final String[] standardFullNames = mailConfig.getStandardFullNames();
        return getDefaultFolderFullnames(
            standardFullNames[StorageUtility.INDEX_TRASH],
            standardFullNames[StorageUtility.INDEX_SENT],
            standardFullNames[StorageUtility.INDEX_DRAFTS],
            standardFullNames[StorageUtility.INDEX_SPAM],
            standardFullNames[StorageUtility.INDEX_CONFIRMED_SPAM],
            standardFullNames[StorageUtility.INDEX_CONFIRMED_HAM],
            isSpamEnabled);
    }

    /**
     * Determines the default folder full names (<b>not</b> names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param trashFullname The trash full name
     * @param sentFullname The sent full name
     * @param draftsFullname The drafts full name
     * @param spamFullname The spam full name
     * @param confirmedSpamFullname The confirmed-spam full name
     * @param confirmedHamFullname The confirmed-ham full name
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder full names as an array of {@link String}
     */
    public String[] getDefaultFolderFullnames(final String trashFullname, final String sentFullname, final String draftsFullname, final String spamFullname, final String confirmedSpamFullname, final String confirmedHamFullname, final boolean isSpamEnabled) {
        final String[] fullnames = new String[isSpamEnabled ? 6 : 4];
        if (isEmpty(draftsFullname)) {
            fullnames[INDEX_DRAFTS] = null;
        } else {
            fullnames[INDEX_DRAFTS] = draftsFullname;
        }

        if (isEmpty(sentFullname)) {
            fullnames[INDEX_SENT] = null;
        } else {
            fullnames[INDEX_SENT] = sentFullname;
        }

        if (isEmpty(spamFullname)) {
            fullnames[INDEX_SPAM] = null;
        } else {
            fullnames[INDEX_SPAM] = spamFullname;
        }

        if (isEmpty(trashFullname)) {
            fullnames[INDEX_TRASH] = null;
        } else {
            fullnames[INDEX_TRASH] = trashFullname;
        }

        if (isSpamEnabled) {
            if (isEmpty(confirmedSpamFullname)) {
                fullnames[INDEX_CONFIRMED_SPAM] = null;
            } else {
                fullnames[INDEX_CONFIRMED_SPAM] = confirmedSpamFullname;
            }

            if (isEmpty(confirmedHamFullname)) {
                fullnames[INDEX_CONFIRMED_HAM] = null;
            } else {
                fullnames[INDEX_CONFIRMED_HAM] = confirmedHamFullname;
            }
        }
        return fullnames;
    }

    /**
     * Provides fall-back values.
     */
    public static interface FallbackProvider {

        String getTrash();

        String getSent();

        String getDrafts();

        String getSpam();

        String getConfirmedSpam();

        String getConfirmedHam();

        String getArchive();
    }

    private static final class DefaultAccountFallbackProvider implements FallbackProvider {

        private final MailAccount defaultAccount;

        public DefaultAccountFallbackProvider(final MailAccount defaultAccount) {
            super();
            this.defaultAccount = defaultAccount;
        }

        @Override
        public String getConfirmedHam() {
            final String ret = defaultAccount.getConfirmedHam();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getConfirmedHam();
            }
            return ret;
        }

        @Override
        public String getConfirmedSpam() {
            final String ret = defaultAccount.getConfirmedSpam();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getConfirmedSpam();
            }
            return ret;
        }

        @Override
        public String getDrafts() {
            final String ret = defaultAccount.getDrafts();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getDrafts();
            }
            return ret;
        }

        @Override
        public String getSent() {
            final String ret = defaultAccount.getSent();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getSent();
            }
            return ret;
        }

        @Override
        public String getSpam() {
            final String ret = defaultAccount.getSpam();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getSpam();
            }
            return ret;
        }

        @Override
        public String getTrash() {
            final String ret = defaultAccount.getTrash();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getTrash();
            }
            return ret;
        }

        @Override
        public String getArchive() {
            final String ret = defaultAccount.getArchive();
            if (isEmpty(ret)) {
                return DEFAULT_PROVIDER.getArchive();
            }
            return ret;
        }

    }

    /**
     * The fall-back provider.
     */
    public static final FallbackProvider DEFAULT_PROVIDER = new FallbackProvider() {

        @Override
        public String getConfirmedHam() {
            return MailStrings.CONFIRMED_HAM;
        }

        @Override
        public String getConfirmedSpam() {
            return MailStrings.CONFIRMED_SPAM;
        }

        @Override
        public String getDrafts() {
            return MailStrings.DRAFTS;
        }

        @Override
        public String getSent() {
            return MailStrings.SENT;
        }

        @Override
        public String getSpam() {
            return MailStrings.SPAM;
        }

        @Override
        public String getTrash() {
            return MailStrings.TRASH;
        }

        @Override
        public String getArchive() {
            return MailStrings.ARCHIVE;
        }
    };
}
