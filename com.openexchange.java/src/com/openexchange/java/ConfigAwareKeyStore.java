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

package com.openexchange.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConfigAwareKeyStore} - Links a {@link KeyStore} to its configuration via {@link Properties}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ConfigAwareKeyStore {

    private static class KeyStoreInfo {

        final KeyStore store;
        final String md5Sum;

        KeyStoreInfo(KeyStore store, String md5Sum) {
            super();
            this.store = store;
            this.md5Sum = md5Sum;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (md5Sum == null ? 0 : md5Sum.hashCode());
            result = prime * result + ((store == null) ? 0 : KeyStoreUtil.getHashSum(store));
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
            if (!(obj instanceof KeyStoreInfo)) {
                return false;
            }
            KeyStoreInfo other = (KeyStoreInfo) obj;
            if (md5Sum == null) {
                if (other.md5Sum != null) {
                    return false;
                }
            } else if (false == md5Sum.equals(other.md5Sum)) {
                return false;
            }
            if (store == null) {
                if (other.store != null) {
                    return false;
                }
            } else if (0 != KeyStoreUtil.compare(store, other.store)) {
                return false;
            }
            return true;
        }
    }

    private static final KeyStoreInfo NO_KEYSTORE = new KeyStoreInfo(null, null);

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String keystorePathPropertyName;
    private final String passwordPropertyName;
    private final String typePropertyName;
    private final AtomicReference<KeyStoreInfo> storeReference;

    /**
     * Initializes a new {@link ConfigAwareKeyStore}.
     *
     * @param keystorePathPropertyName The name of the path property for the store
     * @param passwordPropertyName The optional name of the password property for the store
     * @param typePropertyName The optional name of the type property for the store. See <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#KeystoreImplementation">JCA reference guide</a>
     */
    public ConfigAwareKeyStore(String keystorePathPropertyName, String passwordPropertyName, String typePropertyName) {
        super();
        storeReference = new AtomicReference<>(NO_KEYSTORE);
        this.keystorePathPropertyName = keystorePathPropertyName;
        this.passwordPropertyName = passwordPropertyName;
        this.typePropertyName = typePropertyName;
    }


    /**
     * (Re-)loads the {@link KeyStore} held by this instance using given properties.
     *
     * @param properties The {@link Properties} to get the values from
     * @return <code>true</code> if the underlying {@link KeyStore} was (re-)loaded; otherwise <code>false</code>
     * @throws FileNotFoundException If store can't be found
     */
    public boolean reloadStore(Properties properties) throws FileNotFoundException {
        // Get path and password
        String keystorePath = properties.getProperty(keystorePathPropertyName);
        String keystorePassword = properties.getProperty(passwordPropertyName);

        // Check if this instance is used
        if (Strings.isEmpty(keystorePath)) {
            if (NO_KEYSTORE == storeReference.get()) {
                // No store configured
                return false;
            }

            // Store was removed
            storeReference.set(NO_KEYSTORE);
            return true;
        }

        File keyStoreFile = new File(stripPath(keystorePath));
        if (false == keyStoreFile.exists() || false == keyStoreFile.isFile()) {
            throw new FileNotFoundException("The key store does not exist.");
        }

        FileInputStream in = null;
        try {
            String currentMd5Sum = getMd5Sum(keyStoreFile);
            KeyStoreInfo prevStore = storeReference.get();
            if (false == currentMd5Sum.equals(prevStore.md5Sum)) {
                /*
                 * (Re-) Load the key store
                 */
                in = new FileInputStream(keyStoreFile);
                String optKeyStoreType = properties.getProperty(typePropertyName);
                KeyStore store = KeyStore.getInstance(Strings.isEmpty(optKeyStoreType) ? KeyStore.getDefaultType() : optKeyStoreType);
                store.load(in, null == keystorePassword ? null : keystorePassword.toCharArray());
                storeReference.set(new KeyStoreInfo(store, currentMd5Sum));
                return true;
            }
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            FileNotFoundException notFoundException = new FileNotFoundException("Unable to access key store: " + e.getMessage());
            notFoundException.setStackTrace(e.getStackTrace());
            throw notFoundException;
        } finally {
            Streams.close(in);
        }
        return false;
    }

    /**
     * Get a value indicating if the underlying key store is well defined, configured and accessible
     *
     * @return <code>true</code> If the key store is defined, configured and accessible
     *         <code>false</code> otherwise
     */
    public boolean isConfigured() {
        KeyStoreInfo storeInfo = storeReference.get();
        return storeInfo.md5Sum != null && null != storeInfo.store;
    }

    /**
     * Get the key store
     *
     * @return The {@link KeyStore}
     */
    public KeyStore getKeyStore() {
        return storeReference.get().store;
    }

    private String stripPath(String keystorePath) {
        return keystorePath.substring(keystorePath.lastIndexOf(":") + 1, keystorePath.length());
    }

    private static String getMd5Sum(File f) throws FileNotFoundException, IOException, NoSuchAlgorithmException  {
        try (FileInputStream in = new FileInputStream(f)) {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] block = new byte[4096];
            int length = 0;
            while ((length = in.read(block)) != -1) {
                md5.update(block, 0, length);
            }
            return Strings.asHex(md5.digest());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ConfigAwareKeyStore.class.getName());
        KeyStoreInfo storeInfo = storeReference.get();
        sb.append("=[storeHash:").append(storeInfo.md5Sum);
        sb.append(",keystorePathPropertyName:").append(keystorePathPropertyName);
        sb.append(",passwordPropertyName:").append(passwordPropertyName);
        sb.append(",keystore:").append(null != storeInfo.store ? storeInfo.store.toString() : "");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        KeyStoreInfo storeInfo = storeReference.get();
        final int prime = 31;
        int result = 1;
        result = prime * result + (storeInfo == null ? 0 : storeInfo.hashCode());
        result = prime * result + ((passwordPropertyName == null) ? 0 : passwordPropertyName.hashCode());
        result = prime * result + ((keystorePathPropertyName == null) ? 0 : keystorePathPropertyName.hashCode());
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
        KeyStoreInfo storeInfo = storeReference.get();
        KeyStoreInfo otherStoreInfo = other.storeReference.get();
        if (storeInfo == null) {
            if (otherStoreInfo != null) {
                return false;
            }
        } else if (!storeInfo.equals(otherStoreInfo)) {
            return false;
        }
        if (passwordPropertyName == null) {
            if (other.passwordPropertyName != null) {
                return false;
            }
        } else if (!passwordPropertyName.equals(other.passwordPropertyName)) {
            return false;
        }
        if (keystorePathPropertyName == null) {
            if (other.keystorePathPropertyName != null) {
                return false;
            }
        } else if (!keystorePathPropertyName.equals(other.keystorePathPropertyName)) {
            return false;
        }
        return true;
    }

}

