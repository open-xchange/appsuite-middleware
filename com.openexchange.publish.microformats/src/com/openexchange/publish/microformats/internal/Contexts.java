package com.openexchange.publish.microformats.internal;

import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;


public class Contexts {
    
    private static ContextService contextService;
    
    public static Context load(int contextId) throws ContextException {
        return contextService.getContext(contextId);
    }

    public static void setContextService(ContextService service) {
        contextService = service;
    }
}
