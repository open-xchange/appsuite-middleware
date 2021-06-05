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

package com.openexchange.filestore.impl.groupware.unified;

import static com.openexchange.filestore.impl.groupware.unified.UnifiedQuotaUtils.isUnifiedQuotaEnabledFor;
import java.net.URI;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FilestoreDataMoveListener;
import com.openexchange.filestore.QuotaFileStorage;

/**
 * {@link UnifiedQuotaFilestoreDataMoveListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class UnifiedQuotaFilestoreDataMoveListener implements FilestoreDataMoveListener {

    /**
     * Initializes a new {@link UnifiedQuotaFilestoreDataMoveListener}.
     */
    public UnifiedQuotaFilestoreDataMoveListener() {
        super();
    }

    @Override
    public void onBeforeContextDataMove(int contextId, URI srcUri, URI dstUri) throws OXException {
        // Don't care
    }

    @Override
    public void onAfterContextDataMoved(int contextId, URI srcUri, URI dstUri) {
        // Don't care
    }

    @Override
    public void onBeforeUserDataMove(int contextId, int userId, URI srcUri, URI dstUri) throws OXException {
        // Don't care
    }

    @Override
    public void onAfterUserDataMoved(int contextId, int userId, URI srcUri, URI dstUri) {
        // Don't care
    }

    @Override
    public void onBeforeContextToUserDataMove(int contextId, int userId, QuotaFileStorage srcContextStorage, QuotaFileStorage dstUserStorage) throws OXException {
        // Don't care. The supported case.
    }

    @Override
    public void onAfterContextToUserDataMoved(int contextId, int userId, QuotaFileStorage srcContextStorage, QuotaFileStorage dstUserStorage) {
        // Don't care
    }

    @Override
    public void onBeforeUserToContextDataMove(int contextId, int userId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstContextStorage) throws OXException {
        if (isUnifiedQuotaEnabledFor(userId, contextId)) {
            throw OXException.general("Denied to move file storage as unified quota is enabled for user " + userId + " in context " + contextId);
        }
    }

    @Override
    public void onAfterUserToContextDataMoved(int contextId, int userId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstContextStorage) {
        // Don't care
    }

    @Override
    public void onBeforeUserToMasterDataMove(int contextId, int userId, int masterId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstMasterStorage) throws OXException {
        if (isUnifiedQuotaEnabledFor(userId, contextId)) {
            throw OXException.general("Denied to move file storage as unified quota is enabled for user " + userId + " in context " + contextId);
        }
    }

    @Override
    public void onAfterUserToMasterDataMoved(int contextId, int userId, int masterId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstMasterStorage) {
        // Don't care
    }

    @Override
    public void onBeforeMasterToUserDataMove(int contextId, int userId, int masterId, QuotaFileStorage srcMasterStorage, QuotaFileStorage dstUserStorage) throws OXException {
        if (isUnifiedQuotaEnabledFor(userId, contextId)) {
            throw OXException.general("Denied to move file storage as unified quota is enabled for user " + userId + " in context " + contextId);
        }
    }

    @Override
    public void onAfterMasterToUserDataMoved(int contextId, int userId, int masterId, QuotaFileStorage srcMasterStorage, QuotaFileStorage dstUserStorage) {
        // Don't care
    }

}
