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

package com.openexchange.drive.impl.internal;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveUtility;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.metadata.JsonDirectoryMetadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.Collators;
import com.openexchange.session.Session;

/**
 * {@link DriveUtilityImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveUtilityImpl implements DriveUtility {

    private static final DriveUtility instance = new DriveUtilityImpl();

    /**
     * Gets the drive utility instance.
     *
     * @return The drive utility instance
     */
    public static DriveUtility getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link DriveUtilityImpl}.
     */
    private DriveUtilityImpl() {
        super();
    }

    @Override
    public boolean isInvalidPath(String path) throws OXException {
        return DriveUtils.isInvalidPath(path);
    }

    @Override
    public boolean isInvalidFileName(String fileName) {
        return DriveUtils.isInvalidFileName(fileName);
    }

    @Override
    public boolean isIgnoredFileName(String fileName) {
        return DriveUtils.isIgnoredFileName(fileName);
    }

    @Override
    public boolean isIgnoredFileName(DriveSession session, String path, String fileName) throws OXException {
        return DriveUtils.isIgnoredFileName(session, path, fileName);
    }

    @Override
    public boolean isDriveSession(Session session) {
        return DriveUtils.isDriveSession(session);
    }

    @Override
    public List<JSONObject> getSubfolderMetadata(DriveSession session) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        Map<String, FileStorageFolder> subfolders = syncSession.getStorage().getSubfolders(DriveConstants.ROOT_PATH);
        if (null == subfolders || 0 == subfolders.size()) {
            return Collections.emptyList();
        }
        List<JSONObject> metadata = new ArrayList<JSONObject>();
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>(subfolders.values());
        if (1 < folders.size()) {
            Collections.sort(folders, new FolderComparator(session.getLocale()));
        }
        for (FileStorageFolder subfolder : folders) {
            metadata.add(new JsonDirectoryMetadata(syncSession, subfolder).build(false));
        }
        return metadata;
    }
    
    private static final class FolderComparator implements Comparator<FileStorageFolder> {

        private final Collator collator;

        /**
         * Initializes a new {@link FolderComparator}.
         * 
         * @param locale The locale to use, or <code>null</code> to fall back to the default locale
         */
        public FolderComparator(Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(null == locale ? Locale.US : locale);
        }

        @Override
        public int compare(FileStorageFolder folder1, FileStorageFolder folder2) {
            return collator.compare(folder1.getName(), folder2.getName());
        }
    }

}
