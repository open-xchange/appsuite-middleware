package com.openexchange.halo;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;

public interface HaloContactDataSource {
	public HaloData investigate(HaloContactQuery query, ServerSession session) throws OXException;
}
