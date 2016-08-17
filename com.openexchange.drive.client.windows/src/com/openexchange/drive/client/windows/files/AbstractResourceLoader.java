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
