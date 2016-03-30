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
            } catch (final NumberFormatException e) {
                return delegate.creationdate(objects);
            }
        } catch (final ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "CreationDate");
        }
    }

    @Override
    public Object anniversary(final Object... objects) throws OXException {
        try {
            try {
                return delegate.anniversary(makeDate(objects));
            } catch (final NumberFormatException e) {
                return delegate.anniversary(objects);
            }
        } catch (final ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "Anniversary");
        }
    }

    @Override
    public Object birthday(final Object... objects) throws OXException {
        try {
            try {
                return delegate.birthday(makeDate(objects));
            } catch (final NumberFormatException e) {
                return delegate.birthday(objects);
            }
        } catch (final ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "Birthday");
        }
    }

    @Override
    public Object imagelastmodified(final Object... objects) throws OXException {
        try {
            try {
                return delegate.imagelastmodified(makeDate(objects));
            } catch (final NumberFormatException e) {
                return delegate.imagelastmodified(objects);
            }
        } catch (final ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "ImageLastModified");
        }
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        try {
            try {
                return delegate.lastmodified(makeDate(objects));
            } catch (final NumberFormatException e) {
                return delegate.lastmodified(objects);
            }
        } catch (final ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "LastModified");
        }
    }
}
