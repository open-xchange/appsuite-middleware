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

package com.openexchange.file.storage.infostore.folder;

import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.SetterAwareFolder;
import com.openexchange.folderstorage.UsedForSync;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link ParsedFolder} - A parsed folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ParsedFolder extends AbstractFolder implements SetterAwareFolder {

    private static final long serialVersionUID = 11110622220507954L;

    private static final String ACCOUNT_ID = IDMangler.mangle(FileID.INFOSTORE_SERVICE_ID, FileID.INFOSTORE_ACCOUNT_ID);

    private boolean containsSubscribed;
    private boolean containsUsedForSync;

    /**
     * Initializes an empty {@link ParsedFolder}.
     */
    public ParsedFolder() {
        super();
    }

    @Override
    public String getAccountID() {
        return ACCOUNT_ID;
    }

    @Override
    public void setAccountID(String accountId) {
        // no-op
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

    @Override
    public void setSubscribed(boolean subscribed) {
        super.setSubscribed(subscribed);
        containsSubscribed = true;
    }

    @Override
    public boolean containsSubscribed() {
        return containsSubscribed;
    }

    @Override
    public void setUsedForSync(UsedForSync usedForSync) {
        super.setUsedForSync(usedForSync);
        containsUsedForSync = true;
    }

    @Override
    public boolean containsUsedForSync() {
        return containsUsedForSync;
    }

}
