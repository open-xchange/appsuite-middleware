package com.openexchange.halo;

import java.util.List;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.session.ServerSession;

public interface ContactHalo {

	public abstract AJAXRequestResult investigate(String provider,
			Contact contact, AJAXRequestData req, ServerSession session) throws OXException;

	public abstract List<String> getProviders(ServerSession session) throws OXException;

}
