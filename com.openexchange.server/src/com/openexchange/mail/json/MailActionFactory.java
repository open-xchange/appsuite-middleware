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

package com.openexchange.mail.json;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.AJAXStateHandler;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.actions.AllAction;
import com.openexchange.mail.json.actions.AllSeenAction;
import com.openexchange.mail.json.actions.ArchiveAction;
import com.openexchange.mail.json.actions.ArchiveFolderAction;
import com.openexchange.mail.json.actions.AutosaveAction;
import com.openexchange.mail.json.actions.BounceAction;
import com.openexchange.mail.json.actions.ClearAction;
import com.openexchange.mail.json.actions.CopyAction;
import com.openexchange.mail.json.actions.DeleteAction;
import com.openexchange.mail.json.actions.EditAction;
import com.openexchange.mail.json.actions.ExamineAction;
import com.openexchange.mail.json.actions.ExpungeAction;
import com.openexchange.mail.json.actions.GetAction;
import com.openexchange.mail.json.actions.GetAttachmentAction;
import com.openexchange.mail.json.actions.GetAttachmentTokenAction;
import com.openexchange.mail.json.actions.GetForwardAction;
import com.openexchange.mail.json.actions.GetMailCountAction;
import com.openexchange.mail.json.actions.GetMultipleAttachmentAction;
import com.openexchange.mail.json.actions.GetMultipleMessagesAction;
import com.openexchange.mail.json.actions.GetReplyAction;
import com.openexchange.mail.json.actions.GetReplyAllAction;
import com.openexchange.mail.json.actions.GetStructureAction;
import com.openexchange.mail.json.actions.GetUpdatesAction;
import com.openexchange.mail.json.actions.GetVersitAction;
import com.openexchange.mail.json.actions.ImportAction;
import com.openexchange.mail.json.actions.ListAction;
import com.openexchange.mail.json.actions.MoveAllAction;
import com.openexchange.mail.json.actions.NewAction;
import com.openexchange.mail.json.actions.ReceiptAckAction;
import com.openexchange.mail.json.actions.ResendAction;
import com.openexchange.mail.json.actions.ResolveShareReference;
import com.openexchange.mail.json.actions.SearchAction;
import com.openexchange.mail.json.actions.SendDataAction;
import com.openexchange.mail.json.actions.SimpleThreadStructureAction;
import com.openexchange.mail.json.actions.ThreadReferencesAction;
import com.openexchange.mail.json.actions.TransportMailAction;
import com.openexchange.mail.json.actions.TrashAction;
import com.openexchange.mail.json.actions.UpdateAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OAuthModule
public class MailActionFactory implements AJAXActionServiceFactory, AJAXStateHandler, MailActionConstants {

    private static final AtomicReference<MailActionFactory> INSTANCE_REFERENCE = new AtomicReference<MailActionFactory>();

    public static final String MODULE = "mail";

    /**
     * The read-only scope for OAuth requests
     */
    @Deprecated
    public static final String OAUTH_READ_SCOPE = "read_mail";

    /**
     * The writable scope for OAuth requests
     */
    @Deprecated
    public static final String OAUTH_WRITE_SCOPE = "write_mail";

    /**
     * Gets the action factory
     *
     * @return The action factory or <code>null</code>
     */
    public static MailActionFactory getActionFactory() {
        return INSTANCE_REFERENCE.get();
    }

    /**
     * Initializes the action factory instance
     *
     * @param services The service look-up
     * @return The initialized instance
     */
    public static MailActionFactory initializeActionFactory(ServiceLookup services) {
        MailActionFactory actionFactory = new MailActionFactory(services);
        INSTANCE_REFERENCE.set(actionFactory);
        return actionFactory;
    }

    /**
     * Releases the action factory instance.
     */
    public static void releaseActionFactory() {
        INSTANCE_REFERENCE.set(null);
    }

    // ----------------------------------------------------------------------------------------------

    private final Map<String, AbstractMailAction> actions;

    /**
     * Initializes a new {@link MailActionFactory}.
     *
     * @param services The service look-up
     */
    private MailActionFactory(ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AbstractMailAction> builder = ImmutableMap.builder();
        builder.put("all", new AllAction(services));
        builder.put("threadedAll", new SimpleThreadStructureAction(services));
        builder.put("get", new GetAction(services));
        builder.put("get_structure", new GetStructureAction(services));
        builder.put("count", new GetMailCountAction(services));
        builder.put("copy", new CopyAction(services));
        builder.put("move_all", new MoveAllAction(services));
        builder.put("archive", new ArchiveAction(services));
        builder.put("archive_folder", new ArchiveFolderAction(services));
        builder.put("reply", new GetReplyAction(services));
        builder.put("replyall", new GetReplyAllAction(services));
        builder.put("updates", new GetUpdatesAction(services));
        builder.put("forward", new GetForwardAction(services));
        builder.put("bounce", new BounceAction(services));
        builder.put("resend", new ResendAction(services));
        builder.put("attachment", new GetAttachmentAction(services));
        builder.put("attachmentToken", new GetAttachmentTokenAction(services));
        builder.put("zip_attachments", new GetMultipleAttachmentAction(services));
        builder.put("zip_messages", new GetMultipleMessagesAction(services));
        builder.put("saveVersit", new GetVersitAction(services));

        builder.put("list", new ListAction(services));
        builder.put("search", new SearchAction(services));
        builder.put("update", new UpdateAction(services));
        builder.put("delete", new DeleteAction(services));
        builder.put("transport", new TransportMailAction(services));
        builder.put("receipt_ack", new ReceiptAckAction(services));
        builder.put("clear", new ClearAction(services));
        builder.put("expunge", new ExpungeAction(services));
        NewAction newAction = new NewAction(services);
        builder.put("new", newAction);
        builder.put("send_data", new SendDataAction(newAction, services));
        builder.put("import", new ImportAction(services));
        builder.put("edit", new EditAction(services));
        builder.put("autosave", new AutosaveAction(services));

        builder.put("all_seen", new AllSeenAction(services));
        builder.put("resolve_share_reference", new ResolveShareReference(services));
        builder.put("examine", new ExamineAction(services));
        builder.put("thread_references", new ThreadReferencesAction(services));
        builder.put("trash", new TrashAction(services));

        this.actions = builder.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }

    @Override
    public void initialize(AJAXState state) throws OXException {
        // Nope
    }

    @Override
    public void cleanUp(AJAXState state) throws OXException {
        /*
         * Drop possibly opened mail access instances
         */
        final MailServletInterface mailInterface = state.removeProperty(PROPERTY_MAIL_IFACE);
        if (null != mailInterface) {
            mailInterface.close(true);
        }
        Streams.close(state.<Collection<Closeable>>removeProperty(PROPERTY_CLOSEABLES));
    }

}
