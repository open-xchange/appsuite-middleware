/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
    ReadOnlyRights(Rights rights) {
        super(rights);
        constructed = true;
    }

    /**
     * Initializes a new {@link ReadOnlyRights}.
     *
     * @param rights The rights for initialization
     */
    ReadOnlyRights(String rights) {
        super(rights);
        constructed = true;
    }

    /**
     * Initializes a new {@link ReadOnlyRights}.
     *
     * @param right The right for initialization
     */
    ReadOnlyRights(Right right) {
        super(right);
        constructed = true;
    }

    /**
     * Adds the specified right to this rights.
     *
     * @param right The right to add
     */
    @Override
    public void add(Right right) {
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
    public void add(Rights rights) {
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
    public void remove(Right right) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all rights in the given rights from this rights.
     *
     * @param rights The rights to be removed
     */
    @Override
    public void remove(Rights rights) {
        throw new UnsupportedOperationException();
    }

}
