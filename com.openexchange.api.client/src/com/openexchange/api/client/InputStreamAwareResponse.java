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

package com.openexchange.api.client;

import java.io.InputStream;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;

/**
 * {@link InputStreamAwareResponse} - Represents an API response object which encapsulates an {@link InputStream}.
 * <p>
 * Implementing this interface is useful if an response object encapsulates an {@link InpuStream} whose closure is controlled by the caller.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public interface InputStreamAwareResponse {

    /**
     * Gets the {@link InputStream} of the response object
     *
     * @return The {@link InputStream} of the response object
     */
    @Nullable
    InputStream getInputStream();

    /**
     * Sets the {@link InputStream} of the response object
     *
     * @param stream The {@link InputStream} of the response object
     */
    void setInputStream(@NonNull InputStream stream);
}
