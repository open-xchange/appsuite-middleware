package com.openexchange.calendar.itip;

import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.UserService;

public class ITipCalendarWrapper {

	protected Session session;

	protected Context ctx;

	protected User user;

	protected ServiceLookup services;

	public ITipCalendarWrapper(Session session, ServiceLookup services) {
		super();
		this.session = session;
		this.services = services;
	}

	protected void loadUser() throws OXException {
		if (user != null) {
			return;
		}
		loadContext();
		UserService users = services.getService(UserService.class);
		user = users.getUser(session.getUserId(), ctx);

	}

	protected void loadContext() throws OXException {
		if (ctx != null) {
			return;
		}
		ContextService contexts = services.getService(ContextService.class);
		ctx = contexts.getContext(session.getContextId());

	}

	// Returns the user id for a certain folder, if it is a shared folder, -1
	// otherwise
	protected int onBehalfOf(final int parentFolderID) throws OXException {
		loadContext();
		final OXFolderAccess ofa = new OXFolderAccess(ctx);
		if (!ofa.exists(parentFolderID)) {
		    return -1;
        }
		final int folderType = ofa.getFolderType(parentFolderID, session.getUserId());
		if (folderType == FolderObject.SHARED) {
			return ofa.getFolderOwner(parentFolderID);
		}
		return -1;
	}

}
