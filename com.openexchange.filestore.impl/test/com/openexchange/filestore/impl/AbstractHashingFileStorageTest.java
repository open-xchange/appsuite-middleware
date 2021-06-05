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

package com.openexchange.filestore.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.Before;

/**
 * {@link AbstractHashingFileStorageTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AbstractHashingFileStorageTest {

    protected File tmpFile;
    protected HashingFileStorage fs;

    @SuppressWarnings("unused")
    @Before
    public void setUp() throws Exception {
        tmpFile = new File("/tmp/" + this.getClass().getCanonicalName() + "_" + System.currentTimeMillis());
        tmpFile.mkdirs();
        fs = new HashingFileStorage(tmpFile.toURI(), tmpFile);
    }

    @After
    public void tearDown() throws Exception {
        fs.remove();
        tmpFile.delete();
    }

    @SuppressWarnings("unused")
    protected InputStream IS(String data) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(data.getBytes(com.openexchange.java.Charsets.UTF_8));
    }
}
