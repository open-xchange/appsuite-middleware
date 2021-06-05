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

package com.openexchange.mail.utils;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.java.Charsets;
import com.openexchange.java.util.Tools;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link StorageUtility} - Offers utility methods for both folder and message storage
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StorageUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StorageUtility.class);

    /*
     * Public constants
     */

    /** The index for standard drafts folder */
    public static final int INDEX_DRAFTS = 0;

    /** The index for standard sent folder */
    public static final int INDEX_SENT = 1;

    /** The index for standard spam folder */
    public static final int INDEX_SPAM = 2;

    /** The index for standard trash folder */
    public static final int INDEX_TRASH = 3;

    /** The index for standard confirmed-spam folder */
    public static final int INDEX_CONFIRMED_SPAM = 4;

    /** The index for standard confirmed-ham folder */
    public static final int INDEX_CONFIRMED_HAM = 5;

    /** The index for standard INBOX folder */
    public static final int INDEX_INBOX = 6;

    /** Signals to perform a hard delete */
    public static final int MAIL_PARAM_HARD_DELETE = 1;

    /** Unlimted quota */
    public static final int UNLIMITED_QUOTA = -1;

    /** An empty <code>javax.mail.Message</code> array */
    public static final Message[] EMPTY_MSGS = new Message[0];

    /**
     * Prevent instantiation
     */
    private StorageUtility() {
        super();
    }

    /**
     * Gets the default max. running millis.
     *
     * @return The default max. running millis
     */
    public static long getMaxRunningMillis() {
        final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == service) {
            return 60000L;
        }
        return service.getIntProperty("com.openexchange.requestwatcher.maxRequestAge", 60000);
    }

    public static String getAllAddresses(InternetAddress[] internetAddrs) {
        if ((internetAddrs == null) || (internetAddrs.length == 0)) {
            return "";
        }
        final StringBuilder addressBuilder = new StringBuilder(32 * internetAddrs.length);
        addressBuilder.append(internetAddrs[0].toUnicodeString());
        for (int i = 1; i < internetAddrs.length; i++) {
            addressBuilder.append(',').append(internetAddrs[i].toUnicodeString());
        }

        return (addressBuilder.toString());
    }

    /**
     * Parses specified headers into a map
     *
     * @param headers The headers as raw bytes
     * @return An instance of {@link Map} containing the headers
     */
    public static Map<HeaderName, String> parseHeaders(byte[] headers) {
        try {
            return parseHeaders(Charsets.toAsciiString(headers));
        } catch (UnsupportedCharsetException e) {
            /*
             * Cannot occur
             */
            LOG.error("", e);
            return null;
        }
    }

    private static final Pattern PATTERN_PARSE_HEADER = Pattern.compile("(\\S+):\\s(.*)((?:\r?\n(?:\\s(?:.+)))*|(?:$))");

    /**
     * Parses specified headers into a map
     *
     * @param headers The headers as {@link String}
     * @return An instance of {@link Map} containing the headers
     */
    public static Map<HeaderName, String> parseHeaders(String headers) {
        final Matcher m = PATTERN_PARSE_HEADER.matcher(unfold(headers));
        final Map<HeaderName, String> retval = new HashMap<HeaderName, String>();
        final StringBuilder valBuilder = new StringBuilder(256);
        while (m.find()) {
            valBuilder.append(m.group(2));
            if (m.group(3) != null) {
                valBuilder.append(unfold(m.group(3)));
            }
            retval.put(HeaderName.valueOf(m.group(1)), valBuilder.toString());
            valBuilder.setLength(0);
        }
        return retval;
    }

    private static final String SWITCH_DEFAULT_FOLDER = "Switching to default value %s";

    /**
     * Determines the default folder names (<b>not</b> full names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param accountId The account ID
     * @param usm The user's mail settings
     * @return The default folder names as an array of {@link String}
     * @throws OXException If spam enablement/disablement cannot be determined
     */
    public static String[] getDefaultFolderNames(int accountId, UserSettingMail usm) throws OXException {
        return getDefaultFolderNames(accountId, usm, usm.isSpamEnabled());
    }

    /**
     * Determines the default folder names (<b>not</b> full names). The returned array of {@link String} indexes the names as given through
     * constants: {@link StorageUtility#INDEX_DRAFTS}, {@link StorageUtility#INDEX_SENT}, etc.
     *
     * @param accountId The account ID
     * @param usm The user's mail settings
     * @param isSpamEnabled <code>true</code> if spam is enabled for current user; otherwise <code>false</code>
     * @return The default folder names as an array of {@link String}
     * @throws OXException If spam enablement/disablement cannot be determined
     */
    public static String[] getDefaultFolderNames(int accountId, UserSettingMail usm, boolean isSpamEnabled) throws OXException {
        return new DefaultFolderNamesProvider(accountId, usm.getUserId(), usm.getCid()).getDefaultFolderNames(
            usm.getStdTrashName(),
            usm.getStdSentName(),
            usm.getStdDraftsName(),
            usm.getStdSpamName(),
            usm.getConfirmedSpam(),
            usm.getConfirmedHam(),
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
     * @throws MailConfigException If spam enablement/disablement cannot be determined
     * @deprecated Use {@link DefaultFolderNamesProvider} instead
     */
    @Deprecated
    public static String[] getDefaultFolderNames(String trash, String sent, String drafts, String spam, String confirmedSpam, String confirmedHam, boolean isSpamEnabled) throws MailConfigException {
        final String[] names = new String[isSpamEnabled ? 6 : 4];
        if ((drafts == null) || (drafts.length() == 0)) {
            final OXException e = MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(MailStrings.DRAFTS);
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, MailStrings.DRAFTS), e);
            names[INDEX_DRAFTS] = MailStrings.DRAFTS;
        } else {
            names[INDEX_DRAFTS] = drafts;
        }
        if ((sent == null) || (sent.length() == 0)) {
            final OXException e = MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(MailStrings.SENT);
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, MailStrings.SENT), e);
            names[INDEX_SENT] = MailStrings.SENT;
        } else {
            names[INDEX_SENT] = sent;
        }
        if ((spam == null) || (spam.length() == 0)) {
            final OXException e = MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(MailStrings.SPAM);
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, MailStrings.SPAM), e);
            names[INDEX_SPAM] = MailStrings.SPAM;
        } else {
            names[INDEX_SPAM] = spam;
        }
        if ((trash == null) || (trash.length() == 0)) {
            final OXException e = MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(MailStrings.TRASH);
            LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, MailStrings.TRASH), e);
            names[INDEX_TRASH] = MailStrings.TRASH;
        } else {
            names[INDEX_TRASH] = trash;
        }
        if (isSpamEnabled) {
            if ((confirmedSpam == null) || (confirmedSpam.length() == 0)) {
                final OXException e = MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(MailStrings.CONFIRMED_SPAM);
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, MailStrings.CONFIRMED_SPAM), e);
                names[INDEX_CONFIRMED_SPAM] = MailStrings.CONFIRMED_SPAM;
            } else {
                names[INDEX_CONFIRMED_SPAM] = confirmedSpam;
            }
            if ((confirmedHam == null) || (confirmedHam.length() == 0)) {
                final OXException e = MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(MailStrings.CONFIRMED_HAM);
                LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, MailStrings.CONFIRMED_HAM), e);
                names[INDEX_CONFIRMED_HAM] = MailStrings.CONFIRMED_HAM;
            } else {
                names[INDEX_CONFIRMED_HAM] = confirmedHam;
            }
        }
        return names;
    }

    /**
     * Gets the prepared mail fields for search.
     *
     * @param mailFields The requested mail fields by client
     * @param sortField The sort field
     * @return The prepared mail fields for search
     */
    public static MailFields prepareMailFieldsForSearch(MailField[] mailFields, MailSortField sortField) {
        final MailFields usedFields = new MailFields(mailFields);
        usedFields.add(MailField.toField(sortField.getListField()));

        // Second-level sort field
        usedFields.add(MailField.RECEIVED_DATE);
        return usedFields;
    }

    /**
     * Gets the prepared mail fields for search.
     *
     * @param mailFields The requested mail fields by client
     * @param sortField The sort field
     * @return The prepared mail fields for search
     */
    public static MailFields prepareMailFieldsForSearch(MailFields mailFields, MailSortField sortField) {
        final MailFields usedFields = new MailFields(mailFields);
        usedFields.add(MailField.toField(sortField.getListField()));

        // Second-level sort field
        usedFields.add(MailField.RECEIVED_DATE);
        return usedFields;
    }

    /**
     * Parses the string argument as a signed decimal <code>long</code>. The characters in the string must all be decimal digits.
     * <p>
     * Note that neither the character <code>L</code> (<code>'&#92;u004C'</code>) nor <code>l</code> (<code>'&#92;u006C'</code>) is
     * permitted to appear at the end of the string as a type indicator, as would be permitted in Java programming language source code.
     *
     * @param s A <code>String</code> containing the <code>long</code> representation to be parsed
     * @return The <code>long</code> represented by the argument in decimal or <code>-1</code> if the string does not contain a parsable
     *         <code>long</code>.
     */
    public static long parseUnsignedLong(String s) {
        return Tools.getUnsignedLong(s);
    }

}
