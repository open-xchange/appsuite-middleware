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

package com.openexchange.drive.client.windows.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link ResourceLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ResourceLoader {

    InputStream get(String name) throws IOException;

    String getMD5(String name) throws IOException;
    
    long getSize(String name) throws IOException;

    boolean exists(String name) throws IOException;
    
    public Set<String> getAvailableFiles() throws OXException;

    /**
     * @param name
     * @return
     * @throws IOException
     */
    File getFile(String name) throws IOException;

}
