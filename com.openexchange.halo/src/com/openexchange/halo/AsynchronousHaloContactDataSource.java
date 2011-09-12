package com.openexchange.halo;

import java.util.concurrent.Future;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;

public interface AsynchronousHaloContactDataSource {
	public Future<HaloData> investigate(HaloContactQuery query, ServerSession session) throws OXException;
}
