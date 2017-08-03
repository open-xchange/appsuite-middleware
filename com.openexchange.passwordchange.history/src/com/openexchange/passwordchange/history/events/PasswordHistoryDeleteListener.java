
package com.openexchange.passwordchange.history.events;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.passwordchange.history.handler.PasswordChangeHandlerRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

public class PasswordHistoryDeleteListener implements DeleteListener {

    private final ServiceLookup        service;
    private final PasswordChangeHelper helper;

    public PasswordHistoryDeleteListener(ServiceLookup service, PasswordChangeHandlerRegistry registry) {
        super();
        this.service = service;
        this.helper = new PasswordChangeHelper(service, registry);
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {

        // Only context and user are relevant
        switch (event.getType()) {
            case DeleteEvent.TYPE_CONTEXT:
                // Get users in context and remove password for them
                UserService userService = service.getService(UserService.class);
                if (null == userService) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
                }
                Context ctx = userService.getContext(event.getId());
                for (User user : userService.getUser(ctx)) {
                    helper.clearFor(event.getContext().getContextId(), user.getId(), -1);
                }
                break;

            case DeleteEvent.TYPE_USER:
                helper.clearFor(event.getContext().getContextId(), event.getId(), -1);
                break;

            default:
                // Ignore all other
                return;
        }
    }
}
