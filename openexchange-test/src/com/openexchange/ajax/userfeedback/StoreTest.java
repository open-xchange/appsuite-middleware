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

package com.openexchange.ajax.userfeedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.userfeedback.actions.StoreRequest;
import com.openexchange.ajax.userfeedback.actions.StoreResponse;

/**
 * 
 * {@link StoreTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class StoreTest extends AbstractAJAXSession {

    private String emptyFeedback = "{}";

    // @formatter:off
    private final String validFeedback = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
        "\"Comment\": \"s\u00FC\u00DFer die gl\u00F6cken nie klingen !|\u00A7$%&/()=?@\","+
        "\"Operating_System\": \"Mac OS X 10.10\","+
        "\"Browser\":\"Chrome\","+
        "\"Browser_version\": \"77.0\","+
        "\"User_agent\": \"Chrome/55.0.2883.87\","+
        "\"Screen_Resolution\":\"1600x900\","+
        "\"Language\": \"de_de\""+
        "}");

    // @formatter:off
    private final String littleFeedback = new String("{ " +
        "\"score\":\"3\""+
        "}");
    
    @Test
    public void testStore_typeNull_returnError() throws Exception {
        StoreRequest store = new StoreRequest(null, emptyFeedback);
        StoreResponse response = getClient().execute(store);

        assertTrue(response.hasError());
        assertTrue(response.getException().getLogMessage().contains("Missing the following request parameter: type"));
    }

    @Test
    public void testStore_feedbackNull_returnError() throws Exception {
        StoreRequest store = new StoreRequest("star-rating-v1", null);
        StoreResponse response = getClient().execute(store);

        assertTrue(response.hasError());
        assertTrue(response.getException().getLogMessage().contains("Missing request body."));
    }

    @Test
    public void testStore_emptyFeedback_returnError() throws Exception {
        StoreRequest store = new StoreRequest("star-rating-v1", emptyFeedback);
        StoreResponse response = getClient().execute(store);

        assertTrue(response.hasError());
        assertTrue(response.getException().getMessage().contains("Provided JSON object does not contain content."));
    }

    @Test
    public void testStore_feedbackTypeUnknown_returnError() throws Exception {
        String type = "jibbetNet";
        StoreRequest store = new StoreRequest(type, validFeedback);
        StoreResponse response = getClient().execute(store);

        assertTrue(response.hasError());
        assertTrue(response.getException().getLogMessage().contains(type));
        assertTrue(response.getException().getLogMessage().contains("Unknown feedback type"));
    }

    @Test
    public void testStore() throws Exception {
        StoreRequest store = new StoreRequest("star-rating-v1", validFeedback);
        StoreResponse response = getClient().execute(store);

        assertFalse(response.hasError());
    }
    
    @Test
    public void testStore_fewInfo_addedByImplemenation() throws Exception {
        StoreRequest store = new StoreRequest("star-rating-v1", littleFeedback);
        StoreResponse response = getClient().execute(store);

        assertFalse(response.hasError());
        
    }

    @Test
    public void testStore_invalidScore_returnException() throws Exception {
        String feedback = new String("{\"score\":\"0\"}");
        StoreRequest store = new StoreRequest("star-rating-v1", feedback);
        StoreResponse response = getClient().execute(store);

        assertTrue(response.hasError());
        assertEquals("The provided feedback score is invalid. Please choose a number > 0.", response.getErrorMessage());
    }
}
