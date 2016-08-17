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

package com.openexchange.rest.services.html;

import static org.junit.Assert.assertEquals;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.html.SimHtmlService;
import com.openexchange.rest.services.html.HtmlRESTService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link HtmlRESTServiceTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class HtmlRESTServiceTest {

    private HtmlRESTService service;

    private JSONObject body;

    @Before
    public void setup() throws JSONException {
        service = new HtmlRESTService(new SimHtmlService());
        body = new JSONObject();
        body.put("content", "<script>alert('Hello World!');</script><p>Hello World!</p><img src=\"http://lorempixel.com/400/200/\" />");
    }

    @Test
    public void testSanitizeHtmlString() throws OXException, JSONException {
        String actual = service.getSanitizedHtmlString(body.getString("content"));
        String expected = "<p>Hello World!</p><img src=\"\">";
        assertEquals(expected, actual);
    }

    @Test
    public void testSanitizeHtmlJSON() throws OXException, JSONException {
        JSONObject actual = service.getSanitizedHtmlJSON(body);
        JSONObject expected = new JSONObject();
        expected.put("content", "<p>Hello World!</p><img src=\"\">");
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testSanitizeHtmlStringKeepImages() throws OXException, JSONException {
        String actual = service.getSanitizedHtmlWithoutExternalImages(body.getString("content"));
        String expected = "<p>Hello World!</p><img src=\"http://lorempixel.com/400/200/\">";
        assertEquals(expected, actual);
    }

    @Test
    public void testSanitizeHtmlJSONKeepImages() throws OXException, JSONException {
        JSONObject actual = service.getSanitizedHtmlWithoutExternalImages(body);
        JSONObject expected = new JSONObject();
        expected.put("content", "<p>Hello World!</p><img src=\"http://lorempixel.com/400/200/\">");
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testMissingBody() {
        try {
            service.getSanitizedHtmlJSON(null);
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY.getNumber());
        }

        try {
            service.getSanitizedHtmlJSON(new JSONObject());
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY.getNumber());
        }

        try {
            service.getSanitizedHtmlWithoutExternalImages((JSONObject) null);
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY.getNumber());
        }

        try {
            service.getSanitizedHtmlWithoutExternalImages(new JSONObject());
        } catch (OXException e) {
            assertEquals(e.getExceptionCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY);
            assertEquals(e.getCode(), AjaxExceptionCodes.MISSING_REQUEST_BODY.getNumber());
        }
    }
}
