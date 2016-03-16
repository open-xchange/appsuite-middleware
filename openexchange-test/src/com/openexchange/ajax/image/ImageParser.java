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

package com.openexchange.ajax.image;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;


/**
 * {@link ImageParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ImageParser extends AbstractAJAXParser<ImageResponse> {

    private byte[] fileBytes;

    /**
     * Initializes a new {@link ImageParser}.
     * @param failOnError
     */
    protected ImageParser(boolean failOnError) {
        super(failOnError);
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#checkResponse(org.apache.http.HttpResponse)
     */
    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        assertEquals("Response code is not okay.", HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
        HttpEntity entity = resp.getEntity();
        fileBytes = EntityUtils.toByteArray(entity);

        return null;
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#createResponse(com.openexchange.ajax.container.Response)
     */
    @Override
    protected ImageResponse createResponse(Response response) throws JSONException {
        return null;
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#parse(java.lang.String)
     */
    @Override
    public ImageResponse parse(String body) throws JSONException {
        return new ImageResponse(fileBytes);
    }

}
