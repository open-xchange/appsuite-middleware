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

package com.openexchange.file.storage.search;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;


/**
 * {@link CameraModelTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.2
 */
public class CameraModelTerm extends AbstractStringSearchTerm {

    /**
     * Initializes a new {@link CameraModelTerm}.
     *
     * @param cameraModel The camera model to look for
     */
    public CameraModelTerm(final String cameraModel) {
        super(cameraModel, true, true);
    }


    /**
     * Initializes a new {@link CameraModelTerm}
     *
     * @param cameraModel The camera model to look for
     * @param ignoreCase The ignore-case flag
     * @param substringSearch The substring vs. equals flag
     */
    public CameraModelTerm(final String cameraModel, final boolean ignoreCase, final boolean substringSearch) {
        super(cameraModel, ignoreCase, substringSearch);
    }


    @Override
    public void visit(final SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(Collection<Field> col) {
        if (null != col) {
            col.add(Field.CAMERA_MODEL);
        }
    }

    @Override
    protected String getString(File file) {
        return file.getCameraModel();
    }

}
