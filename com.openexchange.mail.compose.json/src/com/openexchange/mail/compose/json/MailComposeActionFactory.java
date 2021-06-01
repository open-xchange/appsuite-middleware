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

package com.openexchange.mail.compose.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.json.action.AbstractMailComposeAction;
import com.openexchange.mail.compose.json.action.AddAttachmentMailComposeAction;
import com.openexchange.mail.compose.json.action.AddOriginalAttachmentsMailComposeAction;
import com.openexchange.mail.compose.json.action.AddVCardMailComposeAction;
import com.openexchange.mail.compose.json.action.AllMailComposeAction;
import com.openexchange.mail.compose.json.action.DeleteAttachmentMailComposeAction;
import com.openexchange.mail.compose.json.action.DeleteCompositionSpaceAction;
import com.openexchange.mail.compose.json.action.GetAttachmentMailComposeAction;
import com.openexchange.mail.compose.json.action.GetCompositionSpaceAction;
import com.openexchange.mail.compose.json.action.OpenCompositionSpaceAction;
import com.openexchange.mail.compose.json.action.ReplaceAttachmentMailComposeAction;
import com.openexchange.mail.compose.json.action.SaveDraftCompositionSpaceAction;
import com.openexchange.mail.compose.json.action.SendCompositionSpaceAction;
import com.openexchange.mail.compose.json.action.UpdateCompositionSpaceAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailComposeActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MailComposeActionFactory implements AJAXActionServiceFactory {

    public static final String MODULE = "mailcompose";

    /**
     * Gets the <code>"mailcompose"</code> module identifier for mail compose action factory.
     *
     * @return The module identifier
     */
    public static String getModule() {
        return MODULE;
    }

    // ------------------------------------------------------------------------------------------

    private final Map<String, AbstractMailComposeAction> actions;

    /**
     * Initializes a new {@link MailComposeActionFactory}.
     */
    public MailComposeActionFactory(ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AbstractMailComposeAction> actions = ImmutableMap.builderWithExpectedSize(16);
        actions.put("open", new OpenCompositionSpaceAction(services));
        actions.put("delete", new DeleteCompositionSpaceAction(services));
        actions.put("get", new GetCompositionSpaceAction(services));
        actions.put("all", new AllMailComposeAction(services));
        actions.put("update", new UpdateCompositionSpaceAction(services));
        actions.put("addAttachment", new AddAttachmentMailComposeAction(services));
        actions.put("replaceAttachment", new ReplaceAttachmentMailComposeAction(services));
        actions.put("addOriginalAttachments", new AddOriginalAttachmentsMailComposeAction(services));
        actions.put("addVCardAttachment", new AddVCardMailComposeAction(services));
        actions.put("deleteAttachment", new DeleteAttachmentMailComposeAction(services));
        actions.put("getAttachment", new GetAttachmentMailComposeAction(services));
        actions.put("send", new SendCompositionSpaceAction(services));
        actions.put("save", new SaveDraftCompositionSpaceAction(services));

        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }

}
