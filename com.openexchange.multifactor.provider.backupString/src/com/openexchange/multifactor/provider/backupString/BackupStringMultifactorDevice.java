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

package com.openexchange.multifactor.provider.backupString;

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
        return getParameter(BACKUP_STRING_LENGTH_PARAMETER);
    }

    /**
     * Sets the length of the shared secret
     *
     * @param length The length of the shared secret
     * @return this
     */
    public BackupStringMultifactorDevice setSecretLength(int length) {
        setParameter(BACKUP_STRING_LENGTH_PARAMETER, length);
        return this;
    }
}
