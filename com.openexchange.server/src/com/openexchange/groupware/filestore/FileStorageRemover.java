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

package com.openexchange.groupware.filestore;

import java.sql.Connection;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.ContextDelete;
import com.openexchange.groupware.delete.DeleteEvent;

/**
 * This class implements a delete listener and removes the directories of the file store if the context is deleted.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FileStorageRemover extends ContextDelete {

    /**
     * Default constructor.
     */
    public FileStorageRemover() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (isContextDelete(event)) {
            removeFileStorage(event.getContext(), new SimpleDBProvider(readCon, writeCon));
        }
    }

    private void removeFileStorage(final Context ctx, final DBProvider dbProvider) throws OXException {
        FileStorage storage = getFileStorage(ctx, dbProvider);
        storage.remove();
    }

    private FileStorage getFileStorage(final Context ctx, final DBProvider dbProvider) throws OXException {
        return FileStorages.getQuotaFileStorageService().getQuotaFileStorage(ctx.getContextId(), Info.administrative());
    }
}
