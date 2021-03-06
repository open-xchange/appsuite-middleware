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

package com.openexchange.pgp.keys.parsing.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import com.openexchange.pgp.keys.parsing.KeyRingParserResult;
import com.openexchange.pgp.keys.parsing.PGPKeyRingParser;

/**
 * {@link PGPKeyParserImpl} - default impl. of the {@link PGPKeyRingParser} service.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPKeyParserImpl implements PGPKeyRingParser {

    private final KeyParser parser;

    /**
     * Initializes a new {@link PGPKeyParserImpl}.
     */
    public PGPKeyParserImpl(KeyParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser must not be null");
    }

    @Override
    public KeyRingParserResult parse(InputStream inputStream) throws IOException {
        KeyRingParserResult result = parser.parse(inputStream);
        return result;
    }

    @Override
    public KeyRingParserResult parse(String data) throws IOException {
        KeyRingParserResult result = parser.parse(data);
        return result;
    }
}
