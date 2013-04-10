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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import jonelo.jacksum.algorithm.MD;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link ChecksumProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ChecksumProvider {

    public static String getMD5(DriveSession session, List<File> files) throws OXException {
        if (null == files || 0 == files.size()) {
            return DriveConstants.EMPTY_MD5;
        }
        try {
            MD md5 = new MD("MD5");
            for (File file : files) {
                if (null != file.getFileName()) {
                    md5.update(file.getFileName().getBytes(Charsets.UTF_8));
                    md5.update(getMD5(session, file).getBytes(Charsets.UTF_8));
                }
            }
            return md5.getFormattedValue();
        } catch (NoSuchAlgorithmException e) {
            throw new OXException(e);
        }
    }

    public static String getMD5(DriveSession session, File file) throws OXException {
        /*
         * try available metadata first
         */
        String md5sum = file.getFileMD5Sum();
        if (null == md5sum) {
            /*
             * query checksum store
             */
            md5sum = session.getChecksumStore().getChecksum(session.getServerSession(), file);
            if (null == md5sum) {
                /*
                 * calculate and store checksum
                 */
                InputStream document = null;
                try {
                    document = session.getStorage().getDocument(file);
                    md5sum = getMD5(document);
                } catch (IOException e) {
                    throw DriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
                } finally {
                    Streams.close(document);
                }
                if (null != md5sum) {
                    session.getChecksumStore().addChecksum(session.getServerSession(), file, md5sum);
                }
            }
        }
        return md5sum;
    }

    public static void invalidateChecksums(DriveSession session, File file) throws OXException {
        session.getChecksumStore().removeChecksums(session.getServerSession(), file);
    }

    public static String getMD5(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        try {
            MD md5 = new MD("MD5");
            int read;
            do {
                read = inputStream.read(buffer);
                if (0 < read) {
                    md5.update(buffer, 0, read);
                }
            } while (-1 != read);
            return md5.getFormattedValue();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

}

