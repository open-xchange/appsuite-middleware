
package com.openexchange.authentication.acs.impl;

import javax.security.auth.login.LoginException;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;

/**
 * Login implementation for ACS.
 * @author <a href="mailto:benjamin.otterbach@open-xchange.org">Benjamin Otterbach</a>
 */
public class AcsAuthentication implements AuthenticationService {

    /**
     * Default constructor.
     */
    public AcsAuthentication() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo)	throws OXException {
        final String password = loginInfo.getPassword();
        if (null == password || 0 == password.length()) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }

        final String login = loginInfo.getUsername();
        final String[] changedLogin;
        if (login.indexOf('@') != -1) {
            final String[] splitted = split(loginInfo.getUsername());
            final String contextInfo = splitted[0];
            final String userInfo = splitted[1];
            changedLogin = doLogin(contextInfo, userInfo, password);
        } else if (login.indexOf('.') != -1) {
            final String[] splitted = split(login, '.');
            final String contextInfo = splitted[1];
            final String userInfo = splitted[0];
            changedLogin = doLogin(contextInfo, userInfo, password);
        } else {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }
        return new Authenticated() {
            @Override
            public String getContextInfo() {
                return changedLogin[0];
            }
            @Override
            public String getUserInfo() {
                return changedLogin[1];
            }
        };

    }

    /**
     * This method performs the login.
     * @param contextInfo login context information.
     * @param userInfo login user information.
     * @param password login password.
     * @return the string array for the {@link #handleLoginInfo(Object[])}
     * method.
     * @throws OXException if the authentication fails.
     */
    private String[] doLogin(final String contextInfo, final String userInfo, final String password) throws OXException {
        final ContextStorage ctxStor = ContextStorage.getInstance();
        final int ctxId = ctxStor.getContextId(contextInfo);
        if (ContextStorage.NOT_FOUND == ctxId) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }
        final Context ctx = ctxStor.getContext(ctxId);
        final UserStorage userStor = UserStorage.getInstance();
        final int userId;
        try {
            userId = userStor.getUserId(userInfo, ctx);
        } catch (final OXException e) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        }
        final User user = userStor.getUser(userId, ctx);
        if (!userStor.authenticate(user, password)) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }
        return new String[] { contextInfo, userInfo };
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @param separator for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted = new String[2];
        if (-1 == pos) {
            splitted[1] = loginInfo;
            splitted[0] = "defaultcontext";
        } else {
            splitted[1] = loginInfo.substring(0, pos);
            splitted[0] = loginInfo.substring(pos + 1);
        }
        return splitted;
    }
}
