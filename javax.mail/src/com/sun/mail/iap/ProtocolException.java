/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.iap;

/**
 * @author John Mani
 */

public class ProtocolException extends Exception {
    protected transient Response response = null;

    private static final long serialVersionUID = -4360500807971797439L;

    private final String responseCode;
    private final String rest;
    private final ResponseCode knownResponseCode;

    /**
     * Constructs a ProtocolException with no detail message.
     */
    public ProtocolException() {
	super();
	responseCode = null;
	rest = null;
	knownResponseCode = null;
    }

    /**
     * Constructs a ProtocolException with the specified detail message.
     *
     * @param message		the detail message
     */
    public ProtocolException(String message) {
	super(message);
    responseCode = null;
    rest = null;
    knownResponseCode = null;
    }

    /**
     * Constructs a ProtocolException with the specified detail message
     * and cause.
     *
     * @param message		the detail message
     * @param cause		the cause
     */
    public ProtocolException(String message, Throwable cause) {
	super(message, cause);
    responseCode = null;
    rest = null;
    knownResponseCode = null;
    }

    /**
     * Constructs a ProtocolException with the specified Response object.
     *
     * @param	r	the Response
     */
    public ProtocolException(Response r) {
	super(r.toString());
	response = r;

	String responseCode = null;
	String rest = null;
	String s = r.getRest();    // get the text after the response
	if (s.startsWith("[")) {   // a response code
	    int i = s.indexOf(']');
        if (i > 0) {
            responseCode = s.substring(0, i + 1);
            rest = s.substring(i + 1).trim();
        }
	}
	this.responseCode = responseCode;
	this.rest = rest;
	this.knownResponseCode = ResponseCode.responseCodeFor(responseCode);
    }

    /**
     * Gets the response code from offending Response object.
     * <p>
     * E.g. <code>"NO [INUSE] Mailbox in use"</code>; response code is <code>[INUSE]</code>.
     *
     * @return The response code or <code>null</code>
     * @see #getKnownResponseCode()
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Gets the rest of the response as a string, usually used to return the arbitrary message text after a NO response.
     * <p>
     * This method omits the response code (if any)
     *
     * @return The rest of the response as a string
     */
    public String getResponseRest() {
        return rest;
    }

    /**
     * Gets the known response code (according to <a href="https://tools.ietf.org/html/rfc5530">RFC 5530</a>) from offending Response object.
     * <p>
     * <b>Note:</b> Even if this method returns <code>null</code>, there might nevertheless be a response code available. Check {@link #getResponseCode()} for evidence.
     *
     * @return The known response code or <code>null</code>
     * @see #getResponseCode()
     */
    public ResponseCode getKnownResponseCode() {
        return knownResponseCode;
    }

    /**
     * Return the offending Response object.
     *
     * @return	the Response object
     */
    public Response getResponse() {
	return response;
    }
}
