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
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;

/**
 * {@link Object2ICalDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class Object2ICalDataHandler<T extends Component, U> implements DataHandler {

    private final Class<U> clazz;
    private final Class<U[]> arrayClass;

    /**
     * Initializes a new {@link Object2ICalDataHandler}.
     *
     * @param clazz The object class
     * @param arrayClass The array of objects class
     */
    protected Object2ICalDataHandler(Class<U> clazz, Class<U[]> arrayClass) {
        super();
        this.clazz = clazz;
        this.arrayClass = arrayClass;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { clazz, arrayClass, Collection.class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        ConversionResult result = new ConversionResult();
        Object sourceData = data.getData();
        if (null == sourceData) {
            result.setData(null);
            return result;
        }
        ICalParameters parameters = getParameters();
        ComponentList components = new ComponentList();
        List<OXException> warnings = new ArrayList<OXException>();
        if (clazz.isInstance(sourceData)) {
            components.add(export(clazz.cast(sourceData), parameters, warnings));
        } else if (arrayClass.isInstance(sourceData)) {
            for (U object : arrayClass.cast(sourceData)) {
                components.add(export(object, parameters, warnings));
            }
        } else if (Collection.class.isInstance(sourceData)) {
            for (Object object : (Collection<?>) sourceData) {
                components.add(export(clazz.cast(object), parameters, warnings));
            }
        } else {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(sourceData.getClass().toString());
        }
        result.setData(export(components));
        result.setWarnings(warnings);
        return result;
    }

    private String export(ComponentList components) throws OXException {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = Streams.newByteArrayOutputStream();
            ICalUtils.exportComponents(outputStream, components);
            return outputStream.toString(Charsets.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            throw ICalExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(outputStream);
        }
    }

    protected abstract T export(U object, ICalParameters parameters, List<OXException> warnings) throws OXException;

    protected ICalParameters getParameters() {
        return getParametersOrDefault(null);
    }

}
