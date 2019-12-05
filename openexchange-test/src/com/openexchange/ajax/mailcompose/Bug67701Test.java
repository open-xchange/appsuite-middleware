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

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.Attachment;
import com.openexchange.testing.httpclient.models.ComposeBody;
import com.openexchange.testing.httpclient.models.MailComposeMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;

/**
 * {@link Bug67701Test}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Bug67701Test extends AbstractMailComposeTest {
    
    
    /**
     * Initializes a new {@link Bug67701Test}.
     */
    public Bug67701Test() {
        super("bug67701.eml");
    }
    
    
    @Test
    public void testAttachmentForwarded() throws Exception {
        ComposeBody body = new ComposeBody();
        body.setFolderId(mailWithAttachment.getFolderId());
        body.setId(mailWithAttachment.getId());
        MailComposeResponse reply = api.postMailCompose(getSessionId(), "FORWARD", null, Collections.singletonList(body));
        check(reply);
        MailComposeMessageModel data = reply.getData();
        compositionSpaceIds.add(data.getId());

        MailComposeResponse response = api.getMailComposeById(getSessionId(), data.getId());
        check(response);
        assertThat(I(response.getData().getAttachments().size()), is(I(1)));
        Attachment attach = response.getData().getAttachments().get(0);
        assertThat(attach.getMimeType(), is("application/pdf"));
    }
    

}
