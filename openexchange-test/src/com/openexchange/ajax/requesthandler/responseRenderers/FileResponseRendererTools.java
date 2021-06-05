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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link FileResponseRendererTools}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FileResponseRendererTools {

    public static enum Delivery {
        view, download
    };

    public static enum Disposition {
        attachment, inline
    };

    /**
     * @param filename
     * @return
     * @throws IOException
     */
    public static ByteArrayFileHolder getFileHolder(String filename) throws IOException {
        return getFileHolder(readFile(filename), filename);
    }

    /**
     * @param filename
     * @param contentType
     * @param delivery
     * @param disposition
     * @param fname
     * @return
     * @throws IOException
     */
    public static ByteArrayFileHolder getFileHolder(String filename, String contentType, Delivery delivery, Disposition disposition, String fname) throws IOException {
        return getFileHolder(readFile(filename), contentType, delivery, disposition, fname);
    }

    /**
     * @param bytes
     * @param contentType
     * @param delivery
     * @param disposition
     * @param filename
     * @return
     */
    public static ByteArrayFileHolder getFileHolder(byte[] bytes, String contentType, Delivery delivery, Disposition disposition, String filename) {
        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setContentType(contentType);
        fileHolder.setDelivery(delivery.toString());
        fileHolder.setDisposition(disposition.toString());
        fileHolder.setName(filename);
        return fileHolder;
    }

    /**
     * @param bytes
     * @param filename
     * @return
     */
    public static ByteArrayFileHolder getFileHolder(byte[] bytes, String filename) {
        final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
        fileHolder.setName(filename);

        return fileHolder;
    }

    /**
     * Create a new byte array
     * 
     * @param length
     * @return
     */
    public static byte[] newByteArray(int length) {
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

    private static byte[] readFile(String filename) throws IOException {
        String testDataDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR);
        final File file = new File(testDataDir, filename);
        final InputStream is = new FileInputStream(file);
        final byte[] bytes = IOUtils.toByteArray(is);
        return bytes;
    }
}
