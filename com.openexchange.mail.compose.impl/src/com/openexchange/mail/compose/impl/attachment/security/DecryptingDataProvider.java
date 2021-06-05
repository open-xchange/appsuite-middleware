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

package com.openexchange.mail.compose.impl.attachment.security;

import java.io.InputStream;
import java.security.Key;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CryptoUtility;
import com.openexchange.mail.compose.DataProvider;

/**
 * {@link DecryptingDataProvider} - A decrypting data provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class DecryptingDataProvider implements DataProvider {

    private final DataProvider dataProvider;
    private final Key key;
    private final CryptoService cryptoService;

    /**
     * Initializes a new {@link DecryptingDataProvider}.
     *
     * @param dataProvider The delegate data provider
     */
    public DecryptingDataProvider(DataProvider dataProvider, Key key, CryptoService cryptoService) {
        super();
        this.dataProvider = dataProvider;
        this.key = key;
        this.cryptoService = cryptoService;
    }

    @Override
    public InputStream getData() throws OXException {
        InputStream data = dataProvider.getData();
        return CryptoUtility.decryptingStreamFor(data, key, cryptoService);
    }

}
