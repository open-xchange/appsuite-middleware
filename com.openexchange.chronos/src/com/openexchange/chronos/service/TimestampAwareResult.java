package com.openexchange.chronos.service;

import java.util.Date;

public interface TimestampAwareResult {
	
	/**
     * Gets the updated server timestamp as used as new/updated last-modification date of the modified data in storage, which is usually
     * also returned to clients.
     *
     * @return The server timestamp
     */
    Date getTimestamp();

}
