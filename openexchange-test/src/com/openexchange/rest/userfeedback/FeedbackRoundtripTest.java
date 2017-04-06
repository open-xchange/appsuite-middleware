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

package com.openexchange.rest.userfeedback;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.core.Application;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.userfeedback.actions.StoreRequest;
import com.openexchange.exception.OXException;
import com.openexchange.testing.restclient.invoker.ApiException;
import com.openexchange.userfeedback.rest.services.DeleteUserFeedbackService;
import com.openexchange.userfeedback.rest.services.ExportUserFeedbackService;
import com.openexchange.userfeedback.rest.services.SendUserFeedbackService;

/**
 * {@link FeedbackRoundtripTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class FeedbackRoundtripTest extends AbstractUserFeedbackTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FeedbackRoundtripTest.class);

    @Override
    protected Application configure() {
        return new ResourceConfig(ExportUserFeedbackService.class, DeleteUserFeedbackService.class, SendUserFeedbackService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        removeFeedbacks();
    }

    @Test
    public void testRawExport() throws Exception {
        storeFeedbacks(3);

        String export = userfeedbackApi.exportRAW("default", type, new Long(0), new Long(0));
        JSONArray jsonExport = new JSONArray(export);

        assertEquals(3, jsonExport.length());
    }

    @Test
    public void testCsvExport() throws Exception {
        storeFeedbacks(3);

        File export2 = userfeedbackApi.exportCSV("default", type, new Long(0), new Long(0));
        String readFileToString = FileUtils.readFileToString(export2);
        System.out.println(readFileToString);

    }

    private void storeFeedbacks(int numberOfFeedbacks) throws OXException, IOException, JSONException {
        StoreRequest feedback = new StoreRequest(type, validFeedback);
        for (int feedbacks = 0; feedbacks < numberOfFeedbacks; feedbacks++) {
            getAjaxClient().execute(feedback);
        }

    }

    @Override
    public void tearDown() throws Exception {
        try {
            removeFeedbacks();
        } finally {
            super.tearDown();
        }
    }

    private void removeFeedbacks() {
        try {
            userfeedbackApi.delete("default", type, new Long(0), new Long(0));
        } catch (ApiException e) {
            LOG.warn("Unable to cleanup: {}", e.getMessage(), e);
        }
    }
}
