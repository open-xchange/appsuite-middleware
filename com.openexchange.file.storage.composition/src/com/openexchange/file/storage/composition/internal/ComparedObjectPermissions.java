/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.ComparedPermissions;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ComparedObjectPermissions}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ComparedObjectPermissions extends ComparedPermissions<FileStorageObjectPermission, FileStorageGuestObjectPermission> {

    private final Session session;
    private final Map<Integer, GuestInfo> guestInfos;

    private ShareService shareService;

    /**
     * Initializes a new {@link ComparedObjectPermissions}.
     *
     * @param session The session
     * @param oldMetadata The old metadata, or <code>null</code> for new documents
     * @param newMetadata The new metadata
     */
    public ComparedObjectPermissions(Session session, File oldMetadata, File newMetadata) throws OXException {
        this(session, null == oldMetadata ? null : oldMetadata.getObjectPermissions(), null == newMetadata ? null : newMetadata.getObjectPermissions());
    }

    /**
     * Initializes a new {@link ComparedObjectPermissions}.
     *
     * @param session The session
     * @param oldPermissions The old object permissions, or <code>null</code> for new documents
     * @param newPermissions The new object permissions
     */
    public ComparedObjectPermissions(Session session, List<FileStorageObjectPermission> oldPermissions, List<FileStorageObjectPermission> newPermissions) throws OXException {
        super(newPermissions, oldPermissions);
        this.session = session;
        guestInfos = new HashMap<>();
        calc();
    }

    @Override
    protected boolean isSystemPermission(FileStorageObjectPermission p) {
        return false;
    }

    @Override
    protected boolean isUnresolvedGuestPermission(FileStorageObjectPermission p) {
        return p instanceof FileStorageGuestObjectPermission;
    }

    @Override
    protected boolean isGroupPermission(FileStorageObjectPermission p) {
        return p.isGroup();
    }

    @Override
    protected int getEntityId(FileStorageObjectPermission p) {
        return p.getEntity();
    }

    @Override
    protected boolean areEqual(FileStorageObjectPermission p1, FileStorageObjectPermission p2) {
        if (p1 == null) {
            if (p2 == null) {
                return true;
            }

            return false;
        }

        if (p2 == null) {
            return false;
        }

        return p1.equals(p2);
    }

    @Override
    protected boolean isGuestUser(int userId) throws OXException {
        return getGuestInfo(userId) != null;
    }

    /**
     * Remembers a guest info. A guest info must be set for every new guest permission,
     * after the according guest entity has been created. Other guest infos (i.e. for added
     * but not new guests) don't need to be set, they will be loaded on-demand when calling {@link #getGuestInfo(int)}.
     *
     * @param guestInfo The guest info
     */
    public void rememberGuestInfo(GuestInfo guestInfo) {
        guestInfos.put(I(guestInfo.getGuestID()), guestInfo);
    }

    /**
     * Gets a guest info for every new, modified or added guest permissions entity.
     *
     * @param guestId The user ID of the guest
     * @return The guest info or <code>null</code> if the passed id does not belong to a guest user
     * @throws OXException If loading the guest info fails
     */
    public GuestInfo getGuestInfo(int guestId) throws OXException {
        GuestInfo guestInfo = guestInfos.get(I(guestId));
        if (guestInfo == null) {
            guestInfo = getShareService().getGuestInfo(session, guestId);
            if (guestInfo == null) {
                guestInfo = NO_GUEST;
            }
            guestInfos.put(I(guestId), guestInfo);
        }

        if (guestInfo == NO_GUEST) {
            return null;
        }

        return guestInfo;
    }

    private ShareService getShareService() throws OXException {
        if (this.shareService != null) {
            return shareService;
        }
        ShareService service = Services.getService(ShareService.class);
        if (service == null) {
            throw ServiceExceptionCode.absentService(ShareService.class);
        }
        return shareService = service;
    }

    private static final GuestInfo NO_GUEST = new GuestInfo() {

        @Override
        public RecipientType getRecipientType() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public int getGuestID() {
            return 0;
        }

        @Override
        public String getEmailAddress() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public int getCreatedBy() {
            return 0;
        }

        @Override
        public int getContextID() {
            return 0;
        }

        @Override
        public String getBaseToken() {
            return null;
        }

        @Override
        public AuthenticationMode getAuthentication() {
            return null;
        }

        @Override
        public ShareTarget getLinkTarget() {
            return null;
        }

        @Override
        public Date getExpiryDate() {
            return null;
        }

        @Override
        public String generateLink(HostData hostData, ShareTargetPath targetPath) {
            return null;
        }
    };

}
