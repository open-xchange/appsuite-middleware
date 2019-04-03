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

package com.openexchange.drive.events.apn;

import java.util.Arrays;

/**
 * {@link APNAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNAccess {

    private final String password;
    private final String stringKeystore;
    private final byte[] bytesKeystore;
    private final boolean production;
    private int hash = 0;

    /**
     * Initializes a new {@link APNAccess}.
     *
     * @param keystore A keystore containing the private key and the certificate signed by Apple. <p/>
     *                 The following formats can be used:
     *                 <ul>
     *                 <li><code>byte[]</code></li>
     *                 <li><code>java.lang.String</code> for a file path</li>
     *                 </ul>
     * @param password The keystore's password.
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     */
    public APNAccess(String keystore, String password, boolean production) {
        super();
        this.stringKeystore = keystore;
        this.bytesKeystore = null;
        this.password = password;
        this.production = production;
    }

    /**
     * Initializes a new {@link APNAccess}.
     *
     * @param keystore A keystore containing the private key and the certificate signed by Apple. <p/>
     *                 The following formats can be used:
     *                 <ul>
     *                 <li><code>byte[]</code></li>
     *                 <li><code>java.lang.String</code> for a file path</li>
     *                 </ul>
     * @param password The keystore's password.
     * @param production <code>true</code> to use Apple's production servers, <code>false</code> to use the sandbox servers
     */
    public APNAccess(byte[] keystore, String password, boolean production) {
        super();
        this.stringKeystore = null;
        this.bytesKeystore = keystore;
        this.password = password;
        this.production = production;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the keystore
     *
     * @return The keystore
     */
    public Object getKeystore() {
        return null == stringKeystore ? bytesKeystore : stringKeystore;
    }

    /**
     * Gets the production
     *
     * @return The production
     */
    public boolean isProduction() {
        return production;
    }

    @Override
    public int hashCode() {
        int result = hash; // Does not need to be thread-safe
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + (production ? 1231 : 1237);
            result = prime * result + ((password == null) ? 0 : password.hashCode());
            result = prime * result + ((stringKeystore == null) ? 0 : stringKeystore.hashCode());
            result = prime * result + Arrays.hashCode(bytesKeystore);
            this.hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof APNAccess)) {
            return false;
        }
        APNAccess other = (APNAccess) obj;
        if (production != other.production) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (stringKeystore == null) {
            if (other.stringKeystore != null) {
                return false;
            }
        } else if (!stringKeystore.equals(other.stringKeystore)) {
            return false;
        }
        if (!Arrays.equals(bytesKeystore, other.bytesKeystore)) {
            return false;
        }
        return true;
    }

}
