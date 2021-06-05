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

package com.openexchange.filestore.s3.internal.config;


/**
 * Denotes the scope of an S3 API client. I.e. if it is shared across multiple filestore
 * instances or not.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public enum S3ClientScope {

    /**
     * The S3 client instance is used only for a single filestore instance.
     */
    DEDICATED,
    /**
     * The S3 client instance is shared between multiple filestore instances that
     * rely on the very same configuration and have only different buckets set.
     */
    SHARED,
    ;

    /**
     * Checks whether this {@link S3ClientScope} is of type {@link S3ClientScope#SHARED} or not
     *
     * @return <code>true</code> if it of type {@link S3ClientScope#SHARED}, <code>false</code> otherwise
     */
    public boolean isShared() {
        return this.equals(SHARED);
    }
}
