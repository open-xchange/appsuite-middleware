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

package com.openexchange.drive.impl.checksum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;

/**
 * {@link ChecksumSupplier}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class ChecksumSupplier {

    private final SyncSession session;
    private final int view;
    private final boolean useDriveMeta;
    private final List<List<String>> folderIdChunks;
    private final Map<String, DirectoryChecksum> knownChecksums;

    private int chunkIndex;

    /**
     * Initializes a new {@link ChecksumSupplier}.
     *
     * @param session The sync session
     * @param folderIds The list of folder IDs to get the checksum for
     * @param chunkSize The number of directory versions to pre-fetch per chunk
     */
    public ChecksumSupplier(SyncSession session, List<String> folderIds, int chunkSize) throws OXException {
        this(session, folderIds, DriveUtils.calculateView(session.getDriveSession()), session.getDriveSession().useDriveMeta(), chunkSize);
    }

    /**
     * Initializes a new {@link ChecksumSupplier}.
     *
     * @param session The sync session
     * @param folderIds The list of folder IDs to get the checksum for
     * @param view The client view to use when determining the directory checksums
     * @param <code>true</code> to consider <code>.drive-meta</code> files, <code>false</code>, otherwise
     * @param chunkSize The number of directory versions to pre-fetch per chunk
     */
    public ChecksumSupplier(SyncSession session, List<String> folderIds, int view, boolean useDriveMeta, int chunkSize) throws OXException {
        super();
        this.view = view;
        this.session = session;
        this.useDriveMeta = useDriveMeta;
        this.knownChecksums = new HashMap<String, DirectoryChecksum>();
        this.folderIdChunks = Lists.partition(folderIds, chunkSize);
        this.chunkIndex = 0;
        prefetchChecksums();
    }

    /**
     * Gets the checksum for a specific folder. Unless already prefetched, the underlying {@link ChecksumProvider} is consulted and the
     * next batch of directory checksums is retrieved.
     *
     * @param folderID The identifier of the folder to get the checksum for
     * @return The directory checksum
     */
    public DirectoryChecksum getChecksum(String folderID) throws OXException {
        DirectoryChecksum checksum = knownChecksums.get(folderID);
        while (null == checksum && prefetchChecksums()) {
            checksum = knownChecksums.get(folderID);
        }
        if (null == checksum) {
            throw DriveExceptionCodes.NO_CHECKSUM_FOR_DIRECTORY.create(folderID);
        }
        return checksum;
    }

    /**
     * Optionally gets the checksum for a specific folder, if it has already been prefetched.
     *
     * @param folderID The identifier of the folder to get the checksum for
     * @return The directory checksum, or <code>null</code> if not yet available
     */
    public DirectoryChecksum optChecksum(String folderID) {
        return knownChecksums.get(folderID);
    }

    private boolean prefetchChecksums() throws OXException {
        List<String> idChunk = nextFolderIdChunk();
        if (null != idChunk && 0 < idChunk.size()) {
            session.trace("Prefetching next chunk of " + idChunk.size() + " checksums.");
            List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, idChunk, view, useDriveMeta);
            for (int i = 0; i < idChunk.size(); i++) {
                knownChecksums.put(idChunk.get(i), checksums.get(i));
            }
            return true;
        }
        return false;
    }

    private List<String> nextFolderIdChunk() {
        if (chunkIndex < folderIdChunks.size()) {
            return folderIdChunks.get(chunkIndex++);
        }
        return null;
    }

}
