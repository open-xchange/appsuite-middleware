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

package com.openexchange.crypto;

import java.io.InputStream;
import java.security.Key;
import com.openexchange.exception.OXException;

/**
 * {@link DoNothingCryptoService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DoNothingCryptoService implements CryptoService {

    @Override
    public String decrypt(String encryptedPayload, String password) throws OXException {
        return encryptedPayload;
    }

    @Override
    public String decrypt(EncryptedData data, String password, boolean useSalt) throws OXException {
        return data.toString();
    }

    @Override
    public String decrypt(String encryptedPayload, Key key) throws OXException {
        return encryptedPayload;
    }

    @Override
    public String encrypt(String data, String password) throws OXException {
        return data;
    }

    @Override
    public EncryptedData encrypt(String data, String password, boolean useSalt) throws OXException {
        return null;
    }

    @Override
    public String encrypt(String data, Key key) throws OXException {
        return data;
    }

    @Override
    public InputStream encryptingStreamFor(InputStream in, Key key) throws OXException {
        return in;
    }

    @Override
    public InputStream decryptingStreamFor(InputStream in, Key key) throws OXException {
        return in;
    }

}
