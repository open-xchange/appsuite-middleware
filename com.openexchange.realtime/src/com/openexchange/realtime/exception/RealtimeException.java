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

package com.openexchange.realtime.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
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

    private final OXException delegate;
    private final Transformer transformer;
    private final int hash;

    public RealtimeException(OXException origin) {
        this(origin, null);
    }

    public RealtimeException(OXException origin, Transformer transformer) {
        this.delegate = origin;
        this.transformer = transformer;
        super.copyFrom(origin);

        int prime = 31;
        int result = prime * 1 + ((delegate == null) ? 0 : delegate.hashCode());
        result = prime * result + ((transformer == null) ? 0 : transformer.hashCode());
        hash = result;
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

    @Override
    public String getLocalizedMessage() {
        return delegate.getLocalizedMessage();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RealtimeException)) {
            return false;
        }
        RealtimeException other = (RealtimeException) obj;
        if (delegate == null) {
            if (other.delegate != null) {
                return false;
            }
        } else if (!delegate.equals(other.delegate)) {
            return false;
        }
        if (transformer == null) {
            if (other.transformer != null) {
                return false;
            }
        } else if (!transformer.equals(other.transformer)) {
            return false;
        }
        return true;
    }

    @Override
    public Throwable getCause() {
        return delegate.getCause();
    }

    @Override
    public void copyFrom(OXException e) {
        delegate.copyFrom(e);
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return delegate.initCause(cause);
    }

    @Override
    public int getCode() {
        return delegate.getCode();
    }

    @Override
    public Generic getGeneric() {
        return delegate.getGeneric();
    }

    @Override
    public boolean isGeneric(Generic generic) {
        return delegate.isGeneric(generic);
    }

    @Override
    public boolean isNotFound() {
        return delegate.isNotFound();
    }

    @Override
    public boolean isNoPermission() {
        return delegate.isNoPermission();
    }

    @Override
    public boolean isMandatory() {
        return delegate.isMandatory();
    }

    @Override
    public boolean isConflict() {
        return delegate.isConflict();
    }

    @Override
    public OXException setGeneric(Generic generic) {
        return delegate.setGeneric(generic);
    }

    @Override
    public void printStackTrace() {
        delegate.printStackTrace();
    }

    @Override
    public OXException setLogMessage(String logMessage) {
        return delegate.setLogMessage(logMessage);
    }

    @Override
    public OXException setLogMessage(String displayFormat, Object... args) {
        return delegate.setLogMessage(displayFormat, args);
    }

    @Override
    public OXException setDisplayMessage(String displayMessage, Object... displayArgs) {
        return delegate.setDisplayMessage(displayMessage, displayArgs);
    }

    @Override
    public String getPlainLogMessage() {
        return delegate.getPlainLogMessage();
    }

    @Override
    public Object[] getLogArgs() {
        return delegate.getLogArgs();
    }

    @Override
    public Object[] getDisplayArgs() {
        return delegate.getDisplayArgs();
    }

    @Override
    public void log(Logger log) {
        delegate.log(log);
    }

    @Override
    public String getLogMessage(LogLevel logLevel) {
        return delegate.getLogMessage(logLevel);
    }

    @Override
    public String getLogMessage(LogLevel logLevel, String defaultLog) {
        return delegate.getLogMessage(logLevel, defaultLog);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        delegate.printStackTrace(s);
    }

    @Override
    public String getLogMessage() {
        return delegate.getLogMessage();
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        delegate.printStackTrace(s);
    }

    @Override
    public String getSoleMessage() {
        return delegate.getSoleMessage();
    }

    @Override
    public boolean isLoggable() {
        return delegate.isLoggable();
    }

    @Override
    public Category getCategory() {
        return delegate.getCategory();
    }

    @Override
    public List<Category> getCategories() {
        return delegate.getCategories();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return delegate.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        delegate.setStackTrace(stackTrace);
    }

    @Override
    public OXException setCategory(Category category) {
        return delegate.setCategory(category);
    }

    @Override
    public void removeCategory(Category category) {
        delegate.removeCategory(category);
    }

    @Override
    public String getExceptionId() {
        return delegate.getExceptionId();
    }

    @Override
    public void setExceptionId(String exceptionId) {
        delegate.setExceptionId(exceptionId);
    }

    @Override
    public String getPrefix() {
        return delegate.getPrefix();
    }

    @Override
    public boolean isPrefix(String expected) {
        return delegate.isPrefix(expected);
    }

    @Override
    public OXException setPrefix(String prefix) {
        return delegate.setPrefix(prefix);
    }

    @Override
    public String getDisplayMessage(Locale locale) {
        return delegate.getDisplayMessage(locale);
    }

    @Override
    public String getMessage() {
        return delegate.getMessage();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void addTruncatedId(int truncatedId) {
        delegate.addTruncatedId(truncatedId);
    }

    @Override
    public void addProblematic(ProblematicAttribute problematic) {
        delegate.addProblematic(problematic);
    }

    @Override
    public boolean containsProperty(String name) {
        return delegate.containsProperty(name);
    }

    @Override
    public String getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public String setProperty(String name, String value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public OXException setSessionProperties(Session session) {
        return delegate.setSessionProperties(session);
    }

    @Override
    public String removeProperty(String name) {
        return delegate.removeProperty(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

}
