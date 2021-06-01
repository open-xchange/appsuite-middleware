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

package com.openexchange.appsuite.history.impl;

import com.openexchange.appsuite.DefaultFileCache;
import com.openexchange.appsuite.FileCache;
import com.openexchange.appsuite.FileCacheProvider;
import com.openexchange.session.Session;

/**
 * {@link HistoryFileCacheProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class HistoryFileCacheProvider implements FileCacheProvider {

    private final String version;
    private final FileCache cache;
    
    /**
     * Initializes a new {@link HistoryFileCacheProvider}.
     * 
     * @param version The version of this {@link DefaultFileCache}
     * @param fileCache The {@link DefaultFileCache} containing the files
     */
    public HistoryFileCacheProvider(String version, FileCache fileCache) {
        super();
        this.version = version;
        this.cache = fileCache;
    }
    
    @Override
    public FileCache getData() {
        return cache;
    }

	@Override
	public boolean isApplicable(Session session, String version, String path) {
		return this.version.equalsIgnoreCase(version);
	}
	
}
