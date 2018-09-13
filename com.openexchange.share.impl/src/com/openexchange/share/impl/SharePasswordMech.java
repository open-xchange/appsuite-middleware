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

package com.openexchange.share.impl;

import java.util.Base64;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.crypto.EncryptedData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.password.mechanism.AbstractPasswordMech;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.share.core.ShareConstants;

/**
 * {@link SharePasswordMech}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class SharePasswordMech extends AbstractPasswordMech {

    private final CryptoService cryptoService;
    private final String cryptKey;
    private final ConfigurationService configurationService;

    /**
     * Initializes a new {@link SharePasswordMech}.
     * 
     * @param configurationService The configuration service
     * @param cryptoService The underlying crypto service
     * @param cryptKey The key use to encrypt / decrypt data
     */
    public SharePasswordMech(ConfigurationService configurationService, CryptoService cryptoService, String cryptKey) {
        super(ShareConstants.PASSWORD_MECH_ID);
        this.configurationService = configurationService;
        this.cryptoService = cryptoService;
        this.cryptKey = cryptKey;
    }

    @Override
    public PasswordDetails encode(String str) throws OXException {
        if (doSalt()) {
            EncryptedData encryptedData = cryptoService.encrypt(str, cryptKey, getSalt());
            return new PasswordDetails(str, encryptedData.getData(), getIdentifier(), Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData.getSalt()));
        }
        return new PasswordDetails(str, cryptoService.encrypt(str, cryptKey), getIdentifier(), null);
    }

    //FIXME REMOVE THIS OPTION WHEN SALT IS DEFAULT
    private static final String COM_OPENEXCHANGE_PASSWORD_MECHANISM_SALT_ENABLED = "com.openexchange.password.mechanism.salt.enabled";

    protected boolean doSalt() {
        return configurationService.getBoolProperty(COM_OPENEXCHANGE_PASSWORD_MECHANISM_SALT_ENABLED, false);
    }

    @Override
    public boolean checkPassword(String toCheck, String encoded, String salt) throws OXException {
        if ((Strings.isEmpty(toCheck)) && (Strings.isEmpty(encoded))) {
            return true;
        } else if ((Strings.isEmpty(toCheck)) && (Strings.isNotEmpty(encoded))) {
            return false;
        } else if ((Strings.isNotEmpty(toCheck)) && (Strings.isEmpty(encoded))) {
            return false;
        }

        String decoded = decode(encoded, salt);
        if (toCheck.equals(decoded)) {
            return true;
        }
        return false;
    }

    @Override
    public String decode(String encodedPassword, String salt) throws OXException {
        if (Strings.isEmpty(salt) ) {
            return cryptoService.decrypt(encodedPassword, cryptKey);
        }
        return cryptoService.decrypt(new EncryptedData(cryptKey, salt.getBytes()), encodedPassword, true);
    }

    @Override
    public int getHashLength() {
        return 16;
    }
}
