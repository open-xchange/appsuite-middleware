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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.imap.acl;

import com.sun.mail.imap.Rights;

/**
 * {@link ReadOnlyRights} - A {@link Rights} object providing read-only access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ReadOnlyRights extends Rights {

    private boolean constructed = false;

    /**
     * Initializes a new {@link ReadOnlyRights}.
     *
     * @param rights The rights for initialization
     */
    ReadOnlyRights(final Rights rights) {
        super(rights);
        constructed = true;
    }

    /**
     * Initializes a new {@link ReadOnlyRights}.
     *
     * @param rights The rights for initialization
     */
    ReadOnlyRights(final String rights) {
        super(rights);
        constructed = true;
    }

    /**
     * Initializes a new {@link ReadOnlyRights}.
     *
     * @param right The right for initialization
     */
    ReadOnlyRights(final Right right) {
        super(right);
        constructed = true;
    }

    /**
     * Adds the specified right to this rights.
     *
     * @param right The right to add
     */
    @Override
    public void add(final Right right) {
        if (constructed) {
            throw new UnsupportedOperationException();
        }
        super.add(right);
    }

    /**
     * Adds all the rights in the given rights to this rights.
     *
     * @param rights The rights to add
     */
    @Override
    public void add(final Rights rights) {
        if (constructed) {
            throw new UnsupportedOperationException();
        }
        super.add(rights);
    }

    /**
     * Removes the specified right from this rights.
     *
     * @param right The right to be removed
     */
    @Override
    public void remove(final Right right) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all rights in the given rights from this rights.
     *
     * @param rights The rights to be removed
     */
    @Override
    public void remove(final Rights rights) {
        throw new UnsupportedOperationException();
    }

}
