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

package com.openexchange.groupware.contact.helpers;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;

/**
 * This switcher is able to convert a given String into a date by
 * interpreting is as a timestamp (type: String holding a long) and
 * then pass it on to its delegate.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactSwitcherForTimestamp extends AbstractContactSwitcherWithDelegate {

    protected Object[] makeDate(final Object... objects) throws NumberFormatException {
        if (objects[1] instanceof String) {
            objects[1] = new Date(Long.parseLong((String) objects[1]));
        } else {
            objects[1] = new Date(((Long) objects[1]).longValue());
        }
        return objects;
    }

    /* CHANGED METHODS */
    @Override
    public Object creationdate(final Object... objects) throws OXException {
        try {
            try {
                return delegate.creationdate(makeDate(objects));
            } catch (NumberFormatException e) {
                return delegate.creationdate(objects);
            }
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "CreationDate");
        }
    }

    @Override
    public Object anniversary(final Object... objects) throws OXException {
        try {
            try {
                return delegate.anniversary(makeDate(objects));
            } catch (NumberFormatException e) {
                return delegate.anniversary(objects);
            }
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "Anniversary");
        }
    }

    @Override
    public Object birthday(final Object... objects) throws OXException {
        try {
            try {
                return delegate.birthday(makeDate(objects));
            } catch (NumberFormatException e) {
                return delegate.birthday(objects);
            }
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "Birthday");
        }
    }

    @Override
    public Object imagelastmodified(final Object... objects) throws OXException {
        try {
            try {
                return delegate.imagelastmodified(makeDate(objects));
            } catch (NumberFormatException e) {
                return delegate.imagelastmodified(objects);
            }
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "ImageLastModified");
        }
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        try {
            try {
                return delegate.lastmodified(makeDate(objects));
            } catch (NumberFormatException e) {
                return delegate.lastmodified(objects);
            }
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "LastModified");
        }
    }
}
