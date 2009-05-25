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

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ErrorMessage implements Comparable<ErrorMessage>, OXErrorMessage {

    private int errorCode;

    private String message;

    private String help;

    private Component component;

    private String applicationId;

    private AbstractOXException.Category category;

    private OXErrorMessage delegate;

    public ErrorMessage(final int errorCode, final Component component, final String applicationId, final AbstractOXException.Category category, final String message, final String help) {
        this.errorCode = errorCode;
        this.component = component;
        this.applicationId = applicationId;
        this.category = category;
        this.message = message;
        this.help = help;
    }

    public ErrorMessage(final OXErrorMessage error, final Component component, final String applicationId) {
        delegate = error;
        this.component = component;
        this.applicationId = applicationId;
    }

    public int getDetailNumber() {
        if (delegate != null) {
            return delegate.getDetailNumber();
        }
        return errorCode;
    }

    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        if (delegate != null) {
            return delegate.getMessage();
        }
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getHelp() {
        if (delegate != null) {
            return delegate.getHelp();
        }
        return help;
    }

    public void setHelp(final String help) {
        this.help = help;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(final Component component) {
        this.component = component;
    }

    public AbstractOXException.Category getCategory() {
        if (delegate != null) {
            return delegate.getCategory();
        }
        return category;
    }

    public void setCategory(final AbstractOXException.Category category) {
        this.category = category;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    public int compareTo(final ErrorMessage other) {
        return getDetailNumber() - other.getDetailNumber();
    }

    public OXErrorMessage getOXErrorMessage() {
        if (delegate != null) {
            return delegate;
        }
        return this;
    }
}
