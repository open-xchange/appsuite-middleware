/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.jsieve.export.exceptions;

import com.openexchange.jsieve.export.SieveResponse;

/**
 * {@link OXSieveHandlerException}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXSieveHandlerException extends Exception {

	private static final long serialVersionUID = 2990692657778743217L;

	private final String sieveHost;
	private final int sieveHostPort;
    private final SieveResponse response;
    private boolean parseError;
    private boolean authTimeoutError;

    /**
     * Initializes a new {@link OXSieveHandlerException}
     *
     * @param message The message
     * @param sieveHost The sieve host name
     * @param sieveHostPort The sieve host port
     * @param response The {@link SieveResponse}
     */
    public OXSieveHandlerException(final String message, final String sieveHost, final int sieveHostPort, SieveResponse response) {
        super(message);
        this.sieveHost = sieveHost;
        this.sieveHostPort = sieveHostPort;
        this.response = response;
    }

    /**
     * Initializes a new {@link OXSieveHandlerException}
     *
     * @param message The message
     * @param sieveHost The sieve host name
     * @param sieveHostPort The sieve host port
     * @param response The {@link SieveResponse}
     * @param cause The cause
     */
    public OXSieveHandlerException(final String message, final String sieveHost, final int sieveHostPort, SieveResponse response, Throwable cause) {
        super(message, cause);
        this.sieveHost = sieveHost;
        this.sieveHostPort = sieveHostPort;
        this.response = response;
    }

    /**
     * Sets whether this SIEVE exception is caused by a parsing error
     *
     * @param parseError The flag to set
     * @return This <code>OXSieveHandlerException</code> instance
     */
    public OXSieveHandlerException setParseError(final boolean parseError) {
        this.parseError = parseError;
        return this;
    }

    /**
     * Sets whether this SIEVE exception is caused by a timeout during authentication
     *
     * @param authTimeoutError The flag to set
     * @return This <code>OXSieveHandlerException</code> instance
     */
    public OXSieveHandlerException setAuthTimeoutError(boolean authTimeoutError) {
        this.authTimeoutError = authTimeoutError;
        return this;
    }

    /**
     * Signals whether this SIEVE exception is caused by a parsing error
     *
     * @return <code>true</code> for parse error; otherwise <code>false</code>
     */
    public boolean isParseError() {
        return parseError;
    }

    /**
     * Signals whether this SIEVE exception is caused by a timeout during authentication
     *
     * @return <code>true</code> for timeout during authentication; otherwise <code>false</code>
     */
    public boolean isAuthTimeoutError() {
        return authTimeoutError;
    }

	/**
	 * Gets the name of the sieve host for which this exception was thrown
	 *
	 * @return The name of the sieve host for which this exception was thrown
	 */
	public String getSieveHost() {
		return sieveHost;
	}

	/**
	 * Gets the port of the sieve host for which this exception was thrown
	 *
	 * @return The port of the sieve host for which this exception was thrown
	 */
	public int getSieveHostPort() {
		return sieveHostPort;
	}

	/**
     * @return the {@link SieveResponse}
     */
    public final SieveResponse getSieveResponse() {
        return response;
	}
}

