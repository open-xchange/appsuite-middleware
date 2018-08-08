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

package com.openexchange.database.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ConfigAwareKeyStore} - Links a {@link KeyStore} to the {@link Configuration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ConfigAwareKeyStore {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigAwareKeyStore.class);

    private final String propertyName;
    private final String passwordPropertyName;
    private final String typePropertyName;
    private String filePath;
    private KeyStore store;
    private int storeHash = -1;



    /**
     * Initializes a new {@link ConfigAwareKeyStore}.
     *
     * @param configuration The {@link Configuration} to get the values from
     * @param propertyName The name of the JDBC property for the store
     * @param passwordPropertyName The optional password for the store
     * @param typePropertyName The type of the store. See <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#KeystoreImplementation">JCA reference guide</a>
     */
    public ConfigAwareKeyStore(Configuration configuration, String propertyName, String passwordPropertyName, String typePropertyName) {
        super();
        if (Strings.isNotEmpty(propertyName) && Strings.isNotEmpty(configuration.getJdbcProps().getProperty(propertyName))) {
            filePath = configuration.getJdbcProps().getProperty(propertyName);
        }
        this.propertyName = propertyName;
        this.passwordPropertyName = passwordPropertyName;
        this.typePropertyName = typePropertyName;
        try {
            initializeKeyStore(configuration, typePropertyName);
        } catch (KeyStoreException e) {
            LOGGER.error("Unable to load keystore for type {}.", typePropertyName, e);
        }
    }

    
    /**
     * (Re-)loads the {@link KeyStore} held by this instance using given configuration
     *
     * @param configuration The {@link Configuration} to get the values from
     * @return <code>true</code> if the underlying {@link KeyStore} was (re-)loaded; otherwise <code>false</code>
     * @throws OXException If store can't be found or accessed
     */
    public boolean reloadStore(Configuration configuration) throws OXException {
        // Get path and password
        String keystorePath = configuration.getJdbcProps().getProperty(propertyName);
        String keystorePassword = configuration.getJdbcProps().getProperty(passwordPropertyName);

        // Check if this instance is used
        if (Strings.isEmpty(keystorePath)) {
            if (Strings.isEmpty(filePath)) {
                // No store configured
                return false;
            } else {
                // Store was removed
                store = null;
                storeHash = -1;
                return true;
            }
        }

        File keyStoreFile = new File(stripPath(keystorePath));
        if (false == keyStoreFile.exists() || false == keyStoreFile.isFile()) {
            throw DatabaseExceptionCodes.KEYSTORE_FILE_ERROR.create(stripPath(keystorePath));
        }

        int currentHash = getBytes(keyStoreFile);
        if (storeHash != currentHash) {
            /*
             * (Re-) Load the key store
             *
             * We do this also knowing that the JDBC is loading the stores (for each connection) itself.
             * See com.mysql.jdbc.ExportControlled.getSSLSocketFactoryDefaultOrConfigured(MysqlIO)
             *
             * Advantage: We can avoid the generic 'CommunicationLinkFailure' error by testing the SSL
             *            properties and outline more helpful messages.
             */
            try (FileInputStream in = new FileInputStream(keyStoreFile)) {
                if (null == store) {
                    initializeKeyStore(configuration, typePropertyName);
                }
                store.load(in, null == keystorePassword ? null : keystorePassword.toCharArray());
                storeHash = currentHash;
                return true;

            } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
                throw DatabaseExceptionCodes.KEYSTORE_UNAVAILABLE.create(e, e.getMessage());
            }
        }
        return false;
    }

    private String stripPath(String keystorePath) {
        return keystorePath.substring(keystorePath.lastIndexOf(":") + 1, keystorePath.length());
    }

    private int getBytes(File f) throws OXException {
        try (FileInputStream in = new FileInputStream(f)) {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] block = new byte[4096];
            int length = 0;
            while ((length = in.read(block)) != -1) {
                md5.update(block, 0, length);
            }
            return Arrays.hashCode(md5.digest());
        } catch (Exception e) {
            throw DatabaseExceptionCodes.KEYSTORE_UNAVAILABLE.create(e, e.getMessage());
        }
    }

    private void initializeKeyStore(Configuration configuration, String typePropertyName) throws KeyStoreException {
        String actualType = configuration.getJdbcProps().getProperty(typePropertyName);
        this.store = KeyStore.getInstance(Strings.isEmpty(actualType) ? KeyStore.getDefaultType() : actualType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ConfigAwareKeyStore.class.getName());
        sb.append("=[storeHash:").append(storeHash);
        sb.append(",propertyName:").append(propertyName);
        sb.append(",password:").append(passwordPropertyName);
        sb.append(",keystore:").append(null != store ? store.toString() : "");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + storeHash;
        result = prime * result + ((passwordPropertyName == null) ? 0 : passwordPropertyName.hashCode());
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
        result = prime * result + ((store == null) ? 0 : ConfigurationUtil.getHashSum(store));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConfigAwareKeyStore other = (ConfigAwareKeyStore) obj;
        if (storeHash != other.storeHash) {
            return false;
        }
        if (passwordPropertyName == null) {
            if (other.passwordPropertyName != null) {
                return false;
            }
        } else if (!passwordPropertyName.equals(other.passwordPropertyName)) {
            return false;
        }
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        if (store == null) {
            if (other.store != null) {
                return false;
            }
        } else if (0 != ConfigurationUtil.compare(store, other.store)) {
            return false;
        }
        return true;
    }

}
