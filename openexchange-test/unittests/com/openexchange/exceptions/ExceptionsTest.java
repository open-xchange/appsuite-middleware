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

import junit.framework.TestCase;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ExceptionsTest extends TestCase {
    private Exceptions<OXTestException> exceptions;
    private Component component;
    private String applicationId;

    public void setUp() {
        this.exceptions = new TestExceptions();
        this.applicationId = "com.openexchange.test";
        this.exceptions.setApplicationId(applicationId);
        this.component = new StringComponent("TST");
        this.exceptions.setComponent(component);
    }


    public void testCreateExceptionByCode() {
        NullPointerException cause = new NullPointerException();
        OXTestException exception = exceptions.create(12, cause, "arg1", "arg2", "arg3");

        assertEquals(12, exception.getDetailNumber());
        assertEquals(component, exception.getComponent());
        assertEquals(AbstractOXException.Category.USER_INPUT, exception.getCategory());
        assertEquals(cause, exception.getCause());
        assertEquals("MESSAGE12", exception.getOrigMessage());
        assertMessageArgs(exception, "arg1", "arg2", "arg3");

        exception = exceptions.create(13, "arg11", "arg12", "arg13", "arg14");

        assertEquals(13, exception.getDetailNumber());
        assertEquals(component, exception.getComponent());
        assertEquals(AbstractOXException.Category.CODE_ERROR, exception.getCategory());
        assertNull(exception.getCause());
        assertEquals("MESSAGE13", exception.getOrigMessage());
        assertMessageArgs(exception, "arg11", "arg12", "arg13", "arg14");


    }

    public void testCreateExceptionByErrorMessage() {
        NullPointerException cause = new NullPointerException();
        OXTestException exception = exceptions.create(errorMessage, cause, "arg11", "arg12", "arg13", "arg14");

        assertEquals(13, exception.getDetailNumber());
        assertEquals(component, exception.getComponent());
        assertEquals(AbstractOXException.Category.CODE_ERROR, exception.getCategory());
        assertEquals(cause, exception.getCause());
        assertEquals("MESSAGE13", exception.getOrigMessage());
        assertMessageArgs(exception, "arg11", "arg12", "arg13", "arg14");

    }

    public void testThrowExceptionByCode() {
        NullPointerException cause = new NullPointerException();
        try {
            exceptions.throwException(12, cause, "arg1", "arg2", "arg3");
            fail("Didn't throw exception");
        } catch (OXTestException exception) {
            assertEquals(12, exception.getDetailNumber());
            assertEquals(component, exception.getComponent());
            assertEquals(AbstractOXException.Category.USER_INPUT, exception.getCategory());
            assertEquals(cause, exception.getCause());
            assertEquals("MESSAGE12", exception.getOrigMessage());
            assertMessageArgs(exception, "arg1", "arg2", "arg3");
        }
    }

    public void testThrowExceptionByErrorMessage() {
        NullPointerException cause = new NullPointerException();
        try {
            exceptions.throwException(errorMessage, cause,"arg11", "arg12", "arg13", "arg14");
            fail("Didn't throw exception");
        } catch (OXTestException exception) {
            assertEquals(13, exception.getDetailNumber());
            assertEquals(component, exception.getComponent());
            assertEquals(AbstractOXException.Category.CODE_ERROR, exception.getCategory());
            assertEquals(cause, exception.getCause());
            assertEquals("MESSAGE13", exception.getOrigMessage());
            assertMessageArgs(exception, "arg11", "arg12", "arg13", "arg14");
        }
    }

    public void testLookupErrorMessage() {
        ErrorMessage msg = exceptions.findMessage(12);
        assertEquals(12, msg.getErrorCode());
        assertEquals(component, msg.getComponent());
        assertEquals(AbstractOXException.Category.USER_INPUT, msg.getCategory());
        assertEquals(applicationId, msg.getApplicationId());
        assertEquals("MESSAGE12", msg.getMessage());
        assertEquals("HELP12", msg.getHelp());

        msg = exceptions.findMessage(13);
        assertEquals(13, msg.getErrorCode());
        assertEquals(component, msg.getComponent());
        assertEquals(AbstractOXException.Category.CODE_ERROR, msg.getCategory());
        assertEquals(applicationId, msg.getApplicationId());
        assertEquals("MESSAGE13", msg.getMessage());
        assertEquals("HELP13", msg.getHelp());
    }

    public void testLookupOXErrorMessage() {
        OXErrorMessage msg = exceptions.findOXErrorMessage(12);
        assertEquals(12, msg.getErrorCode());
        assertEquals(AbstractOXException.Category.USER_INPUT, msg.getCategory());
        assertEquals("MESSAGE12", msg.getMessage());
        assertEquals("HELP12", msg.getHelp());

        msg = exceptions.findOXErrorMessage(13);
        assertEquals(errorMessage, msg);
    }

    public void testGetMessages() {
        Set<ErrorMessage> messages = exceptions.getMessages();
        assertEquals(2, messages.size());

        Set<Integer> codes = new HashSet<Integer>();

        for(ErrorMessage message : messages) { codes.add(message.getErrorCode()); }

        assertTrue(codes.remove(12));
        assertTrue(codes.remove(13));

    }

    public void testUndeclaredException() {
        try {
            exceptions.create(23);
            fail("Should throw exception");
        } catch (UndeclaredErrorCodeException undeclared) {
            assertEquals(23, undeclared.getErrorCode());
            assertEquals(component, undeclared.getComponent());
            assertEquals(applicationId, undeclared.getApplicationId());
        }
    }

    private static void assertMessageArgs(AbstractOXException exception, Object...expected) {
        Object[] actual = exception.getMessageArgs();
        assertEquals(actual.length , expected.length);
        for(int i = 0; i < actual.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }



    private static final OXErrorMessage errorMessage = new OXErrorMessage() {

        public int getErrorCode() {
            return 13;
        }

        public String getMessage() {
            return "MESSAGE13";
        }

        public String getHelp() {
            return "HELP13";
        }

        public AbstractOXException.Category getCategory() {
            return AbstractOXException.Category.CODE_ERROR;
        }
    };


    private static final class OXTestException extends AbstractOXException {
        public OXTestException(ErrorMessage message, Throwable cause, Object...args) {
            super(message.getComponent(), message.getCategory(), message.getErrorCode(), message.getMessage(), cause);
            setMessageArgs(args);
        }
    }

    private static final class TestExceptions extends Exceptions<OXTestException> {

        protected void knownExceptions() {
            declare(12, AbstractOXException.Category.USER_INPUT, "MESSAGE12", "HELP12");
            declare(errorMessage);
        }

        protected OXTestException createException(ErrorMessage message, Throwable cause, Object... args) {
            return new OXTestException(message, cause, args);
        }
    }

}
