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

package com.openexchange.jslob.storage.db.groupware;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.jslob.storage.db.DBJSlobStorage;

/**
 * {@link JSlobDBDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobDBDeleteListener implements DeleteListener {

    private final DBJSlobStorage dbJSlobStorage;

    /**
     * Initializes a new {@link JSlobDBDeleteListener}.
     */
    public JSlobDBDeleteListener(final DBJSlobStorage dbJSlobStorage) {
        super();
        this.dbJSlobStorage = dbJSlobStorage;
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            dbJSlobStorage.dropAllUserJSlobs(event.getId(), event.getContext().getContextId());
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            dbJSlobStorage.dropAllUsersJSlobs(event.getUserIds(), event.getContext().getContextId());
        }
    }

}
