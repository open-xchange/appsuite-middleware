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

package com.openexchange.multifactor.provider.backupString;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.multifactor.AbstractMultifactorDevice;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.provider.backupString.impl.MultifactorBackupStringProvider;

/**
 * {@link BackupStringMultifactorDevice} - A "backup string" multifactor device based on a hashed secret value.
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class BackupStringMultifactorDevice extends AbstractMultifactorDevice {

    private static final String SHARED_SECRET_PARAMETER = "sharedSecret";
    public static final String BACKUP_STRING_LENGTH_PARAMETER = "backupStringLength";

    private String sharedSecretHash;

    /**
     * Initializes a new {@link BackupStringMultifactorDevice}.
     *
     * @param id The unique ID of the device
     * @param name A user friendly name which describes the device
     */
    public BackupStringMultifactorDevice(String id, String name) {
        super(id, MultifactorBackupStringProvider.NAME, name);
        setBackup(true);
    }

    /**
     * Initializes a new {@link BackupStringMultifactorDevice}.
     *
     * @param id The unique ID of the device
     * @param name A user friendly name which describes the device
     * @param sharedSecretHash The hash of the shared secret to use
     * @param secretLength The length of the origin, unhashed, shared secret
     */
    public BackupStringMultifactorDevice(String id, String name, String sharedSecretHash, int secretLength) {
        super(id, MultifactorBackupStringProvider.NAME, name);
        this.sharedSecretHash = sharedSecretHash;
        setSecretLength(secretLength);
        setBackup(true);
    }

    /**
     * Initializes a new {@link BackupStringMultifactorDevice} on base of the given {@link MultifactorDevice}.
     *
     * @param source The device to create the new device from
     */
    public BackupStringMultifactorDevice(MultifactorDevice source) {
        super(source.getId(),
              source.getProviderName(),
              source.getName(),
              source.getParameters());
        setBackup(source.isBackup());
    }

    /**
     * Convenience method to add shared secret as plaintext to the device's parameter
     *
     * @param sharedSecret the secret to set
     */
    public void setSharedSecret(String sharedSecret) {
        setParameter(SHARED_SECRET_PARAMETER, sharedSecret);
    }

    /**
     * Gets The hashed shared secret
     *
     * @return The hashed shared secret
     */
    public String getHashedSharedSecret() {
       return this.sharedSecretHash;
    }

    /**
     * Gets the original length of the shared secret
     *
     * @return The original length of the unhashed secret
     */
    public int getSecretLength() {
        return this.<Integer> getParameter(BACKUP_STRING_LENGTH_PARAMETER).intValue();
    }

    /**
     * Sets the length of the shared secret
     *
     * @param length The length of the shared secret
     * @return this
     */
    public BackupStringMultifactorDevice setSecretLength(int length) {
        setParameter(BACKUP_STRING_LENGTH_PARAMETER, I(length));
        return this;
    }
}
