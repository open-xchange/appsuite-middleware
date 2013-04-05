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

package com.openexchange.drive.sync;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AcknowledgeFileAction;
import com.openexchange.drive.actions.DownloadFileAction;
import com.openexchange.drive.actions.EditFileAction;
import com.openexchange.drive.actions.RemoveFileAction;
import com.openexchange.drive.actions.UploadFileAction;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.drive.internal.UploadHelper;
import com.openexchange.exception.OXException;


/**
 * {@link FileSynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileSynchronizer extends Synchronizer<FileVersion> {

    private Set<String> usedFilenames;
    private final String path;

    public FileSynchronizer(DriveSession session, VersionMapper<FileVersion> mapper, String path) throws OXException {
        super(session, mapper);
        this.path = path;
    }

    @Override
    public SyncResult<FileVersion> sync() throws OXException {
        usedFilenames = new HashSet<String>(mapper.getKeys());
        return super.sync();
    }

    @Override
    protected void processServerChange(SyncResult<FileVersion> result, Change serverChange, FileVersion originalVersion, FileVersion clientVersion, FileVersion serverVersion) {
        switch (serverChange) {
        case DELETED:
            /*
             * deleted on server, delete file on client, too
             */
            result.addActionForClient(new RemoveFileAction(clientVersion));
            break;
        case MODIFIED:
        case NEW:
            /*
             * new/modified on server, let client download the file
             */
            result.addActionForClient(new DownloadFileAction(clientVersion, serverVersion, path, ((ServerFileVersion)serverVersion).getFile().getFileSize()));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processClientChange(SyncResult<FileVersion> result, Change clientChange, FileVersion originalVersion, FileVersion clientVersion, FileVersion serverVersion) throws OXException {
        switch (clientChange) {
        case DELETED:
            /*
             * deleted on client, delete on server, too, let client remove it's metadata
             */
            result.addActionForServer(new RemoveFileAction(serverVersion));
            result.addActionForClient(new AcknowledgeFileAction(originalVersion, null));
            break;
        case MODIFIED:
        case NEW:
            /*
             * new/modified on client, let client upload the file
             */
            result.addActionForClient(new UploadFileAction(serverVersion, clientVersion, path, getUploadOffset(path, clientVersion)));
            break;
        default:
            break;
        }
    }

    @Override
    protected void processConflictingChange(SyncResult<FileVersion> result, Change clientChange, Change serverChange, FileVersion originalVersion, FileVersion clientVersion, FileVersion serverVersion) throws OXException {
        if (Change.DELETED == serverChange && Change.DELETED == clientChange) {
            /*
             * both deleted, just let client remove it's metadata
             */
            result.addActionForClient(new AcknowledgeFileAction(originalVersion, null));
        } else if ((Change.NEW == clientChange || Change.MODIFIED == clientChange) &&
            (Change.NEW == serverChange || Change.MODIFIED == serverChange)) {
            /*
             * name clash for new/modified files, check file equivalence
             */
            if (Change.NONE.equals(Change.get(clientVersion, serverVersion))) {
                /*
                 * same file version, let client update it's metadata
                 */
                result.addActionForClient(new AcknowledgeFileAction(originalVersion, clientVersion));
            } else {
                /*
                 * keep both client- and server versions, let client first rename it's file...
                 */
                FileVersion renamedVersion = getRenamedVersion(clientVersion, usedFilenames);
                result.addActionForClient(new EditFileAction(clientVersion, renamedVersion));
                /*
                 * ... then upload it, and download the server version afterwards
                 */
                result.addActionForClient(new UploadFileAction(null, renamedVersion, path, getUploadOffset(path, renamedVersion)));
                result.addActionForClient(new DownloadFileAction(null, serverVersion, path, ((ServerFileVersion)serverVersion).getFile().getFileSize()));
            }
        } else if (Change.DELETED == clientChange && (Change.MODIFIED == serverChange || Change.NEW == serverChange)) {
            /*
             * delete-edit conflict, let client download server version
             */
            result.addActionForClient(new DownloadFileAction(null, serverVersion, path, ((ServerFileVersion)serverVersion).getFile().getFileSize()));
        } else if ((Change.NEW == clientChange || Change.MODIFIED == clientChange) && Change.DELETED == serverChange) {
            /*
             * edit-delete conflict, let client upload it's file
             */
            result.addActionForClient(new UploadFileAction(null, clientVersion, path, getUploadOffset(path, clientVersion)));
        } else {
            throw new UnsupportedOperationException("Not implemented: Server: " + serverChange + ", Client: " + clientChange);
        }
    }

    private static FileVersion getRenamedVersion(final FileVersion conflictingVersion, Set<String> usedFilenames) {
        final String alternativeName = findAlternativeName(conflictingVersion.getName(), usedFilenames);
        usedFilenames.add(alternativeName);
        return new FileVersion() {

            @Override
            public String getName() {
                return alternativeName;
            }

            @Override
            public String getChecksum() {
                return conflictingVersion.getChecksum();
            }
        };
    }

    protected static String findAlternativeName(String conflictingName, Set<String> usedFilenames) {
        int extensionIndex = conflictingName.lastIndexOf('.');
        String fileName, fileExtension;
        if (-1 == extensionIndex) {
            fileName = conflictingName;
            fileExtension = "";
        } else {
            fileName = conflictingName.substring(0, extensionIndex);
            fileExtension = conflictingName.substring(extensionIndex);
        }
        Pattern regex = Pattern.compile("\\((\\d+)\\)\\z");
        String alternativeName;
        do {
            Matcher matcher = regex.matcher(fileName);
            if (false == matcher.find()) {
                /*
                 * append new initial sequence number
                 */
                fileName += " (1)";
            } else {
                /*
                 * incremented existing sequence number
                 */
                int number = 0;
                try {
                    number = Integer.valueOf(matcher.group(1)).intValue();
                } catch (NumberFormatException e) {
                    // should not get here
                }
                fileName = fileName.substring(0, matcher.start()) + '(' + String.valueOf(number) + ')';
            }
            alternativeName = fileName + fileExtension;
        } while (usedFilenames.contains(alternativeName));
        return alternativeName;
    }

    private long getUploadOffset(String path, FileVersion fileVersion) throws OXException {
        return new UploadHelper(session).getUploadOffset(fileVersion);
    }

}
