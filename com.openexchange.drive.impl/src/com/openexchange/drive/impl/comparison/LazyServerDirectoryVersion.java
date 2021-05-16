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

package com.openexchange.drive.impl.comparison;

import com.openexchange.drive.impl.checksum.ChecksumSupplier;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.exception.OXException;

/**
 * {@link LazyServerDirectoryVersion}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class LazyServerDirectoryVersion extends ServerDirectoryVersion {

    private final String normalizedPath;
    private final String folderID;
    private final ChecksumSupplier checksumSupplier;

    private DirectoryChecksum knownChecksum;

    /**
     * Initializes a new {@link LazyServerDirectoryVersion}.
     *
     * @param path The path
     * @param folderID The folder identifier
     * @param checksumSupplier The checksum supplier
     */
    public LazyServerDirectoryVersion(String path, String folderID, ChecksumSupplier checksumSupplier) {
        super(path, null);
        this.normalizedPath = PathNormalizer.normalize(path);
        this.folderID = folderID;
        this.checksumSupplier = checksumSupplier;
    }

    @Override
    public String getChecksum() {
        return getDirectoryChecksum().getChecksum();
    }

    @Override
    public String getPath() {
        return normalizedPath;
    }

    @Override
    public DirectoryChecksum getDirectoryChecksum() {
        if (null == knownChecksum) {
            try {
                knownChecksum = checksumSupplier.getChecksum(folderID);
            } catch (OXException e) {
                throw new IllegalStateException(e);
            }
        }
        return knownChecksum;
    }

    /**
     * Optionally gets the known directory checksum, if already available.
     *
     * @return The directory checksum, or <code>null</code> if not yet retrieved
     */
    public DirectoryChecksum optDirectoryChecksum() {
        if (null == knownChecksum) {
            knownChecksum = checksumSupplier.optChecksum(folderID);
        }
        return knownChecksum;
    }

    @Override
    public String toString() {
        return getPath() + " | " + (null != knownChecksum ? knownChecksum.getChecksum() : "<pending>") + " [" + folderID + ']';
    }

}
