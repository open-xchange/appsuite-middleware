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

package com.openexchange.messaging.json;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.ReferenceContent;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ReferenceContentRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReferenceContentRenderer implements MessagingContentWriter {

    /**
     *
     */
    private static final String REF = "ref";

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public boolean handles(final MessagingPart part, final MessagingContent content) {
        return ReferenceContent.class.isInstance(content);
    }

    @Override
    public Object write(final MessagingPart part, final MessagingContent content, final ServerSession session, final DisplayMode mode) throws OXException, JSONException {
        final ReferenceContent refContent = (ReferenceContent) content;
        final JSONObject object = new JSONObject();
        object.put(REF, refContent.getId());
        return object;
    }

}
