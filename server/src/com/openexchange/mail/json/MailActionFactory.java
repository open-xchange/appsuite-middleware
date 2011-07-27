
package com.openexchange.mail.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.AJAXStateHandler;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailActionFactory}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailActionFactory implements AJAXActionServiceFactory, AJAXStateHandler {

    private final Map<String, AbstractMailAction> actions;

    /**
     * Initializes a new {@link MailActionFactory}.
     * 
     * @param services The service look-up
     */
    public MailActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AbstractMailAction>(8);
        actions.put("get", new GetAction(services));
    }

    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    public void begin(final AJAXState state) throws OXException {
        // Nope
    }

    public void end(final AJAXState state) throws OXException {
        /*
         * Drop opened mail access instances
         */
        
    }

}
