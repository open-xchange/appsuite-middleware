package com.openexchange.mailaccount.json.servlet;

import com.openexchange.ajax.MultipleAdapterServletNew;
import com.openexchange.mailaccount.json.actions.MailAccountActionFactory;
import com.openexchange.tools.session.ServerSession;


public class MailAccountServlet extends MultipleAdapterServletNew {

    private static final long serialVersionUID = -2969154857342400038L;

    public MailAccountServlet() {
        super(MailAccountActionFactory.getInstance());
    }

    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return session.getUserConfiguration().hasWebMail();
    }

}
