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

import java.io.File;
import java.io.InputStream;
import org.json.JSONObject;

/**
 * {@link ExifService}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public interface IMetadataReader {

        /**
         * @param mediaInputStm
         * @return
         */
        public IMetadata readMetadata(final InputStream mediaInputStm) throws MetadataException;

        /**
         * @param mediaInputFile
         * @return
         */
        public IMetadata readMetadata(final File mediaInputFile) throws MetadataException;

        /**
         * @param jsonExif
         * @return
         */
        public IMetadata readMetadata(final JSONObject jsonMetada) throws MetadataException;
}
