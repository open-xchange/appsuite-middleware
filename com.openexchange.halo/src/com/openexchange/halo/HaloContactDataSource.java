package com.openexchange.halo;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public interface HaloContactDataSource {
	public AJAXRequestResult investigate(HaloContactQuery query, ServerSession session) throws OXException;
	public String getId();
}
