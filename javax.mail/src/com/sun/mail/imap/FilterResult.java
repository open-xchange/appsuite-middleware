/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.mail.imap;

/**
 * {@link FilterResult} - Indicates whether a filter was successfully applied to a message.
 *
 * @author Thorben Betten
 */
public class FilterResult {

    private final long uid;
    private final String errors;
    private final String warnings;

    /**
     * Initializes a new {@link FilterResult}.
     * 
     * @param uid The optional UID or <code>-1</code>
     * @param errors The possible errors
     * @param warnings The possible warnings
     */
    public FilterResult(long uid, String errors, String warnings) {
        super();
        this.uid = uid < 0 ? -1 : uid;
        this.errors = errors;
        this.warnings = warnings;
    }
    
    /**
     * Checks if the filter was applied successfully.
     *
     * @return <code>true</code> if filter was applied successfully; otherwise <code>false</code>
     */
    public boolean isOK() {
        return null == errors && null == warnings;
    }
    
    /**
     * Checks if the filter was applied successfully, but there were one or more warnings produced by the filter.
     *
     * @return <code>true</code> if warnings exist; otherwise <code>false</code>
     */
    public boolean hasWarnings() {
        return null != warnings;
    }
    
    /**
     * Checks if application of the filter failed for some reason.
     *
     * @return <code>true</code> if filter failed; otherwise <code>false</code>
     */
    public boolean hasErrors() {
        return null != errors;
    }

    /**
     * Gets the UID
     *
     * @return The UID or <code>-1</code>
     */
    public long getUid() {
        return uid;
    }

    /**
     * Gets the a human-readable descriptive text listing the encountered errors
     *
     * @return The errors
     */
    public String getErrors() {
        return errors;
    }

    /**
     * Gets the a human-readable descriptive text listing the produced warnings.
     *
     * @return The warnings
     */
    public String getWarnings() {
        return warnings;
    }

}
