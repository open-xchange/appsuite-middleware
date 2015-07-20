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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.file.storage.composition.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.ComparedPermissions;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ComparedObjectPermissions}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ComparedObjectPermissions extends ComparedPermissions<FileStorageObjectPermission, FileStorageGuestObjectPermission> {

    private final ShareService shareService;

    private final int contextId;

    private final Map<Integer, GuestInfo> guestInfos;

    /**
     * Initializes a new {@link ComparedObjectPermissions}.
     *
     * @param contextId The context ID
     * @param oldMetadata The old metadata, or <code>null</code> for new documents
     * @param newMetadata The new metadata
     * @throws OXException
     */
    public ComparedObjectPermissions(int contextId, File oldMetadata, File newMetadata) throws OXException {
        this(contextId, null == oldMetadata ? null: oldMetadata.getObjectPermissions(), null == newMetadata ? null : newMetadata.getObjectPermissions());
    }

    /**
     * Initializes a new {@link ComparedObjectPermissions}.
     *
     * @param contextId The context ID
     * @param oldPermissions The old object permissions, or <code>null</code> for new documents
     * @param newPermissions The new object permissions
     * @throws OXException
     */
    public ComparedObjectPermissions(int contextId, List<FileStorageObjectPermission> oldPermissions, List<FileStorageObjectPermission> newPermissions) throws OXException {
        super(newPermissions, oldPermissions);
        this.contextId = contextId;
        shareService = Services.getService(ShareService.class);
        if (shareService == null) {
            throw ServiceExceptionCode.absentService(ShareService.class);
        }
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

    public GuestInfo getGuestInfo(int guestId) throws OXException {
        GuestInfo guestInfo = guestInfos.get(guestId);
        if (guestInfo == null) {
            guestInfo = shareService.getGuestInfo(contextId, guestId);
            if (guestInfo == null) {
                guestInfo = NO_GUEST;
            }
            guestInfos.put(guestId, guestInfo);
        }

        if (guestInfo == NO_GUEST) {
            return null;
        }

        return guestInfo;
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
    };

}
