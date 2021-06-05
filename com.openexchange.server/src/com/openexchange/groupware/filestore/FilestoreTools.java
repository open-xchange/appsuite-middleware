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

package com.openexchange.groupware.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * Contains tools the ease the use of the filestore.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FilestoreTools {

    private FilestoreTools() {
        super();
    }

    /**
     * Generates a context specific location of the filestore. The return value
     * can be used directly with the file storage classes for accessing the
     * stores.
     * @param store meta data for the filestore.
     * @param context Context that wants to use the file store.
     * @return read to use URI to the file store.
     */
    public static URI createLocation(final Filestore store, final Context context) throws OXException {
        if (store.getId() != context.getFilestoreId()) {
            throw FilestoreExceptionCodes.FILESTORE_MIXUP.create(I(store.getId()), I(context.getContextId()), I(context.getFilestoreId()));
        }
        final URI uri = store.getUri();
        try {
            return new URI(
                uri.getScheme(),
                uri.getAuthority(),
                uri.getPath() + '/' + context.getFilestoreName(),
                uri.getQuery(),
                uri.getFragment());
        } catch (URISyntaxException e) {
            throw FilestoreExceptionCodes.URI_CREATION_FAILED.create(e, uri.toString() + '/' + context.getFilestoreName());
        }
    }
}
