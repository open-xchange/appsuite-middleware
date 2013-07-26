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
 * @(#)CommandObject.java	1.9 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.activation;

import java.io.IOException;

/**
 * JavaBeans components that are Activation Framework aware implement
 * this interface to find out which command verb they're being asked
 * to perform, and to obtain the DataHandler representing the
 * data they should operate on.  JavaBeans that don't implement
 * this interface may be used as well.  Such commands may obtain
 * the data using the Externalizable interface, or using an
 * application-specific method.<p>
 */
public interface CommandObject {

    /**
     * Initialize the Command with the verb it is requested to handle
     * and the DataHandler that describes the data it will
     * operate on. <b>NOTE:</b> it is acceptable for the caller
     * to pass <i>null</i> as the value for <code>DataHandler</code>.
     *
     * @param verb The Command Verb this object refers to.
     * @param dh The DataHandler.
     */
    public void setCommandContext(String verb, DataHandler dh)
						throws IOException;
}
