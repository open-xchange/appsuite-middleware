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
package com.openexchange.groupware.infostore;

import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.StaticDBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.groupware.infostore.facade.impl.EventFiringInfostoreFacadeImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InfostoreDowngrade implements DowngradeListener {
    @Override
	public void downgradePerformed(final DowngradeEvent event) throws OXException {
        final UserConfiguration newUserConfiguration = event.getNewUserConfiguration();
        if (newUserConfiguration.hasInfostore()) {
            // Still access to infostore...
            return;
        }
        final DBProvider provider = new StaticDBPoolProvider(event.getWriteCon());

        final ServerSession session = ServerSessionAdapter.valueOf(event.getSession(), event.getContext());

        final InfostoreFacade infostore = new EventFiringInfostoreFacadeImpl(provider);
        infostore.setTransactional(true);
        infostore.setCommitsTransaction(false);
        try {

            final OXFolderAccess access = new OXFolderAccess(event.getContext());
            final FolderObject fo = access.getDefaultFolder(newUserConfiguration.getUserId(), FolderObject.INFOSTORE);
            final int folderId = fo.getObjectID();


            infostore.startTransaction();

            infostore.removeDocument(folderId, Long.MAX_VALUE, session);

            infostore.commit();
        } catch (OXException e) {
            try {
                infostore.rollback();
            } catch (OXException e1) {
                //IGNORE
            }
            throw e;
        } finally {
            try {
                infostore.finish();
            } catch (OXException e) {
                //IGNORE
            }
        }
    }

    @Override
	public int getOrder() {
        return 2;
    }
}
