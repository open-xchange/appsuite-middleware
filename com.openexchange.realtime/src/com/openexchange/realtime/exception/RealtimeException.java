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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link RealtimeException}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class RealtimeException extends OXException {

    private static final long serialVersionUID = -3650839506266250736L;

    private OXException delegate;

    private Transformer transformer;

    public RealtimeException(OXException origin) {
        this.delegate = origin;
    }

    public RealtimeException(OXException origin, Transformer transformer) {
        this.delegate = origin;
        this.transformer = transformer;
    }

    public RealtimeException toXMPPException() {
        if (!getPrefix().equals("RT")) {
            return this;
        }

        return RealtimeExceptionFactory.getInstance().create(transformer.getXMPP());
    }

    public RealtimeException toAtmosphereException() {
        if (!this.getPrefix().equals("RT")) {
            return this;
        }

        return RealtimeExceptionFactory.getInstance().create(transformer.getAtmosphere());
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public String getLocalizedMessage() {
        return delegate.getLocalizedMessage();
    }

    public Throwable getCause() {
        return delegate.getCause();
    }

    public void copyFrom(OXException e) {
        delegate.copyFrom(e);
    }

    public Throwable initCause(Throwable cause) {
        return delegate.initCause(cause);
    }

    public int getCode() {
        return delegate.getCode();
    }

    public Generic getGeneric() {
        return delegate.getGeneric();
    }

    public boolean isGeneric(Generic generic) {
        return delegate.isGeneric(generic);
    }

    public boolean isNotFound() {
        return delegate.isNotFound();
    }

    public boolean isNoPermission() {
        return delegate.isNoPermission();
    }

    public boolean isMandatory() {
        return delegate.isMandatory();
    }

    public boolean isConflict() {
        return delegate.isConflict();
    }

    public OXException setGeneric(Generic generic) {
        return delegate.setGeneric(generic);
    }

    public void printStackTrace() {
        delegate.printStackTrace();
    }

    public OXException setLogMessage(String logMessage) {
        return delegate.setLogMessage(logMessage);
    }

    public OXException setLogMessage(String displayFormat, Object... args) {
        return delegate.setLogMessage(displayFormat, args);
    }

    public OXException setDisplayMessage(String displayMessage, Object... displayArgs) {
        return delegate.setDisplayMessage(displayMessage, displayArgs);
    }

    public String getPlainLogMessage() {
        return delegate.getPlainLogMessage();
    }

    public Object[] getLogArgs() {
        return delegate.getLogArgs();
    }

    public Object[] getDisplayArgs() {
        return delegate.getDisplayArgs();
    }

    public void log(Log log) {
        delegate.log(log);
    }

    public String getLogMessage(LogLevel logLevel) {
        return delegate.getLogMessage(logLevel);
    }

    public String getLogMessage(LogLevel logLevel, String defaultLog) {
        return delegate.getLogMessage(logLevel, defaultLog);
    }

    public void printStackTrace(PrintStream s) {
        delegate.printStackTrace(s);
    }

    public String getLogMessage() {
        return delegate.getLogMessage();
    }

    public void printStackTrace(PrintWriter s) {
        delegate.printStackTrace(s);
    }

    public String getSoleMessage() {
        return delegate.getSoleMessage();
    }

    public boolean isLoggable(LogLevel logLevel) {
        return delegate.isLoggable(logLevel);
    }

    public Category getCategory() {
        return delegate.getCategory();
    }

    public Throwable fillInStackTrace() {
        return delegate.fillInStackTrace();
    }

    public List<Category> getCategories() {
        return delegate.getCategories();
    }

    public StackTraceElement[] getStackTrace() {
        return delegate.getStackTrace();
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        delegate.setStackTrace(stackTrace);
    }

    public OXException setCategory(Category category) {
        return delegate.setCategory(category);
    }

    public void removeCategory(Category category) {
        delegate.removeCategory(category);
    }

    public String getExceptionId() {
        return delegate.getExceptionId();
    }

    public void setExceptionId(String exceptionId) {
        delegate.setExceptionId(exceptionId);
    }

    public String getPrefix() {
        return delegate.getPrefix();
    }

    public boolean isPrefix(String expected) {
        return delegate.isPrefix(expected);
    }

    public OXException setPrefix(String prefix) {
        return delegate.setPrefix(prefix);
    }

    public String getDisplayMessage(Locale locale) {
        return delegate.getDisplayMessage(locale);
    }

    public String getMessage() {
        return delegate.getMessage();
    }

    public String toString() {
        return delegate.toString();
    }

    public void addTruncatedId(int truncatedId) {
        delegate.addTruncatedId(truncatedId);
    }

    public void addProblematic(ProblematicAttribute problematic) {
        delegate.addProblematic(problematic);
    }

    public boolean containsProperty(String name) {
        return delegate.containsProperty(name);
    }

    public String getProperty(String name) {
        return delegate.getProperty(name);
    }

    public String setProperty(String name, String value) {
        return delegate.setProperty(name, value);
    }

    public OXException setSessionProperties(Session session) {
        return delegate.setSessionProperties(session);
    }

    public String remove(String name) {
        return delegate.remove(name);
    }

    public Set<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

}
