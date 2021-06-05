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

package com.openexchange.chronos.provider.internal.share;

import static com.openexchange.chronos.provider.internal.Constants.QUALIFIED_ACCOUNT_ID;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.ModuleAdjuster;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link CalendarModuleAdjuster}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarModuleAdjuster implements ModuleAdjuster {

    /**
     * Initializes a new {@link CalendarModuleAdjuster}.
     */
    public CalendarModuleAdjuster() {
        super();
    }

    @Override
    public Collection<String> getModules() {
        return Collections.singleton(Module.CALENDAR.getName());
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId, Connection connection) throws OXException {
        return adjustTarget(target);
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId, Connection connection) throws OXException {
        return adjustTarget(target);
    }

    private ShareTarget adjustTarget(ShareTarget target) {
        if (null == target || false == target.isFolder()) {
            return target;
        }
        String realFolderId = optRelativeFolderId(target.getFolderToLoad());
        if (null == realFolderId) {
            return target;
        }
        String folderId = getUniqueFolderId(realFolderId);
        return new ShareTarget(target.getModule(), folderId, realFolderId, target.getItem());
    }

    private static String optRelativeFolderId(String folderId) {
        try {
            return getRelativeFolderId(folderId);
        } catch (OXException e) {
            return null;
        }
    }

    private static String getUniqueFolderId(String folderId) {
        if (folderId.startsWith(QUALIFIED_ACCOUNT_ID)) {
            return folderId;
        }
        return QUALIFIED_ACCOUNT_ID + '/' + folderId;
    }

    private static String getRelativeFolderId(String folderId) throws OXException {
        if (null == folderId) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(folderId, "");
        }
        try {
            return String.valueOf(Integer.parseInt(folderId));
        } catch (NumberFormatException e) {
            if (false == folderId.startsWith(QUALIFIED_ACCOUNT_ID)) {
                throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(e, folderId, "");
            }
            List<String> components = IDMangler.unmangle(folderId);
            if (3 != components.size()) {
                throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(folderId, "");
            }
            return getRelativeFolderId(components.get(2));
        }
    }

}
