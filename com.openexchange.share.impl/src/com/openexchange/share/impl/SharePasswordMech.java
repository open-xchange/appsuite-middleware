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

package com.openexchange.share.impl;

import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
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

    /**
     * Initializes a new {@link SharePasswordMech}.
     *
     * @param cryptoService The underlying crypto service
     * @param cryptKey The key use to encrypt / decrypt data
     */
    public SharePasswordMech(CryptoService cryptoService, String cryptKey) {
        super(ShareConstants.PASSWORD_MECH_ID, 11); //FIXME
        this.cryptoService = cryptoService;
        this.cryptKey = cryptKey;
    }

    @Override
    public String getIdentifier() {
        return ShareConstants.PASSWORD_MECH_ID;
    }

    @Override
    public PasswordDetails encodePassword(String str) throws OXException {
        String encrypt = cryptoService.encrypt(str, cryptKey);
        return new PasswordDetails(str, encrypt, ShareConstants.PASSWORD_MECH_ID, null);
    }

    @Override
    public String decode(String encodedPassword, byte[] salt) throws OXException {
        return cryptoService.decrypt(encodedPassword, cryptKey);
    }

    @Override
    public boolean isExposed() {
        return false;
    }

    @Override
    public boolean checkPassword(String candidate, String encoded, byte[] salt) throws OXException {
        String decoded = decode(encoded, null);
        if (candidate.equals(decoded)) {
            return true;
        }
        return false;
    }
}
