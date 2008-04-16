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

package com.openexchange.groupware;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.AbstractOXException.Category;

public abstract class AbstractOXExceptionFactory<T> {
    private int classId;
    private EnumComponent component;
    
    private static final Log LOG = LogFactory.getLog(AbstractOXExceptionFactory.class);

    private Map<Integer, ExceptionInfo> throwsMap = new HashMap<Integer, ExceptionInfo>();

    private static final class ExceptionInfo {
        public Category category;
        public String message;


        public ExceptionInfo(OXThrows throwsInfo) {
            this.category = throwsInfo.category();
            this.message = throwsInfo.msg();
        }

        public ExceptionInfo(OXThrowsMultiple throwsInfo, int index) {
            if(throwsInfo.category().length <= index) {
            	LOG.fatal("Missing Category for Exceptions near ids "+idList(throwsInfo.exceptionId()));
                throw new IllegalArgumentException("Missing Category for Exceptions near ids "+idList(throwsInfo.exceptionId()));
            }
            if(throwsInfo.msg().length <= index) {
            	LOG.fatal("Missing Message for Exceptions near ids "+idList(throwsInfo.exceptionId()));
            	throw new IllegalArgumentException("Missing Message for Exceptions near ids "+idList(throwsInfo.exceptionId()));
            }
            this.category = throwsInfo.category()[index];
            this.message = throwsInfo.msg()[index];
        }

        private String idList(final int[] is) {
        	final StringBuilder b = new StringBuilder();
            for(int i : is) { b.append(i).append(','); }
            b.setLength(b.length()-1);
            return b.toString();
        }
    }

    protected AbstractOXExceptionFactory(Class<?> clazz) {
    	final OXExceptionSource exceptionSource  = clazz.getAnnotation(OXExceptionSource.class);
        if(exceptionSource == null) {
        	LOG.fatal(clazz+" doesn't seem to be an OXExceptionSource");
            throw new IllegalArgumentException(clazz+" doesn't seem to be an OXExceptionSource");
        }
        classId = exceptionSource.classId();
        component = exceptionSource.component();

        addClass(clazz);

    }

    private void addClass(final Class<?> clazz) {
        OXThrows throwsInfo = clazz.getAnnotation(OXThrows.class);
        addThrows(throwsInfo, clazz);
        OXThrowsMultiple multiple = clazz.getAnnotation(OXThrowsMultiple.class);
        addMultiple(multiple,clazz);


        final Method[] methods = clazz.getDeclaredMethods();
        for(Method method : methods) {
            throwsInfo = method.getAnnotation(OXThrows.class);
            addThrows(throwsInfo, clazz);
            multiple = method.getAnnotation(OXThrowsMultiple.class);
            addMultiple(multiple,clazz);
        }

        for(Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            throwsInfo = constructor.getAnnotation(OXThrows.class);
            addThrows(throwsInfo, clazz);
            multiple = constructor.getAnnotation(OXThrowsMultiple.class);
            addMultiple(multiple,clazz);
        }

        /*for(Class inner : clazz.getDeclaredClasses()) {
            addClass(inner);
        } FIXME */
    }

    private void addMultiple(final OXThrowsMultiple multiple, final Class clazz) {
        if(multiple != null) {
            for(int i = 0; i < multiple.exceptionId().length; i++) {
                if(throwsMap.containsKey(Integer.valueOf(multiple.exceptionId()[i]))) {
                	LOG.fatal("Exception ID "+multiple.exceptionId()[i]+" is used twice in "+clazz.getName());
                    throw new IllegalArgumentException("Exception ID "+multiple.exceptionId()[i]+" is used twice in "+clazz.getName());
                }
                throwsMap.put(Integer.valueOf(multiple.exceptionId()[i]), new ExceptionInfo(multiple,i));
            }
        }
    }

    private void addThrows(final OXThrows throwsInfo, final Class clazz) {
        if(throwsInfo != null) {
            if(throwsMap.containsKey(Integer.valueOf(throwsInfo.exceptionId()))) {
            	LOG.fatal("Exception ID "+throwsInfo.exceptionId()+" is used twice in "+clazz.getName());
                throw new IllegalArgumentException("Exception ID "+throwsInfo.exceptionId()+" is used twice in "+clazz.getName());
            }
            throwsMap.put(Integer.valueOf(throwsInfo.exceptionId()),new ExceptionInfo(throwsInfo));
        }
    }

    public T createException(final int id, final Object... msgParams) {
        return createException(id, null, msgParams);
    }

    public T createException(final int id, final Throwable cause, final Object... msgParams) {
        final ExceptionInfo throwsInfo = throwsMap.get(Integer.valueOf(id));
        if (throwsInfo == null) {
            return buildException(component, Category.CODE_ERROR, getClassId() * 100,
                    "Missing OXException annotation " + id, cause);
        }
        return buildException(component, throwsInfo.category, classId * 100 + id, throwsInfo.message, cause, msgParams);
    }

    protected abstract T buildException(EnumComponent component, Category category, int number, String message, Throwable cause, Object...msgArgs);
    protected abstract int getClassId();
}
