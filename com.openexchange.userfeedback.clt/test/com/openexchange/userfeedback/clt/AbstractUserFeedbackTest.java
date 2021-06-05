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

package com.openexchange.userfeedback.clt;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.userfeedback.clt.impl.TestUserFeedbackImpl;

/**
 * {@link AbstractUserFeedbackTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class AbstractUserFeedbackTest {

    private TestUserFeedbackImpl userFeedbackImpl = new TestUserFeedbackImpl();

    @Test
    public void testAddPathIfRequired() {
        String endpointDefault = AbstractUserFeedback.ENDPOINT_DEFAULT;
        String addPathIfRequired = userFeedbackImpl.addPathIfRequired(endpointDefault, "export");

        assertEquals("http://localhost:8009/userfeedback/v1/export/", addPathIfRequired);
    }

    @Test
    public void testAddPathIfRequired1() {
        String endpointDefault = "http://localhost:8009/userfeedback/v1";
        String addPathIfRequired = userFeedbackImpl.addPathIfRequired(endpointDefault, "export");

        assertEquals("http://localhost:8009/userfeedback/v1/export/", addPathIfRequired);
    }

    @Test
    public void testAddPathIfRequired2() {
        String endpointDefault = "http://localhost:8009/userfeedback/v1/export";
        String addPathIfRequired = userFeedbackImpl.addPathIfRequired(endpointDefault, "export");

        assertEquals("http://localhost:8009/userfeedback/v1/export/", addPathIfRequired);
    }

    @Test
    public void testAddPathIfRequired3() {
        String endpointDefault = "http://localhost:8009/userfeedback/v1/export/";
        String addPathIfRequired = userFeedbackImpl.addPathIfRequired(endpointDefault, "export");

        assertEquals("http://localhost:8009/userfeedback/v1/export/", addPathIfRequired);
    }

    @Test
    public void testAddPathIfRequired4() {
        String endpointDefault = AbstractUserFeedback.ENDPOINT_DEFAULT;
        String addPathIfRequired = userFeedbackImpl.addPathIfRequired(endpointDefault, "");

        assertEquals("http://localhost:8009/userfeedback/v1/", addPathIfRequired);
    }

    @Test
    public void testAddPathIfRequired5() {
        String endpointDefault = AbstractUserFeedback.ENDPOINT_DEFAULT;
        String addPathIfRequired = userFeedbackImpl.addPathIfRequired(endpointDefault, null);

        assertEquals("http://localhost:8009/userfeedback/v1/", addPathIfRequired);
    }
}
