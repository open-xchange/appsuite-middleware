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

package com.openexchange.mail.compose.mailstorage;

import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.DataProvider;


/**
 * {@link ThresholdFileHolderDataProvider} - A data provider backed by a {@code ThresholdFileHolder} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ThresholdFileHolderDataProvider implements DataProvider {

    private final ThresholdFileHolder fileHolder;

    /**
     * Initializes a new {@link ThresholdFileHolderDataProvider}.
     *
     * @param fileHolder The file holder
     */
    public ThresholdFileHolderDataProvider(ThresholdFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
    }

    /**
     * Gets the backing file holder.
     *
     * @return The file holder
     */
    public ThresholdFileHolder getFileHolder() {
        return fileHolder;
    }

    @Override
    public InputStream getData() throws OXException {
        return fileHolder.getStream();
    }

}
