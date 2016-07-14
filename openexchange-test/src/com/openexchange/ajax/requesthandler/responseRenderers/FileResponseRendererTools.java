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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.configuration.MailConfig;

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
        String testDataDir = MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR);
        final File file = new File(testDataDir, filename);
        final InputStream is = new FileInputStream(file);
        final byte[] bytes = IOUtils.toByteArray(is);
        return bytes;
    }
}
