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

package com.openexchange.charset;

import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;

/**
 * {@link CustomCharsetProviderInit} - Initialization for custom charset provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomCharsetProviderInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomCharsetProviderInit.class);

    /**
     * Initializes a new {@link CustomCharsetProviderInit}.
     */
    public CustomCharsetProviderInit() {
        super();
    }

    @Override
    public void start() throws OXException {
        CustomCharsetProvider.initCharsetMap();
        final CustomCharsetProvider provider = new CustomCharsetProvider();
        /*
         * Add alias charsets
         */
        provider.addAliasCharset("BIG5", "BIG-5", "BIG_5");
        provider.addAliasCharset("UTF-8", "UTF_8", "iso-UTF-8");
        // provider.addAliasCharset("US-ASCII", "x-unknown");
        provider.addAliasCharset("ISO-8859-1", "ISO", "x-unknown");
        provider.addAliasCharset("MacRoman", "MACINTOSH");
        provider.addAliasCharset("Shift_JIS", "shift-jis");
        /*
         * Add starts-with charsets
         */
        provider.addStartsWithCharset("ISO-8859-1", "ISO-8859-1");
        LOG.info("Custom charsets successfully added to alias charset provider.");
    }

    @Override
    public void stop() throws OXException {
        CustomCharsetProvider.releaseCharsetMap();
        LOG.info("Custom charset provider successfully dropped.");
    }
}
