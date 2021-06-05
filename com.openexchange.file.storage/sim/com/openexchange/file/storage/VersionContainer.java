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

package com.openexchange.file.storage;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * {@link VersionContainer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class VersionContainer {

    private final Map<Integer, FileHolder> versions = new HashMap<Integer, FileHolder>();

    private int currentVersion;

    /**
     * Initializes a new {@link VersionContainer}.
     * @param version
     * @param fileHolder
     */
    public VersionContainer() {
        super();
        currentVersion = -1;
    }

    public boolean containsVersion(int version) {
        return versions.containsKey(I(version));
    }

    public FileHolder getVersion(int version) {
        return versions.get(I(version));
    }

    public int addVersion(FileHolder fileHolder) {
        int version = ++currentVersion;
        versions.put(I(version), fileHolder);
        fileHolder.getInternalFile().setVersion("" + version);

        return version;
    }

    public FileHolder removeVersion(int version)  {
        FileHolder removed = versions.remove(I(version));
        if (version == currentVersion) {
            int tmp = -1;
            for (int v : versions.keySet()) {
                if (v > tmp) {
                    tmp = v;
                }
            }

            currentVersion = tmp;
        }

        return removed;
    }

    public FileHolder getCurrentVersion() {
        return versions.get(I(currentVersion));
    }

    public int getCurrentVersionNumber() {
        return currentVersion;
    }

    public Collection<FileHolder> getAllVersions() {
        return versions.values();
    }
}
