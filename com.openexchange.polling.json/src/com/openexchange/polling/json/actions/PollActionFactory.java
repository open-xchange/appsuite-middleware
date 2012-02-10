
package com.openexchange.polling.json.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

public class PollActionFactory implements AJAXActionServiceFactory {

    private final Map<String, PollAction> actions = new HashMap<String, PollAction>(2);

    public PollActionFactory(final ServiceLookup services) {
        actions.put("new", new CreateAction(services));
        actions.put("get", new GetAction(services));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.unmodifiableCollection(actions.values());
    }

}
