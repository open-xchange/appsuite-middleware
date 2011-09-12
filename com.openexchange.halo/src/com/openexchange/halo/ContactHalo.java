package com.openexchange.halo;

import java.util.List;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.session.ServerSession;

public interface ContactHalo {

	public abstract List<Object> investigate(Contact contact,
			ServerSession session) throws OXException;

	public abstract HaloData resolveToken(String token, ServerSession session)
			throws OXException;
}