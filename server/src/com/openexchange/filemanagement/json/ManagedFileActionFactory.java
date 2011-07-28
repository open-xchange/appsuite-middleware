
package com.openexchange.filemanagement.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.actions.AllAction;
import com.openexchange.mail.json.actions.ClearAction;
import com.openexchange.mail.json.actions.DeleteAction;
import com.openexchange.mail.json.actions.GetAction;
import com.openexchange.mail.json.actions.GetAttachmentAction;
import com.openexchange.mail.json.actions.GetForwardAction;
import com.openexchange.mail.json.actions.GetMailCountAction;
import com.openexchange.mail.json.actions.GetMultipleAttachmentAction;
import com.openexchange.mail.json.actions.GetMultipleMessagesAction;
import com.openexchange.mail.json.actions.GetReplyAction;
import com.openexchange.mail.json.actions.GetReplyAllAction;
import com.openexchange.mail.json.actions.GetStructureAction;
import com.openexchange.mail.json.actions.GetUpdatesAction;
import com.openexchange.mail.json.actions.GetVersitAction;
import com.openexchange.mail.json.actions.ListAction;
import com.openexchange.mail.json.actions.NewAction;
import com.openexchange.mail.json.actions.ReceiptAckAction;
import com.openexchange.mail.json.actions.SearchAction;
import com.openexchange.mail.json.actions.TransportMailAction;
import com.openexchange.mail.json.actions.UpdateAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ManagedFileActionFactory}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ManagedFileActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AbstractMailAction> actions;

    /**
     * Initializes a new {@link ManagedFileActionFactory}.
     * 
     * @param services The service look-up
     */
    public ManagedFileActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AbstractMailAction>(8);
        actions.put("all", new AllAction(services));
        actions.put("get", new GetAction(services));
        actions.put("get_structure", new GetStructureAction(services));
        actions.put("count", new GetMailCountAction(services));
        actions.put("reply", new GetReplyAction(services));
        actions.put("replyall", new GetReplyAllAction(services));
        actions.put("updates", new GetUpdatesAction(services));
        actions.put("forward", new GetForwardAction(services));
        actions.put("attachment", new GetAttachmentAction(services));
        actions.put("zip_attachments", new GetMultipleAttachmentAction(services));
        actions.put("zip_messages", new GetMultipleMessagesAction(services));
        actions.put("saveVersit", new GetVersitAction(services));

        actions.put("list", new ListAction(services));
        actions.put("search", new SearchAction(services));
        actions.put("update", new UpdateAction(services));
        actions.put("delete", new DeleteAction(services));
        actions.put("transport", new TransportMailAction(services));
        actions.put("receipt_ack", new ReceiptAckAction(services));
        actions.put("clear", new ClearAction(services));
        actions.put("new", new NewAction(services));
    }

    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

}
