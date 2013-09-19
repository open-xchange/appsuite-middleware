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

package com.openexchange.drive.actions;

import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.comparison.ServerFileVersion;
import com.openexchange.drive.comparison.ThreeWayComparison;
import com.openexchange.drive.internal.SyncSession;
import com.openexchange.file.storage.File;

/**
 * {@link DownloadFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DownloadFileAction extends AbstractFileAction {

    public DownloadFileAction(SyncSession session, FileVersion file, ServerFileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path) {
        this(session, file, newFile, comparison, path, null != newFile ? newFile.getFile() : null);
    }

    public DownloadFileAction(SyncSession session, FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, File serverFile) {
        super(file, newFile, comparison);
        parameters.put(PARAMETER_PATH, path);
        if (null != serverFile) {
            /*
             * add default metadata
             */
            parameters.put(PARAMETER_TOTAL_LENGTH, Long.valueOf(serverFile.getFileSize()));
            if (null != serverFile.getFileMIMEType()) {
                parameters.put(PARAMETER_CONTENT_TYPE, serverFile.getFileMIMEType());
            }
            if (null != serverFile.getCreated()) {
                parameters.put(PARAMETER_CREATED, Long.valueOf(serverFile.getCreated().getTime()));
            }
            if (null != serverFile.getLastModified()) {
                parameters.put(PARAMETER_MODIFIED, Long.valueOf(serverFile.getLastModified().getTime()));
            }
            /*
             * add additional metadata
             */
            List<DriveFileField> fields = session.getFields();
            if (null != fields) {
                if (fields.contains(DriveFileField.DIRECT_LINK)) {
                    parameters.put(PARAMETER_DIRECT_LINK, session.getLinkGenerator().getFileLink(serverFile));
                }
                if (fields.contains(DriveFileField.DIRECT_LINK_FRAGMENTS)) {
                    parameters.put(PARAMETER_DIRECT_LINK_FRAGMENTS, session.getLinkGenerator().getFileLinkFragments(serverFile));
                }
                if (fields.contains(DriveFileField.THUMBNAIL_LINK)) {
                    parameters.put(PARAMETER_THUMBNAIL_LINK, session.getLinkGenerator().getFileThumbnailLink(serverFile));
                }
                if (fields.contains(DriveFileField.PREVIEW_LINK)) {
                    parameters.put(PARAMETER_PREVIEW_LINK, session.getLinkGenerator().getFilePreviewLink(serverFile));
                }
            }
        }
    }

    @Override
    public Action getAction() {
        return Action.DOWNLOAD;
    }

}
