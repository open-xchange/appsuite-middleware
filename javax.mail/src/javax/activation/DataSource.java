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
 * @(#)DataSource.java	1.11 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.activation;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * The DataSource interface provides the JavaBeans Activation Framework
 * with an abstraction of an arbitrary collection of data.  It
 * provides a type for that data as well as access
 * to it in the form of <code>InputStreams</code> and
 * <code>OutputStreams</code> where appropriate.
 */

public interface DataSource {

    /**
     * This method returns an <code>InputStream</code> representing
     * the data and throws the appropriate exception if it can
     * not do so.  Note that a new <code>InputStream</code> object must be
     * returned each time this method is called, and the stream must be
     * positioned at the beginning of the data.
     *
     * @return an InputStream
     */
    public InputStream getInputStream() throws IOException;

    /**
     * This method returns an <code>OutputStream</code> where the
     * data can be written and throws the appropriate exception if it can
     * not do so.  Note that a new <code>OutputStream</code> object must
     * be returned each time this method is called, and the stream must
     * be positioned at the location the data is to be written.
     *
     * @return an OutputStream
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * This method returns the MIME type of the data in the form of a
     * string. It should always return a valid type. It is suggested
     * that getContentType return "application/octet-stream" if the
     * DataSource implementation can not determine the data type.
     *
     * @return the MIME Type
     */
    public String getContentType();

    /**
     * Return the <i>name</i> of this object where the name of the object
     * is dependant on the nature of the underlying objects. DataSources
     * encapsulating files may choose to return the filename of the object.
     * (Typically this would be the last component of the filename, not an
     * entire pathname.)
     *
     * @return the name of the object.
     */
    public String getName();
}
