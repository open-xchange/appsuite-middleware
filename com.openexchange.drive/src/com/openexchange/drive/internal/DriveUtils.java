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

package com.openexchange.drive.internal;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.ErrorFileAction;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.sync.RenameTools;
import com.openexchange.drive.sync.SimpleFileVersion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.quota.QuotaExceptionCodes;

/**
 * {@link DriveUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveUtils {

    /**
     * Constructs the identifier for the supplied file, setting all relevant properties in the {@link FileID} object, especially the
     * folder ID field is populated with the (non-unique) identifier of the parent folder if not already done by the {@link FileID}
     * constructor.
     *
     * @param file The file to generate the ID for
     * @return The file ID
     * @throws IllegalArgumentException if the supplied file lacks the required properties
     */
    public static FileID getFileID(File file) {
        if (null == file.getId() || null == file.getFolderId()) {
            throw new IllegalArgumentException("File- and folder IDs  are required");
        }
        FileID fileID = new FileID(file.getId());
        FolderID folderID = new FolderID(file.getFolderId());
        if (null == fileID.getFolderId()) {
            fileID.setFolderId(folderID.getFolderId());
        }
        return fileID;
    }

    /**
     * Handles a 'quota-exceeded' situation that happened when a new file version was saved by generating an appropriate sequence of
     * actions to inform the client and instruct him to put the affected file into quarantine.
     *
     * @param session The sync session
     * @param quotaException The quota exception that occurred
     * @param path The path where the new file version was tried to be saved
     * @param originalVersion The original version if it was an update to an existing file, or <code>null</code>, otherwise
     * @param newVersion The new file version that was tried to be saved
     * @return A sequence of file actions the client should execute in order to handle the 'quota-exceeded' situation
     * @throws OXException
     */
    public static List<AbstractAction<FileVersion>> handleQuotaExceeded(SyncSession session, OXException quotaException, String path,
        FileVersion originalVersion, FileVersion newVersion) throws OXException {
        List<AbstractAction<FileVersion>> actionsForClient = new ArrayList<AbstractAction<FileVersion>>();
        /*
         * quota reached
         */
        OXException quotaReachedException = DriveExceptionCodes.QUOTA_REACHED.create(quotaException, (Object[])null);
        if (null != originalVersion) {
            /*
             * upload should have replaced an existing file, let client first rename it's file and mark as error with quarantine flag...
             */
            String alternativeName = RenameTools.findRandomAlternativeName(originalVersion.getName());
            FileVersion renamedVersion = new SimpleFileVersion(alternativeName, originalVersion.getChecksum());
            actionsForClient.add(new EditFileAction(newVersion, renamedVersion, null, path, false));
            actionsForClient.add(new ErrorFileAction(newVersion, renamedVersion, null, path, quotaReachedException, true));
            /*
             * ... then download the server version afterwards
             */
            ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(originalVersion, path, session);
            actionsForClient.add(new DownloadFileAction(session, null, serverFileVersion, null, path));
        } else {
            /*
             * upload of new file, mark as error with quarantine flag
             */
            actionsForClient.add(new ErrorFileAction(null, newVersion, null, path, quotaReachedException, true));
        }
        return actionsForClient;
    }

    /**
     * Gets a value indicating whether the supplied exception indicates a 'quota-exceeded' exception or not.
     *
     * @param e The exception to check
     * @return <code>true</code> if the exception indicates a 'quota-exceeded' exception, <code>false</code>, otherwise
     */
    public static boolean indicatesQuotaExceeded(OXException e) {
        return "FLS-0024".equals(e.getErrorCode()) || FileStorageExceptionCodes.QUOTA_REACHED.equals(e) ||
            DriveExceptionCodes.QUOTA_REACHED.equals(e) || "SMARTDRIVEFILE_STORAGE-0008".equals(e.getErrorCode()) ||
            QuotaExceptionCodes.QUOTA_EXCEEDED.equals(e) || QuotaExceptionCodes.QUOTA_EXCEEDED_FILES.equals(e)
        ;
    }

    private DriveUtils() {
        super();
    }

}
