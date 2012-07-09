package com.openexchange.conversion.simple;

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public interface SimplePayloadConverter {
	
	public enum Quality {
		GOOD, BAD
	}
	
	/**
     * Gets the input format.
     *
     * @return The input format
     */
    String getInputFormat();

    /**
     * Gets the output format.
     *
     * @return The output format
     */
    String getOutputFormat();

    /**
     * Gets the quality.
     *
     * @return The quality
     */
    Quality getQuality();

    /**
     * Converts specified request data and result pair using given converter.
     *
     * @param requestData The request data
     * @param result The result
     * @param session The associated session
     * @param converter The converter
     * @throws OXException If conversion fails
     */
    Object convert(Object data, ServerSession session, SimpleConverter converter) throws OXException;
}
