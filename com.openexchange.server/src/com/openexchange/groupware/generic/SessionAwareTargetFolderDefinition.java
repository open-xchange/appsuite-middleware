package com.openexchange.groupware.generic;

import com.openexchange.session.Session;

public interface SessionAwareTargetFolderDefinition {

    /**
     * Retrieves the session to use.
     * 
     * @return The user session
     */
    public Session getSession();

}
