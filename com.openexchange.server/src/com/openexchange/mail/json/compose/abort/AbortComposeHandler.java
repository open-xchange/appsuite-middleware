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

package com.openexchange.mail.json.compose.abort;

import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DelegatingComposedMailMessage;
import com.openexchange.mail.json.compose.AbstractComposeHandler;
import com.openexchange.mail.json.compose.ComposeDraftResult;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.json.compose.DefaultComposeDraftResult;
import com.openexchange.mail.json.compose.DefaultComposeTransportResult;


/**
 * {@link AbortComposeHandler} - The default compose handler that aborts processing if upload limit is exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class AbortComposeHandler extends AbstractComposeHandler<AbortComposeContext, AbortComposeContext> {

    private static final AbortComposeHandler INSTANCE = new AbortComposeHandler();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static AbortComposeHandler getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AbortComposeHandler}.
     */
    private AbortComposeHandler() {
        super();
    }

    @Override
    public String getId() {
        return "abort";
    }

    @Override
    protected AbortComposeContext createDraftComposeContext(ComposeRequest request) throws OXException {
        return new AbortComposeContext(request);
    }

    @Override
    protected AbortComposeContext createTransportComposeContext(ComposeRequest request) throws OXException {
        return new AbortComposeContext(request);
    }

    @Override
    protected ComposeDraftResult doCreateDraftResult(ComposeRequest request, AbortComposeContext context) throws OXException {
        ComposedMailMessage composeMessage = createRegularComposeMessage(context);
        return new DefaultComposeDraftResult(composeMessage);
    }

    @Override
    protected ComposeTransportResult doCreateTransportResult(ComposeRequest request, AbortComposeContext context) throws OXException {
        ComposedMailMessage composeMessage = createRegularComposeMessage(context);
        DelegatingComposedMailMessage transportMessage = new DelegatingComposedMailMessage(composeMessage);
        transportMessage.setAppendToSentFolder(false);
        return DefaultComposeTransportResult.builder().withTransportMessages(Collections.<ComposedMailMessage> singletonList(transportMessage), true).withSentMessage(composeMessage).withTransportEqualToSent().build();
    }

    @Override
    public boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        return true;
    }

}
