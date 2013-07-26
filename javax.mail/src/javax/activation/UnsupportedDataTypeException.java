/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * @(#)UnsupportedDataTypeException.java	1.9 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.activation;

import java.io.IOException;

/**
 * Signals that the requested operation does not support the
 * requested data type.
 *
 * @see javax.activation.DataHandler
 */

public class UnsupportedDataTypeException extends IOException {
    /**
     * Constructs an UnsupportedDataTypeException with no detail
     * message.
     */
    public UnsupportedDataTypeException() {
	super();
    }

    /**
     * Constructs an UnsupportedDataTypeException with the specified 
     * message.
     * 
     * @param s The detail message.
     */
    public UnsupportedDataTypeException(String s) {
	super(s);
    }
}
