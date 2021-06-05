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

package com.openexchange.chronos.provider.internal.config;

import static com.openexchange.chronos.provider.internal.Constants.CONTENT_TYPE;
import static com.openexchange.chronos.provider.internal.Constants.QUALIFIED_ACCOUNT_ID;
import static com.openexchange.chronos.provider.internal.Constants.TREE_ID;
import static com.openexchange.osgi.Tools.requireService;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultFolderId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultFolderId extends ReadOnlyChronosJSlobEntry {

    /**
     * Initializes a new {@link DefaultFolderId}.
     *
     * @param services A service lookup reference
     */
    public DefaultFolderId(ServiceLookup services) {
        super(services);
    }

    @Override
    public String getPath() {
        return "chronos/defaultFolderId";
    }

    @Override
    protected Object getValue(ServerSession session, JSONObject userConfig) throws OXException {
        UserizedFolder defaultFolder = requireService(FolderService.class, services).getDefaultFolder(
            session.getUser(), TREE_ID, CONTENT_TYPE, PrivateType.getInstance(), session, null);
        return QUALIFIED_ACCOUNT_ID + '/' + defaultFolder.getID();
    }

}
