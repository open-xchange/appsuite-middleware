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

package com.openexchange.config.admin.internal;

import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;

/**
 * 
 * {@link PermissionFilterDocumentMetadataImpl} overrides {@link DocumentMetadataImpl} to filter out administrators permission
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class PermissionFilterDocumentMetadataImpl extends DocumentMetadataImpl implements DocumentMetadata {

    private static final long serialVersionUID = -1093207200136520302L;
    private final int adminUserId;

    /**
     * Initializes a new {@link PermissionFilterDocumentMetadataImpl} from specified folder.
     * 
     * @param adminUserId The user id of the context admin
     * @param userizedFolder The requested origin {@link UserizedFolder}
     */
    public PermissionFilterDocumentMetadataImpl(int adminUserId, DocumentMetadata documentMetadata) {
        super(documentMetadata);
        this.adminUserId = adminUserId;
    }

    /**
     * {@inheritDoc}
     * 
     * The returned {@link ObjectPermission}s will not contain one for the administrator even she actually has got {@link ObjectPermission}s for the related document. So this implementation should be used to view permissions only.
     */
    @Override
    public List<ObjectPermission> getObjectPermissions() {
        List<ObjectPermission> lObjectPermissions = super.getObjectPermissions();
        if (lObjectPermissions == null || lObjectPermissions.isEmpty()) {
            return lObjectPermissions;
        }
        return lObjectPermissions.stream().filter(x -> x.getEntity() != this.adminUserId).collect(Collectors.toList());
    }
}
