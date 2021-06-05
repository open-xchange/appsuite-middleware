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

package com.openexchange.chronos.ical.ical4j.handler;

import static com.openexchange.chronos.ical.impl.ICalUtils.getParametersOrDefault;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;

/**
 * {@link ICal2ObjectDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICal2ObjectDataHandler<O> implements DataHandler {

    protected static final byte[] VEVENT_PREFIX = "BEGIN:VCALENDAR\r\nBEGIN:VEVENT\r\n".getBytes(Charsets.UTF_8);
    protected static final byte[] VEVENT_SUFFIX = "END:VEVENT\r\nEND:VCALENDAR\r\n".getBytes(Charsets.UTF_8);
    protected static final byte[] VCALENDAR_PREFIX = "BEGIN:VCALENDAR\r\n".getBytes(Charsets.UTF_8);
    protected static final byte[] VCALENDAR_SUFFIX = "END:VCALENDAR\r\n".getBytes(Charsets.UTF_8);

    /**
     * Initializes a new {@link ICal2ObjectDataHandler}.
     */
    protected ICal2ObjectDataHandler() {
        super();
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { IFileHolder.class, InputStream.class, byte[].class, String.class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        ConversionResult result = new ConversionResult();
        Object sourceData = data.getData();
        if (null == sourceData) {
            result.setData(null);
        } else if (IFileHolder.class.isInstance(sourceData)) {
            InputStream inputStream = null;
            try {
                inputStream = ((IFileHolder) sourceData).getStream();
                return processData(new SimpleData<InputStream>(inputStream), dataArguments, session);
            } finally {
                Streams.close(inputStream);
            }
        } else if (byte[].class.isInstance(sourceData)) {
            InputStream inputStream = null;
            try {
                inputStream = Streams.newByteArrayInputStream((byte[]) sourceData);
                return processData(new SimpleData<InputStream>(inputStream), dataArguments, session);
            } finally {
                Streams.close(inputStream);
            }
        } else if (String.class.isInstance(sourceData)) {
            return processData(new SimpleData<byte[]>(((String) sourceData).getBytes(Charsets.UTF_8)), dataArguments, session);
        } else if (InputStream.class.isInstance(sourceData)) {
            result.setData(parse((InputStream) sourceData, getParameters()));
        } else {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(sourceData.getClass().toString());
        }
        return result;
    }

    protected abstract O parse(InputStream inputStream, ICalParameters parameters) throws OXException;

    protected ICalParameters getParameters() {
        return getParametersOrDefault(null);
    }

}
