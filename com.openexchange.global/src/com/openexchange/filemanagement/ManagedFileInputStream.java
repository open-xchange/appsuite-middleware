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

package com.openexchange.filemanagement;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link ManagedFileInputStream} - An {@link InputStream} backed by a {@link ManagedFile}.
 * <p>
 * Invoking {@link #close()} also deletes associated managed file resources.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ManagedFileInputStream extends FilterInputStream {

    private final ManagedFile mf;

    /**
     * Initializes a new {@link ManagedFileInputStream}.
     * 
     * @param mf The managed file to read from
     * @throws OXException If obtaining file's input stream fails
     */
    public ManagedFileInputStream(ManagedFile mf) throws OXException {
        super(mf.getInputStream());
        this.mf = mf;
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            mf.delete();
        } catch (Exception x) {
            // Ignore
        }
    }

}
