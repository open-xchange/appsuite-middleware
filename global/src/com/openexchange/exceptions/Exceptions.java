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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class Exceptions<T extends AbstractOXException> {

    
    private final Map<Integer, ErrorMessage> errors = new HashMap<Integer, ErrorMessage>();

    private Component component;

    private String applicationId;

    private boolean initialized;

    public Component getComponent() {
        return component;
    }

    public void setComponent(final Component component) {
        this.component = component;
        initialize();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
        initialize();
    }

    private void initialize() {
        if (null != component && null != applicationId) {
            knownExceptions();
            initialized = true;
        }
    }

    protected void declare(final int code, final AbstractOXException.Category category, final String message, final String help) {
        errors.put(Integer.valueOf(code), new ErrorMessage(code, component, applicationId, category, message, help));
    }

    protected void declare(final OXErrorMessage error) {
        errors.put(Integer.valueOf(error.getDetailNumber()), new ErrorMessage(error, component, applicationId));
    }

    protected void declareAll(final OXErrorMessage[] errors) {
        for (final OXErrorMessage error : errors) {
            declare(error);
        }
    }

    protected void declareAll(final Iterable<OXErrorMessage> errors) {
        for (final OXErrorMessage error : errors) {
            declare(error);
        }
    }

    /**
     * Override this method and declare all your exceptions. This method must call at least one of the methods
     * {@link #declare(OXErrorMessage)}, {@link #declare(int, com.openexchange.groupware.AbstractOXException.Category, String, String)},
     * {@link #declareAll(Iterable)}, {@link #declareAll(OXErrorMessage[])}.
     */
    protected abstract void knownExceptions();

    protected abstract T createException(ErrorMessage message, Throwable cause, Object... args);

    public T create(final int code, final Throwable cause, final Object... args) {
        final ErrorMessage errorMessage = errors.get(Integer.valueOf(code));
        if (errorMessage == null) {
            if(initialized) {
                throw new UndeclaredErrorCodeException(code, getApplicationId(), getComponent());
            } else {
                System.err.println("Apparently this exception factory was not regsitered.: "+this);
                setComponent(new StringComponent("???"));
                setApplicationId("unset");
            }
        }
        return createException(errorMessage, cause, args);
    }

    public T create(final int code, final Object... args) {
        return create(code, null, args);
    }

    public void throwException(final int code, final Object... args) throws T {
        throw create(code, args);
    }

    public void throwException(final int code, final Throwable cause, final Object... args) throws T {
        throw create(code, cause, args);
    }

    public T create(final OXErrorMessage message, final Object... args) {
        return create(message.getDetailNumber(), args);
    }

    public T create(final OXErrorMessage message, final Throwable cause, final Object... args) {
        return create(message.getDetailNumber(), cause, args);
    }

    public void throwException(final OXErrorMessage message, final Object... args) throws T {
        throw create(message, args);
    }

    public void throwException(final OXErrorMessage message, final Throwable cause, final Object... args) throws T {
        throw create(message, cause, args);
    }

    public SortedSet<ErrorMessage> getMessages() {
        return new TreeSet<ErrorMessage>(errors.values());
    }

    public ErrorMessage findMessage(final int code) {
        return errors.get(Integer.valueOf(code));
    }

    public OXErrorMessage findOXErrorMessage(final int code) {
        return findMessage(code).getOXErrorMessage();
    }

}
