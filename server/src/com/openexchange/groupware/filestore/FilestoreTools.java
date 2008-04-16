/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.filestore;

import java.net.URI;
import java.net.URISyntaxException;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;

/**
 * Contains tools the ease the use of the filestore.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(
    classId=Classes.FILESTORE_TOOLS,
    component=EnumComponent.FILESTORE
)
public final class FilestoreTools {

    /**
     * For creating exceptions.
     */
    private static final FilestoreExceptionFactory EXCEPTION =
        new FilestoreExceptionFactory(FilestoreTools.class);

    /**
     * Prevent instantiation 
     */
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
    @OXThrowsMultiple(
        category = { Category.CODE_ERROR, Category.SETUP_ERROR },
        desc = { "", "" },
        exceptionId = { 1, 2 },
        msg = { "Wrong filestore %1$d for context %2$d needing filestore %3$d.",
            "Problem with URI when creating context specific filestore location." }
    )
    public static URI createLocation(final Filestore store,
        final Context context) throws FilestoreException {
        if (store.getId() != context.getFilestoreId()) {
            throw EXCEPTION.create(1, store.getId(), context.getContextId(),
                context.getFilestoreId());
        }
        final URI uri = store.getUri();
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath()
                + '/' + context.getFilestoreName(), uri.getQuery(),
                uri.getFragment());
        } catch (URISyntaxException e) {
            throw EXCEPTION.create(2, e);
        }
    }
}
