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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.AbstractOXExceptionFactory;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.tools.iterator.SearchIteratorException;

@Deprecated
public class ContactExceptionFactory extends AbstractOXExceptionFactory<ContactException> {

    public ContactExceptionFactory(final Class<?> clazz) {
        super(clazz);
    }

    private static final int CLASS = Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACTS_CONTACTEXCEPTIONFACTORY;

    @Override
    protected ContactException buildException(final EnumComponent component, final Category category, final int number, final String message, final Throwable cause, final Object... msgArgs) {
        return new ContactException(category,number,message,cause,msgArgs);
    }

    @Override
    protected int getClassId() {
        return CLASS;
    }

    public ContactException create(final int id, final Object...msgParams) {
        return createException(id, msgParams);
    }
    public ContactException create(final int id, final Throwable cause, final Object...msgParams) {
        return createException(id,cause, msgParams);
    }

    public OXConflictException createOXConflictException(final int id, final Object...msgParams) {
        return new OXConflictException(createException(id,msgParams));
    }

    public OXConflictException createOXConflictException(final int id, final Throwable cause, final Object...msgParams) {
        return new OXConflictException(createException(id,cause, msgParams));
    }

    public OXObjectNotFoundException createOXObjectNotFoundException(final int id, final Object...msgParams) {
        return new OXObjectNotFoundException(createException(id, msgParams));
    }

    public OXObjectNotFoundException createOXObjectNotFoundException(final int id, final Throwable cause, final Object...msgParams) {
        return new OXObjectNotFoundException(createException(id,cause, msgParams));
    }

    public OXConcurrentModificationException createOXConcurrentModificationException(final int id, final Object...msgParams) {
        return new OXConcurrentModificationException(createException(id,msgParams));
    }

    public OXConcurrentModificationException createOXConcurrentModificationException(final int id, final Throwable cause, final Object...msgParams) {
        return new OXConcurrentModificationException(createException(id,cause, msgParams));
    }

    public OXPermissionException createOXPermissionException(final int id, final Object...msgParams) {
        return new OXPermissionException(createException(id,msgParams));
    }

    public OXPermissionException createOXPermissionException(final int id, final Throwable cause, final Object...msgParams) {
        return new OXPermissionException(createException(id,cause, msgParams));
    }

    public SearchIteratorException createSearchIteratorException(final int id, final Object...msgParams) {
        return new SearchIteratorException(createException(id, msgParams));
    }

    public SearchIteratorException createSearchIteratorException(final int id, final Throwable cause, final Object...msgParams) {
        return new SearchIteratorException(createException(id,cause, msgParams));
    }
}
