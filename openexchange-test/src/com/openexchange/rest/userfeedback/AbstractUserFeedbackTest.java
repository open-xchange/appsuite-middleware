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

package com.openexchange.rest.userfeedback;

import org.json.JSONObject;
import com.openexchange.rest.AbstractRestTest;
import com.openexchange.testing.restclient.modules.UserfeedbackApi;

/**
 * {@link AbstractUserFeedbackTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class AbstractUserFeedbackTest extends AbstractRestTest {

    protected String type = "star-rating-v1";

    // @formatter:off
    protected final String validFeedback = new String("{ " +
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
    
    protected JSONObject feedback;

    protected UserfeedbackApi userfeedbackApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userfeedbackApi = new UserfeedbackApi(getRestClient());
        
        feedback = new JSONObject(validFeedback);
    }

}
