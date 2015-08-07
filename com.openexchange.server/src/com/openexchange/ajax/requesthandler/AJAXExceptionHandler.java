package com.openexchange.ajax.requesthandler;

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public interface AJAXExceptionHandler {
	 void exceptionOccurred(AJAXRequestData requestData, OXException x, ServerSession session);
}
