
package com.openexchange.mail.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.AJAXStateHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.actions.AllAction;
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
import com.openexchange.mail.json.actions.SearchAction;
import com.openexchange.mail.json.actions.TransportMailAction;
import com.openexchange.mail.json.actions.UpdateAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailActionFactory}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailActionFactory implements AJAXActionServiceFactory, AJAXStateHandler, MailActionConstants {

    private final Map<String, AbstractMailAction> actions;

    /**
     * Initializes a new {@link MailActionFactory}.
     * 
     * @param services The service look-up
     */
    public MailActionFactory(final ServiceLookup services) {
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
    }

    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    public void begin(final AJAXState state) throws OXException {
        // Nope
    }

    public void end(final AJAXState state) throws OXException {
        /*
         * Drop possibly opened mail access instances
         */
        final MailServletInterface mailInterface = state.optProperty(PROPERTY_MAIL_IFACE);
        if (null != mailInterface) {
            mailInterface.close(true);
        }
    }

}
