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

package com.openexchange.ajax.config;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;

/**
 * Tests if forwarding configuration parameter correctly stores the values
 * "Inline" or "Attachment".
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ForwardInlineOrAttachmentTest extends AbstractAJAXSession {

    /**
     * Path to the configuration parameter.
     */
    private static final String PATH = "/mail/forwardmessage";

    /**
     * Inline value.
     */
    private static final String INLINE = "Inline";

    /**
     * Attachment value.
     */
    private static final String ATTACHMENT = "Attachment";

    /**
     * Tests if the forward configuration parameter is stored correctly.
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws OXException 
     */
    @Test
    public void testForwardParameter() throws OXException, IOException, JSONException {
        GetRequest getRequest = new GetRequest(PATH);
        GetResponse repsonse = getClient().execute(getRequest);
        for (final String testString : new String[] { INLINE, ATTACHMENT, repsonse.getData().toString() }) {
            getClient().execute(new SetRequest(Tree.ForwardMessage, testString));
            assertEquals("Written setting isn't returned from server.", testString, getClient().execute(getRequest).getData().toString());
        }
    }
}
