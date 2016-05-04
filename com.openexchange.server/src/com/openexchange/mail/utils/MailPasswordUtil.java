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

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.openexchange.crypto.CryptoService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.internal.GenericProperty;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.secret.SecretExceptionCodes;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MailPasswordUtil} - Utility class to encrypt/decrypt passwords with a key aka <b>p</b>assword <b>b</b>ased <b>e</b>ncryption
 * (PBE).
 * <p>
 * PBE is a form of symmetric encryption where the same key or password is used to encrypt and decrypt a string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailPasswordUtil {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailPasswordUtil.class);

    /** The key length. */
    private static final int KEY_LENGTH = 8;

    /** The DES algorithm. */
    private static final String ALGORITHM_DES = "DES";

    /** The transformation following pattern <i>"algorithm/mode/padding"</i>. */
    private static final String CIPHER_TYPE = ALGORITHM_DES + "/ECB/PKCS5Padding";

    /**
     * Mail account secret encryption strategy.
     */
    public static final SecretEncryptionStrategy<GenericProperty> STRATEGY = new SecretEncryptionStrategy<GenericProperty>() {

        @Override
        public void update(final String recrypted, final GenericProperty customizationNote) throws OXException {
            final int contextId = customizationNote.session.getContextId();
            final Connection con = Database.get(contextId, true);
            try {
                con.setAutoCommit(false);
                update0(recrypted, customizationNote, con);
                con.commit();
            } catch (final SQLException e) {
                DBUtils.rollback(con);
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                DBUtils.rollback(con);
                throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.autocommit(con);
                Database.back(contextId, true, con);
            }
        }

        private void update0(final String recrypted, final GenericProperty customizationNote, final Connection con) throws SQLException {
            PreparedStatement stmt = null;
            final Session session = customizationNote.session;
            final MailAccountStorageService service = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null != service) {
                MailAccount mailAccount = null;
                try {
                    mailAccount = service.getMailAccount(customizationNote.accountId, session.getUserId(), session.getContextId());
                } catch (OXException e) {
                    LOG.warn("Could not update encrypted mail account password.", e);
                    return;
                }

                if (customizationNote.server != null) {
                    try {
                        if (customizationNote.server.equals(mailAccount.getMailServer())) {
                            stmt = con.prepareStatement("UPDATE user_mail_account SET password = ? WHERE cid = ? AND user = ? AND id = ?");
                            stmt.setString(1, recrypted);
                            stmt.setInt(2, session.getContextId());
                            stmt.setInt(3, session.getUserId());
                            stmt.setInt(4, customizationNote.accountId);
                            stmt.executeUpdate();
                            DBUtils.closeSQLStuff(stmt);
                        }

                        if (customizationNote.server.equals(mailAccount.getTransportServer())) {
                            stmt = con.prepareStatement("UPDATE user_transport_account SET password = ? WHERE cid = ? AND user = ? AND id = ? AND (password IS NOT NULL AND password <> '')");
                            stmt.setString(1, recrypted);
                            stmt.setInt(2, session.getContextId());
                            stmt.setInt(3, session.getUserId());
                            stmt.setInt(4, customizationNote.accountId);
                            stmt.executeUpdate();
                        }
                    } finally {
                        DBUtils.closeSQLStuff(stmt);
                    }

                    try {
                        service.invalidateMailAccount(customizationNote.accountId, session.getUserId(), session.getContextId());
                    } catch (final Exception e) {
                        LOG.warn("Could not invalidate mail account after password update.", e);
                    }
                }
            }
        }
    };

    /**
     * Encrypts specified password with given key.
     *
     * @param password The password
     * @param key The key
     * @return The encrypted password as Base64 encoded string
     * @throws GeneralSecurityException If password encryption fails
     */
    public static String encrypt(final String password, final String key) throws GeneralSecurityException {
        return encrypt(password, generateSecretKey(key));
    }

    /**
     * Decrypts specified encrypted password with given key.
     *
     * @param encryptedPassword The Base64 encoded encrypted password
     * @param session The session
     * @return The decrypted password
     * @throws GeneralSecurityException If password decryption fails
     * @throws OXException
     */
    public static String decrypt(final String encryptedPassword, final Session session, final int accountId, final String login, final String server) throws OXException {
        try {
            SecretEncryptionService<GenericProperty> encryptionService = ServerServiceRegistry.getInstance().getService(SecretEncryptionFactoryService.class).createService(STRATEGY);
            return encryptionService.decrypt(session, encryptedPassword, new GenericProperty(accountId, session, login, server));
        } catch (OXException e) {
            if (!SecretExceptionCodes.EMPTY_SECRET.equals(e)) {
                throw e;
            }
            // Apparently empty password
            OXException oxe = MailExceptionCode.CONFIG_ERROR.create(e, "The mail configuration is invalid. Please check \"com.openexchange.mail.passwordSource\" property or set a valid secret source in file 'secret.properties'.");
            oxe.setCategory(Category.CATEGORY_CONFIGURATION);
            throw oxe;
        }
    }

    /**
     * Decrypts specified encrypted password with given key.
     *
     * @param encryptedPassword The Base64 encoded encrypted password
     * @param key The key
     * @return The decrypted password
     * @throws GeneralSecurityException If password decryption fails
     */
    public static String decrypt(final String encryptedPassword, final String key) throws GeneralSecurityException {
        try {
            return decrypt(encryptedPassword, generateSecretKey(key));
        } catch (final GeneralSecurityException e) {
            // Decrypting failed; retry with CryptoService
            final CryptoService crypto = ServerServiceRegistry.getInstance().getService(CryptoService.class);
            if (null == crypto) {
                LOG.warn("MailPasswordUtil.decrypt(): Missing {}", CryptoService.class.getSimpleName());
                throw e;
            }
            try {
                return crypto.decrypt(encryptedPassword, key);
            } catch (final OXException ce) {
                // CryptoServce failed, too
                final StringBuilder sb = new StringBuilder(128).append("MailPasswordUtil.decrypt(): Failed to decrypt \"");
                sb.append(encryptedPassword).append("\" with ").append(CryptoService.class.getSimpleName());
                LOG.debug(sb.toString(), ce);
            }
            throw e;
        }
    }

    /**
     * Encrypts specified password with given key.
     *
     * @param password The password to encrypt
     * @param key The key
     * @return The encrypted password as Base64 encoded string
     * @throws GeneralSecurityException If password encryption fails
     */
    public static String encrypt(final String password, final Key key) throws GeneralSecurityException {
        if (null == password || null == key) {
            return null;
        }
        final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        final byte[] outputBytes = cipher.doFinal(password.getBytes(com.openexchange.java.Charsets.UTF_8));
        /*-
         * It's safe to use "US-ASCII" to turn bytes into a Base64 encoded encrypted password string.
         * Taken from RFC 2045 Section 6.8. "Base64 Content-Transfer-Encoding":
         *
         * A 65-character subset of US-ASCII is used, enabling 6 bits to be
         * represented per printable character. (The extra 65th character, "=",
         * is used to signify a special processing function.)
         *
         * NOTE: This subset has the important property that it is represented
         * identically in all versions of ISO 646, including US-ASCII, and all
         * characters in the subset are also represented identically in all
         * versions of EBCDIC. Other popular encodings, such as the encoding
         * used by the uuencode utility, Macintosh binhex 4.0 [RFC-1741], and
         * the base85 encoding specified as part of Level 2 PostScript, do not
         * share these properties, and thus do not fulfill the portability
         * requirements a binary transport encoding for mail must meet.
         *
         */
        return Charsets.toAsciiString(org.apache.commons.codec.binary.Base64.encodeBase64(outputBytes));
    }

    /**
     * Decrypts specified encrypted password with given key.
     *
     * @param encryptedPassword The Base64 encoded encrypted password
     * @param key The key
     * @return The decrypted password
     * @throws GeneralSecurityException If password decryption fails
     */
    public static String decrypt(final String encryptedPassword, final Key key) throws GeneralSecurityException {
        if (null == encryptedPassword || null == key) {
            return null;
        }
        final byte encrypted[];
        {
            /*-
             * It's safe to use "US-ASCII" to turn Base64 encoded encrypted password string into bytes.
             * Taken from RFC 2045 Section 6.8. "Base64 Content-Transfer-Encoding":
             *
             * A 65-character subset of US-ASCII is used, enabling 6 bits to be
             * represented per printable character. (The extra 65th character, "=",
             * is used to signify a special processing function.)
             *
             * NOTE: This subset has the important property that it is represented
             * identically in all versions of ISO 646, including US-ASCII, and all
             * characters in the subset are also represented identically in all
             * versions of EBCDIC. Other popular encodings, such as the encoding
             * used by the uuencode utility, Macintosh binhex 4.0 [RFC-1741], and
             * the base85 encoding specified as part of Level 2 PostScript, do not
             * share these properties, and thus do not fulfill the portability
             * requirements a binary transport encoding for mail must meet.
             *
             */
            encrypted = org.apache.commons.codec.binary.Base64.decodeBase64(Charsets.toAsciiBytes(encryptedPassword));
        }

        final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, key);

        final byte[] outputBytes = cipher.doFinal(encrypted);

        return new String(outputBytes, com.openexchange.java.Charsets.UTF_8);
    }

    /**
     * Create a key for use in the cipher code
     */
    public static Key generateRandomKey() throws NoSuchAlgorithmException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_DES);
        keyGenerator.init(new SecureRandom());
        final SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }

    /**
     * Generates a secret key from specified key string.
     *
     * @param key The key string
     * @return A secret key generated from specified key string
     * @throws GeneralSecurityException If generating secret key fails
     */
    public static Key generateSecretKey(final String key) throws GeneralSecurityException {
        if (null == key) {
            return null;
        }
        return new SecretKeySpec(ensureLength(key.getBytes(com.openexchange.java.Charsets.UTF_8)), ALGORITHM_DES);
    }

    private static byte[] ensureLength(final byte[] bytes) {
        final byte[] keyBytes;
        final int len = bytes.length;
        if (len < KEY_LENGTH) {
            keyBytes = new byte[KEY_LENGTH];
            System.arraycopy(bytes, 0, keyBytes, 0, len);
            for (int i = len; i < keyBytes.length; i++) {
                keyBytes[i] = 48;
            }
        } else if (len > KEY_LENGTH) {
            keyBytes = new byte[KEY_LENGTH];
            System.arraycopy(bytes, 0, keyBytes, 0, keyBytes.length);
        } else {
            keyBytes = bytes;
        }
        return keyBytes;
    }

    /*-
     * Encode a secret key as a string that can be stored for later use.
     *
     * @param key
     * @return
    public static String encodeKey(final Key key) {
        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(key.getEncoded());
    }
     */

    /*-
     * Reconstruct a secret key from a string representation.
     *
     * @param encodedKey
     * @return
     * @throws IOException
    public static Key decodeKey(final String encodedKey) throws IOException {
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte raw[] = decoder.decodeBuffer(encodedKey);
        final SecretKey key = new SecretKeySpec(raw, "DES");
        return key;
    }
     */

}
