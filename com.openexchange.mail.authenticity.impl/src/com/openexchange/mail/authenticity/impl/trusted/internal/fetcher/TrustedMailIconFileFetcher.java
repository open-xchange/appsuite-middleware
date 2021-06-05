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

package com.openexchange.mail.authenticity.impl.trusted.internal.fetcher;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TrustedMailIconFileFetcher}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TrustedMailIconFileFetcher extends AbstractTrustedMailIconFetcher implements TrustedMailIconFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedMailIconFileFetcher.class);

    /**
     * Initialises a new {@link TrustedMailIconFileFetcher}.
     */
    public TrustedMailIconFileFetcher() {
        super();
    }

    @Override
    public boolean exists(String resourceUrl) {
        try {
            File f = new File(resourceUrl);
            return f.exists() && !f.isDirectory();
        } catch (SecurityException e) {
            LOG.error("No permission to read the resource URL '{}': {}", resourceUrl, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public byte[] fetch(String url) {
        try {
            return process(ImageIO.read(new File(url)));
        } catch (IOException e) {
            LOG.error("An I/O error occurred while reading the resource URL '{}': {}", url, e.getMessage(), e);
        }
        return null;
    }
}
