
package com.openexchange.passwordchange;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link PasswordChangeEvent} - Event for password change containing the session of the user whose password shall be changed, the context,
 * the new password, and the old password (needed for verification)
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PasswordChangeEvent {

    private final Session session;

    private final Context ctx;

    private final String newPassword;

    private final String oldPassword;

    /**
     * Initializes a new {@link PasswordChangeEvent}
     * 
     * @param session The session of the user whose password shall be changed
     * @param ctx The context
     * @param newPassword The new password
     * @param oldPassword The old password (needed for verification)
     */
    public PasswordChangeEvent(final Session session, final Context ctx, final String newPassword, final String oldPassword) {
        this.session = session;
        this.ctx = ctx;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    /**
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * @return The new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @return The old password
     */
    public String getOldPassword() {
        return oldPassword;
    }
}
