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

package com.openexchange.groupware.infostore.database.impl;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.java.Charsets;
import com.openexchange.tools.exceptions.SimpleTruncatedAttribute;
import com.openexchange.tools.sql.DBUtils;

public class CheckSizeSwitch {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CheckSizeSwitch.class);

    private static Map<Metadata, Integer> SIZES = new HashMap<Metadata, Integer>();
    private final DBProvider provider;
    private final Context ctx;

    private static final Set<Metadata> FIELDS_TO_CHECK = ImmutableSet.of(
        Metadata.CATEGORIES_LITERAL,
        Metadata.FILE_MIMETYPE_LITERAL,
        Metadata.FILENAME_LITERAL,
        Metadata.URL_LITERAL,
        Metadata.DESCRIPTION_LITERAL,
        Metadata.TITLE_LITERAL,
        Metadata.VERSION_COMMENT_LITERAL);

    public CheckSizeSwitch(final DBProvider provider, final Context ctx) {
        this.provider = provider;
        this.ctx = ctx;
    }

    public static void checkSizes(final DocumentMetadata metadata, final DBProvider provider, final Context ctx) throws OXException {
        final CheckSizeSwitch checkSize = new CheckSizeSwitch(provider, ctx);
        final GetSwitch get = new GetSwitch(metadata);

        for(final Metadata m : Metadata.VALUES) {
            if (!FIELDS_TO_CHECK.contains(m)) {
                continue;
            }
            final Object value = m.doSwitch(get);
            final int maxSize = checkSize.getSize(m);
            int valueLength;
            if (value instanceof String) {
                valueLength = Charsets.getBytes((String) value, Charsets.UTF_8).length;
            } else {
                valueLength = 0;
            }
            if (maxSize < valueLength) {
                final OXException x = InfostoreExceptionCodes.TOO_LONG_VALUES.create();
                x.addProblematic(new SimpleTruncatedAttribute(m.getId(), maxSize, valueLength));
                throw x;
            }
        }
    }

    public int getSize(final Metadata field) {
        Integer iSize = SIZES.get(field);
        if (null != iSize) {
            return iSize.intValue();
        }

        Connection con = null;
        try {
            con = provider.getWriteConnection(ctx);
            final String[] tuple = InfostoreQueryCatalog.getInstance().getFieldTuple(field, new InfostoreQueryCatalog.VersionWins());
            final int size = DBUtils.getColumnSize(con, tuple[0], tuple[1]);
            SIZES.put(field, I(size));
            return size;
        } catch (SQLException e) {
            LOG.error("", e);
            return 0;
        } catch (OXException e) {
            LOG.error("",  e);
            return 0;
        } finally {
            provider.releaseWriteConnectionAfterReading(ctx, con);
        }

    }

}
