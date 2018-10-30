/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package javax.mail.search;

import javax.mail.Folder;
import javax.mail.Message;

/**
 * This class implements comparisons for the X-MAILBOX key.
 *
 * @author Felix Marx
 * @author Thorben Betten
 */
public final class XMailboxTerm extends StringTerm {

    private static final long serialVersionUID = 2472568618055573432L;

    /**
     * Constructor.
     *
     * @param fullName  the full name to search for
     */
    public XMailboxTerm(String fullName) {
	// Note: comparison is case-insensitive
	super(fullName);
    }

    /**
     * The match method.
     *
     * @param msg	the pattern match is applied to this Message's parts
     * @return		<code>true</code> if the pattern match succeeds, otherwise <code>false</code>
     */
    @Override
    public boolean match(Message msg) {
        try {
            Folder folder = msg.getFolder();
            if (null == folder) {
                return false;
            }

            String fullName = folder.getFullName();
            return null == fullName ? false : super.match(fullName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof XMailboxTerm)) {
        return false;
    }
	return super.equals(obj);
    }

}
