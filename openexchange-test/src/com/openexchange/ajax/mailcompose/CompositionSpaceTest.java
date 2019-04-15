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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.mailcompose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.MailComposeGetResponse;
import com.openexchange.testing.httpclient.models.MailComposeMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;

/**
 * {@link CompositionSpaceTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public class CompositionSpaceTest extends AbstractMailComposeTest {

    @Test
    public void testCreateCompositionSpace() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        assertNotNull(model);
    }

    @Test
    public void testSendMail() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        MailComposeResponse loaded = api.getMailComposeById(getSessionId(), model.getId());
        assertNotNull(loaded);
        assertEquals("Wrong Composition Space loaded.", model.getId(), loaded.getData().getId());
        model.setFrom(getSender());
        model.setTo(getRecipient());
        model.setSubject(UUID.randomUUID().toString());
        model.setContent(UUID.randomUUID().toString());
        api.postMailComposeSend(getSessionId(), model.getId(), model.toJson());
        loaded = api.getMailComposeById(getSessionId(), model.getId());
        // TODO: Remove
        if (loaded.getData() != null) {
            System.out.println("\t\tOriginal: " + model.getId() + "\tLoaded: " + loaded.getData().getId());
        }
        assertEquals("Error expected.", "MSGCS-0007", loaded.getCode());
        assertNull("No data expected", loaded.getData());
    }

    @Test
    public void testSaveCompositionSpace() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();
        MailComposeResponse loaded = api.getMailComposeById(getSessionId(), model.getId());
        assertEquals("Wrong Composition Space loaded.", model.getId(), loaded.getData().getId());
        assertNotNull(loaded);
        String id = model.getId();
        api.getSave(getSessionId(), id);
        loaded = api.getMailComposeById(getSessionId(), model.getId());
        assertEquals("Error expected.", "MSGCS-0007", loaded.getCode());
        assertNull("No data expected", loaded.getData());
    }

    @Test
    public void testPatchCompositionSpace() throws Exception {
        MailComposeMessageModel model = createNewCompositionSpace();

        MailComposeMessageModel update = new MailComposeMessageModel();
        update.setId(model.getId());
        String subject = "Changed subject";
        List<String> sender = getSender();
        List<List<String>> recipient = getRecipient();
        update.setSubject(subject);
        update.setFrom(sender);
        update.setTo(recipient);
        api.patchMailComposeById(getSessionId(), update.getId(), update);

        MailComposeResponse loaded = api.getMailComposeById(getSessionId(), model.getId());
        MailComposeMessageModel loadedData = loaded.getData();
        assertEquals("Wrong id.", model.getId(), loadedData.getId());
        assertEquals("Wrong subject.", subject, loadedData.getSubject());
        assertEquals("Wrong sender.", sender.get(0), loadedData.getFrom().get(0));
        assertEquals("Wrong sender mail.", sender.get(1), loadedData.getFrom().get(1));
        assertEquals("Wrong amount of recipients.", 1, loadedData.getTo().size());
        assertEquals("Wrong recipient.", recipient.get(0).get(0), loadedData.getTo().get(0).get(0));
        assertEquals("Wrong recipient.", recipient.get(0).get(1), loadedData.getTo().get(0).get(1));
    }

    @Test
    public void testCreateMultipleCompositionSpaces() throws Exception {
        List<String> ids = new ArrayList<String>(5);
        for (int i = 0; i < 5; i++) {
            String id = createNewCompositionSpace().getId();
            ids.add(id);
        }

        MailComposeGetResponse response = api.getMailCompose(getSessionId(), null);
        assertEquals("Wrong amount of open composition spaces.", ids.size(), response.getData().size());
        response.getData().stream().map(MailComposeMessageModel::getId).forEach(id -> assertTrue("Wrong id.", ids.contains(id)));
    }

}
