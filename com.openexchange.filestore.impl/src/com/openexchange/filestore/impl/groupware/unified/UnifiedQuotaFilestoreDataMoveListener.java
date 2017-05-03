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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.filestore.impl.groupware.unified;

import static com.openexchange.filestore.impl.groupware.unified.UnifiedQuotaUtils.isUnifiedQuotaEnabledFor;
import java.net.URI;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FilestoreDataMoveListener;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link UnifiedQuotaFilestoreDataMoveListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class UnifiedQuotaFilestoreDataMoveListener implements FilestoreDataMoveListener {

    private final ServiceListing<UnifiedQuotaService> unifiedQuotaServices;

    /**
     * Initializes a new {@link UnifiedQuotaFilestoreDataMoveListener}.
     */
    public UnifiedQuotaFilestoreDataMoveListener(ServiceListing<UnifiedQuotaService> unifiedQuotaServices) {
        super();
        this.unifiedQuotaServices = unifiedQuotaServices;
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
