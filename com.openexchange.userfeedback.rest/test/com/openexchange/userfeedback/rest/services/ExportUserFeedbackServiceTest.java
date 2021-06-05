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

package com.openexchange.userfeedback.rest.services;

import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ExportUserFeedbackServiceTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class ExportUserFeedbackServiceTest {

    private ExportUserFeedbackService service;

    @Mock
    private ServiceLookup serviceLookup;

    private long now;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        service = new ExportUserFeedbackService(serviceLookup);
        now = new Date().getTime();
    }

    @Test(expected = OXException.class)
    public void testValidateParams_startAfterDate_throwException() throws OXException {
        service.validateParams(now, now - 100000L);

        fail();
    }

    @Test(expected = OXException.class)
    public void testValidateParams_startNegative_throwException() throws OXException {
        service.validateParams(-55555L, now);

        fail();
    }

    @Test(expected = OXException.class)
    public void testValidateParams_endNegative_throwException() throws OXException {
        service.validateParams(now, -44444L);

        fail();
    }

    @Test
    public void testValidateParams_paramsOk_return() {
        try {
            service.validateParams(now - 100000000L, now);
        } catch (OXException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testValidateParams_paramsNotSet_return() {
        try {
            service.validateParams(0L, 0L);
        } catch (OXException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testValidateParams_onlyStartSet_return() {
        try {
            service.validateParams(now, 0L);
        } catch (OXException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testValidateParams_onlyEndSet_return() {
        try {
            service.validateParams(0L, now);
        } catch (OXException e) {
            fail(e.getMessage());
        }
    }
}
