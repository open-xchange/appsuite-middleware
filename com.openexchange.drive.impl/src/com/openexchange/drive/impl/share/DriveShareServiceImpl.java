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

package com.openexchange.drive.impl.share;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.share.DriveShareInfo;
import com.openexchange.drive.share.DriveShareService;
import com.openexchange.drive.share.DriveShareTarget;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.CreatedShare;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link DriveShareServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveShareServiceImpl implements DriveShareService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveShareServiceImpl.class);
    private static final int MODULE_ID = FolderObject.INFOSTORE;

    /**
     * Initializes a new {@link DriveShareServiceImpl}.
     */
    public DriveShareServiceImpl() {
        super();
        LOG.debug("initialized.");
    }

    @Override
    public List<DriveShareInfo> getShares(DriveSession session, DriveShareTarget target) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        /*
         * map drive target to plain share target & lookup shares
         */
        ShareTarget shareTarget = getShareTarget(syncSession, target);
        List<ShareInfo> shareInfos = getShareService().getShares(session.getServerSession(), getModule(), shareTarget.getFolder(), shareTarget.getItem());
        /*
         * convert & return appropriate drive share
         */
        List<DriveShareInfo> driveShareInfos = new ArrayList<DriveShareInfo>(shareInfos.size());
        for (ShareInfo shareInfo : shareInfos) {
            DriveShareTarget driveTarget = new DriveShareTarget(shareTarget, target.getPath(), target.getName(), target.getChecksum());
            driveShareInfos.add(new DefaultDriveShareInfo(shareInfo, driveTarget));
        }
        return driveShareInfos;
    }

    @Override
    public DriveShareInfo addShare(DriveSession session, DriveShareTarget target, ShareRecipient recipient, Map<String, Object> meta) throws OXException {
        SyncSession syncSession = new SyncSession(session);
        /*
         * map drive target to plain share target & add the share
         */
        ShareTarget shareTarget = getShareTarget(syncSession, target);
        CreatedShare createdShare = getShareService().addShare(session.getServerSession(), shareTarget, recipient, meta);
        /*
         * convert & return appropriate drive share
         */
        return new DefaultDriveShareInfo(createdShare.getFirstInfo(), target);
    }

    private ShareTarget getShareTarget(SyncSession session, DriveShareTarget driveTarget) throws OXException {
        if (driveTarget.isFolder()) {
            String folderID = session.getStorage().getFolderID(driveTarget.getDrivePath());
            DirectoryChecksum directoryChecksum = ChecksumProvider.getChecksums(session, Collections.singletonList(folderID)).get(0);
            if (false == driveTarget.getChecksum().equals(directoryChecksum.getChecksum())) {
                throw DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.create(driveTarget.getDrivePath(), driveTarget.getChecksum());
            }
            return new ShareTarget(MODULE_ID, folderID);
        } else {
            File file = session.getStorage().getFileByName(driveTarget.getDrivePath(), driveTarget.getName());
            if (null == file) {
                throw DriveExceptionCodes.FILE_NOT_FOUND.create(driveTarget.getName(), driveTarget.getDrivePath());
            }
            if (false == ChecksumProvider.matches(session, file, driveTarget.getChecksum())) {
                throw DriveExceptionCodes.FILEVERSION_NOT_FOUND.create(driveTarget.getName(), driveTarget.getChecksum(), driveTarget.getDrivePath());
            }
            return new ShareTarget(FolderObject.INFOSTORE, file.getFolderId(), file.getId());
        }
    }

    private static String getModule() {
        return DriveServiceLookup.getService(ModuleSupport.class).getShareModule(MODULE_ID);
    }

    private static ShareService getShareService() {
        return DriveServiceLookup.getService(ShareService.class);
    }

}
