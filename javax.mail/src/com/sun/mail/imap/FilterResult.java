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

import com.sun.mail.imap.filter.ErrorsFilterResult;
import com.sun.mail.imap.filter.OkFilterResult;
import com.sun.mail.imap.filter.WarningsFilterResult;

/**
 * {@link FilterResult} - Indicates whether a filter was successfully applied to a message.
 *
 * @author Thorben Betten
 */
public abstract class FilterResult {

    private static final FilterResult SIMPLE_OK = new OkFilterResult(-1);

    /**
     * Creates an OK filter result.
     *
     * @param uid The optional UID
     * @return The OK filter result
     */
    public static FilterResult okResult(long uid) {
        return uid < 0 ? SIMPLE_OK : new OkFilterResult(uid);
    }

    /**
     * Creates a WARNINGS filter result.
     *
     * @param uid The optional UID
     * @return The WARNINGS filter result
     */
    public static FilterResult warningsResult(String warnings, long uid) {
        return new WarningsFilterResult(uid, warnings);
    }

    /**
     * Creates an ERRORS filter result.
     *
     * @param uid The optional UID
     * @return The ERRORS filter result
     */
    public static FilterResult errorsResult(String errors, long uid) {
        return new ErrorsFilterResult(uid, errors);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    protected final long uid;

    /**
     * Initializes a new {@link FilterResult}.
     *
     * @param uid The optional UID or <code>-1</code>
     */
    protected FilterResult(long uid) {
        super();
        this.uid = uid < 0 ? -1 : uid;
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
     * Checks if the filter was applied successfully.
     *
     * @return <code>true</code> if filter was applied successfully; otherwise <code>false</code>
     */
    public abstract boolean isOK();

    /**
     * Checks if the filter was applied successfully, but there were one or more warnings produced by the filter.
     *
     * @return <code>true</code> if warnings exist; otherwise <code>false</code>
     */
    public abstract boolean hasWarnings();

    /**
     * Checks if application of the filter failed for some reason.
     *
     * @return <code>true</code> if filter failed; otherwise <code>false</code>
     */
    public abstract boolean hasErrors();

    /**
     * Gets the a human-readable descriptive text listing the encountered errors
     *
     * @return The errors
     */
    public abstract String getErrors();

    /**
     * Gets the a human-readable descriptive text listing the produced warnings.
     *
     * @return The warnings
     */
    public abstract String getWarnings();

}
