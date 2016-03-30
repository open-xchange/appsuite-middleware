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
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicTypeAPIResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BasicTypeAPIResultConverter implements ResultConverter {

    /**
     * The basic converters.
     */
    public static final List<ResultConverter> CONVERTERS = Collections.<ResultConverter> unmodifiableList(Arrays.<ResultConverter> asList(
        new BasicTypeAPIResultConverter("string"),
        new BasicTypeAPIResultConverter("int"),
        new BasicTypeAPIResultConverter("float"),
        new BasicTypeAPIResultConverter("boolean"),
        new BasicTypeAPIResultConverter("json")));

    private final String inputFormat;

    /**
     * Initializes a new {@link BasicTypeAPIResultConverter}.
     *
     * @param inputFormat The input format
     */
    protected BasicTypeAPIResultConverter(final String inputFormat) {
        this.inputFormat = inputFormat;
    }

    @Override
    public String getInputFormat() {
        return inputFormat;
    }

    @Override
    public String getOutputFormat() {
        return "apiResponse";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final Response response = new Response(session);
        response.setData(result.getResultObject());
        response.setTimestamp(result.getTimestamp());
        response.setProperties(result.getResponseProperties());
        OXException exception = result.getException();
        if (null != exception) {
            response.setException(exception);
        }
        final Collection<OXException> warnings = result.getWarnings();
        if (null != warnings && !warnings.isEmpty()) {
            for (final OXException warning : warnings) {
                response.addWarning(warning);
            }
        }
        result.setResultObject(response);
    }

}
