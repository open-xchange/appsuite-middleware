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

import java.util.Arrays;
import com.openexchange.file.storage.File;
import com.openexchange.java.Charsets;

/**
 * {@link DirectoryFragment}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryFragment implements Comparable<DirectoryFragment> {

    private final byte[] encodedFileName;
    private final byte[] encodedChecksum;
    private final File file;

    public DirectoryFragment(File file, String checksum) {
        super();
        this.file = file;
        this.encodedFileName = file.getFileName().getBytes(Charsets.UTF_8);
        this.encodedChecksum = checksum.getBytes(Charsets.UTF_8);
    }

    @Override
    public int compareTo(DirectoryFragment other) {
        int minLength = Math.min(encodedFileName.length, other.encodedFileName.length);
        for (int i = 0; i < minLength; i++) {
            int result = encodedFileName[i] - other.encodedFileName[i];
            if (result != 0) {
                return result;
            }
        }
        return encodedFileName.length - other.encodedFileName.length;
    }

    /**
     * Gets the encoded checksum
     *
     * @return The encoded checksum
     */
    public byte[] getEncodedChecksum() {
        return encodedChecksum;
    }

    /**
     * Gets the encoded file name
     *
     * @return The encoded file name
     */
    public byte[] getEncodedFileName() {
        return encodedFileName;
    }

    /**
     * Gets the file
     *
     * @return The file
     */
    public File getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(encodedChecksum);
        result = prime * result + Arrays.hashCode(encodedFileName);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DirectoryFragment)) {
            return false;
        }
        DirectoryFragment other = (DirectoryFragment) obj;
        if (!Arrays.equals(encodedChecksum, other.encodedChecksum)) {
            return false;
        }
        if (!Arrays.equals(encodedFileName, other.encodedFileName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new String(encodedFileName, Charsets.UTF_8) + " - " + new String(encodedChecksum, Charsets.UTF_8);
    }

}
