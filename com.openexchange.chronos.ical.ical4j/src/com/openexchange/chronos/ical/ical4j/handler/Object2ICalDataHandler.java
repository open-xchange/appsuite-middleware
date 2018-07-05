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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
