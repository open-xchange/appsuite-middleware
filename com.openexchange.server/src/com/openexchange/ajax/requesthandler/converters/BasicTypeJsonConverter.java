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

package com.openexchange.ajax.requesthandler.converters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicTypeJsonConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BasicTypeJsonConverter implements ResultConverter {

    private static final String JSON = "json";

    /**
     * The basic converters.
     */
    public static final List<ResultConverter> CONVERTERS = Collections.<ResultConverter> unmodifiableList(Arrays.<ResultConverter> asList(
        new BasicTypeJsonConverter("string"),
        new BasicTypeJsonConverter("int"),
        new BasicTypeJsonConverter("float"),
        new BasicTypeJsonConverter("boolean")));

    private final String inputFormat;

    /**
     * Initializes a new {@link BasicTypeJsonConverter}.
     *
     * @param inputFormat The input format
     */
    protected BasicTypeJsonConverter(final String inputFormat) {
        this.inputFormat = inputFormat;
    }

    @Override
    public String getInputFormat() {
        return inputFormat;
    }

    @Override
    public String getOutputFormat() {
        return JSON;
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Collection) {
            result.setResultObject(new JSONArray((Collection<?>) resultObject), JSON);
        } else if (resultObject instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, ? extends Object> map = (Map<String, ? extends Object>) resultObject;
            result.setResultObject(new JSONObject(map), JSON);
        } else if (resultObject instanceof String) {
            result.setResultObject(resultObject, JSON);
        } else if (resultObject instanceof Number) {
            result.setResultObject(resultObject, JSON);
        } else {
            result.setResultObject(asJSObject(resultObject.toString()), JSON);
        }
    }

    private static Object asJSObject(final String propertyValue) {
        if (null == propertyValue) {
            return null;
        }
        try {
            return new JSONTokener(propertyValue).nextValue();
        } catch (final Exception e) {
            return propertyValue;
        }
    }

}
