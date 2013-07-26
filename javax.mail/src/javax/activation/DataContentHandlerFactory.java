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
 * @(#)DataContentHandlerFactory.java	1.6 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.activation;

/**
 * This interface defines a factory for <code>DataContentHandlers</code>. An
 * implementation of this interface should map a MIME type into an
 * instance of DataContentHandler. The design pattern for classes implementing
 * this interface is the same as for the ContentHandler mechanism used in
 * <code>java.net.URL</code>.
 */

public interface DataContentHandlerFactory {

    /**
     * Creates a new DataContentHandler object for the MIME type.
     *
     * @param mimeType the MIME type to create the DataContentHandler for.
     * @return The new <code>DataContentHandler</code>, or <i>null</i>
     * if none are found.
     */
    public DataContentHandler createDataContentHandler(String mimeType);
}
