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

package com.openexchange.drive.client.windows.files;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import com.openexchange.java.Streams;

/**
 * {@link AbstractResourceLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractResourceLoader implements ResourceLoader {

    private final Map<String, String> md5Cache = new HashMap<String, String>();


    /**
     * Initializes a new {@link AbstractResourceLoader}.
     *
     * @param fileNamePattern
     */
    public AbstractResourceLoader() {
        super();
    }

    @Override
    public String getMD5(String name) throws IOException {
        if (md5Cache.containsKey(name)) {
            return md5Cache.get(name);
        }
        String retval = calculateMD5(name);
        md5Cache.put(name, retval);
        return retval;
    }

    protected String calculateMD5(String name) throws IOException {
        InputStream inputStream = get(name);
        if (inputStream == null) {
            return null;
        }
        try {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                IOException e1 = new IOException(e.getMessage());
                e1.initCause(e);
                throw e1;
            }
            int length = -1;
            byte[] buf = new byte[4096];
            while ((length = inputStream.read(buf)) != -1) {
                digest.update(buf, 0, length);
            }
            return new String(Hex.encodeHex(digest.digest()));
        } finally {
            Streams.close(inputStream);
        }
    }
}
