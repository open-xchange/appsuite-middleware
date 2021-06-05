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

package com.openexchange.imageconverter.api;

import java.io.Closeable;
import com.openexchange.auth.Credentials;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;

/**
 * {@link IImageClient}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public interface IImageClient extends IImageConverter, Reloadable, Closeable  {

    /**
     * @return The status of the remote ImageServer.
     */
    public String status() throws OXException;

    /**
     * @return <code>true</code> if the current client is connected to the server,
     *  <code>false</code> otherwise.
     * @throws OXException
     */
    public boolean isConnected() throws OXException;

    /**
     * @param credentials The credentials to be used for requests,
     *  requiring authentication.
     * @throws OXException
     */
    public void setCredentials(Credentials credentials) throws OXException;
}
