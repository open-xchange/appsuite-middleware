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

package com.openexchange.ajax.mail;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import com.openexchange.ajax.mail.actions.ForwardRequest;
import com.openexchange.ajax.mail.actions.ForwardResponse;
import com.openexchange.ajax.mail.actions.ReplyAllRequest;
import com.openexchange.ajax.mail.actions.ReplyAllResponse;
import com.openexchange.ajax.mail.actions.ReplyRequest;
import com.openexchange.ajax.mail.actions.ReplyResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.ContactTestManager;

/**
 * {@link AbstractReplyTest} - test for the Reply/ReplyAll/Forward family of requests.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractReplyTest extends AbstractMailTest {

    protected ContactTestManager contactManager;

    public AbstractReplyTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        this.contactManager = new ContactTestManager(getClient());
    }

    protected boolean contains(List<String> from, String string) {
        for (String str2 : from) {
            if (str2.contains(string)) {
                return true;
            }
        }
        return false;
    }

    protected JSONObject getReplyEMail(TestMail testMail) throws OXException, IOException, JSONException {
        ReplyRequest reply = new ReplyRequest(testMail.getFolder(), testMail.getId());
        reply.setFailOnError(true);
        ReplyResponse response = getClient().execute(reply);
        return (JSONObject) response.getData();
    }

    protected JSONObject getReplyAllEMail(TestMail testMail) throws OXException, IOException, JSONException {
        ReplyRequest reply = new ReplyAllRequest(testMail.getFolder(), testMail.getId());
        reply.setFailOnError(true);
        ReplyAllResponse response = (ReplyAllResponse) getClient().execute(reply);
        return (JSONObject) response.getData();
    }

    protected JSONObject getForwardMail(TestMail testMail) throws OXException, IOException, JSONException {
        ReplyRequest reply = new ForwardRequest(testMail.getFolder(), testMail.getId());
        reply.setFailOnError(true);
        ForwardResponse response = (ForwardResponse) getClient().execute(reply);
        return (JSONObject) response.getData();
    }

    public static void assertNullOrEmpty(String msg, Collection<?> coll) {
        if (coll == null) {
            return;
        }
        if (coll.size() == 0) {
            return;
        }
        fail(msg);
    }

}
