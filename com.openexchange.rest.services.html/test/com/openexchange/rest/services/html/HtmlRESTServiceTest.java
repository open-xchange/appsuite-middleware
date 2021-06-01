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

package com.openexchange.rest.services.html;

import static org.junit.Assert.assertEquals;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.html.SimHtmlService;
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
