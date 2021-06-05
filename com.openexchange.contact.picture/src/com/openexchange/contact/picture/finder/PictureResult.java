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

package com.openexchange.contact.picture.finder;

import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.PictureSearchData;

/**
 * {@link PictureResult}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class PictureResult {

    private final boolean found;

    private final PictureSearchData data;

    private final ContactPicture picture;

    /**
     * Initializes a new {@link PictureResult} in a failed state.
     * 
     * @param data New data that can be provided by the caller
     * 
     */
    public PictureResult(PictureSearchData data) {
        this(false, null, data);
    }

    /**
     * Initializes a new {@link PictureResult} in a success state.
     * 
     * @param picture The {@link ContactPicture} or <code>null</code>
     * 
     */
    public PictureResult(ContactPicture picture) {
        this(true, picture, null);
    }

    /**
     * Initializes a new {@link PictureResult}.
     * 
     * @param found If the picture was found
     * @param picture The {@link ContactPicture} or <code>null</code>
     * @param data New data that can be provided by the caller
     * 
     */
    public PictureResult(boolean found, ContactPicture picture, PictureSearchData data) {
        super();
        this.found = found;
        this.picture = picture;
        this.data = data;
    }

    /**
     * If the picture was found
     * 
     * @return <code>true</code> if the picture was found
     */
    public boolean wasFound() {
        return found;
    }

    /**
     * Get additional data to make next search more successful
     * 
     * @return Updated {@link PictureSearchData}
     */
    public PictureSearchData getData() {
        return data;
    }

    /**
     * The {@link ContactPicture}
     * 
     * @return The {@link ContactPicture}
     */
    public ContactPicture getPicture() {
        return picture;
    }

}
