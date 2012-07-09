package com.openexchange.conversion.simple;

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * A simple conversion service
 */
public interface SimpleConverter {

	/**
	 * Convert "data" from a certain format to a certain format
	 * @param from The format that data is in
	 * @param to The format the system should try to convert its entries to
	 * @param data the data
	 * @param session the session
	 * @return the converted data in "to" format
	 * @throws OXException if something goes horribly wrong
	 */
	public Object convert(String from, String to, Object data, ServerSession session) throws OXException;
	
}
