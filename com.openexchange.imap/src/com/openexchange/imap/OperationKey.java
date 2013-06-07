/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.imap;

import java.io.Serializable;
import java.util.Arrays;

/**
 * {@link OperationKey} - An operation key.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OperationKey implements Serializable {

    private static final long serialVersionUID = -3628236985679806438L;

    /**
     * The default value associated with a key.
     */
    public static final Object PRESENT = new Object();

    /**
     * Operation type.
     */
    public static enum Type {
        MSG_COPY, MSG_DELETE, MSG_LABEL_UPDATE, MSG_FLAGS_UPDATE, MSG_APPEND, ;
    }

    // --------------------------------------------------------------------------------------- //

    private final Type type;
    private final int accountId;
    private final Object[] objects;
    private final int hash;

    /**
     * Initializes a new {@link OperationKey}.
     */
    public OperationKey(final Type type, final int accountId, final Object... objects) {
        super();
        this.type = type;
        this.accountId = accountId;
        this.objects = objects;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + accountId;
        result = prime * result + Arrays.hashCode(objects);
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OperationKey)) {
            return false;
        }
        final OperationKey other = (OperationKey) obj;
        if (type != other.type) {
            return false;
        }
        if (accountId != other.accountId) {
            return false;
        }
        if (!Arrays.equals(objects, other.objects)) {
            return false;
        }
        return true;
    }

}
