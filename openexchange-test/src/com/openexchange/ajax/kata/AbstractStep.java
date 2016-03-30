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

package com.openexchange.ajax.kata;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Assert;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;


/**
 * {@link AbstractStep}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractStep implements Step{
    protected String name;
    protected String expectedError;
    protected AJAXClient client;

    public AbstractStep(String name, String expectedError) {
        this.name = name;
        this.expectedError = expectedError;
    }

    protected void checkError(AbstractAJAXResponse response) {
        if(response.hasError()) {
            String message = response.getResponse().getErrorMessage();
            if(expectedError != null) {
                Assert.assertTrue(name+" expected error: "+expectedError+" but got: "+message, message.contains(expectedError));
            } else {
                fail(name+" did not expect error, but failed with: "+message);
            }

        } else {

            if(expectedError != null) {
                Assert.fail(name+" expected error "+expectedError+" but didn't get any errors");
            }
        }
    }

    protected boolean expectsError() {
        return expectedError != null;
    }

    protected TimeZone getTimeZone() throws OXException, IOException, SAXException, JSONException {
        return client.getValues().getTimeZone();
    }

    protected <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request) {
        try {
            return client.execute(request);
        } catch (OXException e) {
            fail("AjaxException during task creation: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("IOException during task creation: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            fail("JsonException during task creation: " + e.getLocalizedMessage());
        }
        return null;
    }

}
