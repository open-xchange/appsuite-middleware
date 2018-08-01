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
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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

    private final String path;
    private final String password;

    private KeyStore store;

    private int storeHash = -1;

    /**
     * Initializes a new {@link ConfigAwareKeyStore}.
     * 
     * @param configuration The {@link Configuration} to get the values from
     * @param path The path to the store
     * @param password The optional password for the store
     * @param type The type of the store. See <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#KeystoreImplementation">JCA reference guide</a>
     * 
     * @throws OXException If path is invalid or {@link KeyStore} can't be loaded
     * 
     */
    public ConfigAwareKeyStore(Configuration configuration, String path, String password, String type) throws OXException {
        super();

        this.path = path;
        if (Strings.isEmpty(path) || Strings.isEmpty(configuration.getJdbcProps().getProperty(path))) {
            throw DatabaseExceptionCodes.KEYSTORE_FILE_ERROR.create(path);
        }

        this.password = password;

        try {
            String actualType = configuration.getJdbcProps().getProperty(type);
            this.store = KeyStore.getInstance(Strings.isEmpty(actualType) ? KeyStore.getDefaultType() : actualType);
        } catch (KeyStoreException e) {
            LOGGER.debug("Was not able to load KeyStore for type {}.", type);
            throw DatabaseExceptionCodes.KEYSTORE_UNAVAILABLE.create(e, e.getMessage());
        }
    }

    private String stripPath(String keystorePath) {
        String retval;
        int lastIndexOf = keystorePath.lastIndexOf(":");
        if (keystorePath.charAt(lastIndexOf + 2) == '/') {
            retval = keystorePath.substring(lastIndexOf + 2, keystorePath.length());
        } else {
            retval = keystorePath.substring(lastIndexOf + 1, keystorePath.length());
        }
        return retval;
    }

    /**
     * (Re-) loads the {@link KeyStore} hold by this instance with given for given configuration
     * 
     * @param configuration The {@link Configuration} to get the values from
     * @return <code>true</code> if the underlying {@link KeyStore} was (re)loaded
     * @throws OXException In case store can't be found or accessed
     */
    public boolean reloadStore(Configuration configuration) throws OXException {
        // Get path and password
        String keystorePath = configuration.getJdbcProps().getProperty(path);
        String keystorePassword = configuration.getJdbcProps().getProperty(password);

        if (Strings.isEmpty(keystorePath)) {
            throw DatabaseExceptionCodes.KEYSTORE_FILE_ERROR.create(keystorePath);
        }

        File keyStoreFile = new File(stripPath(keystorePath));
        if (false == keyStoreFile.exists() || false == keyStoreFile.isFile()) {
            throw DatabaseExceptionCodes.KEYSTORE_FILE_ERROR.create(stripPath(keystorePath));
        }

        if (keyStoreFile.hashCode() != storeHash) {
            // (Re-) Load the key store
            try (FileInputStream in = new FileInputStream(keyStoreFile)) {
                store.load(in, null == keystorePassword ? null : keystorePassword.toCharArray());
                storeHash = keyStoreFile.hashCode();
                return true;
            } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                LOGGER.debug("Unable to load keystore!", e);
                throw DatabaseExceptionCodes.KEYSTORE_UNAVAILABLE.create(e, e.getMessage());
            }
        }
        return false;

    }

    public KeyStore getStore() {
        return store;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ConfigAwareKeyStore.class.getName());
        sb.append("=[storeHash:").append(storeHash);
        sb.append(",path:").append(path);
        sb.append(",password:").append(password);
        sb.append(",keystore:").append(store.toString());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((store == null) ? 0 : ConfigurationUtil.getHashSum(store));
        result = prime * result + storeHash;
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
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (store == null) {
            if (other.store != null) {
                return false;
            }
        } else if (0 != ConfigurationUtil.compare(store, other.store)) {
            return false;
        }
        if (storeHash != other.storeHash) {
            return false;
        }
        return true;
    }

}
