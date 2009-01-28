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

package com.openexchange.search;

import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link SearchException} - Indicates an error during search.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchException extends AbstractOXException {

    private static final long serialVersionUID = 1777110556069218276L;

    public static final transient Component SEARCH_COMPONENT = new Component() {

        public String getAbbreviation() {
            return STR_COMPONENT;
        }
    };

    private static final String STR_COMPONENT = "SEARCH";

    /**
     * Initializes a new exception using the information provided by the cause.
     * 
     * @param cause the cause of the exception.
     */
    public SearchException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Constructor for the {@link SearchExceptionFactory}. If you want to instantiate a {@link SearchException} use
     * {@link SearchExceptionFactory#create(Object...)} or {@link SearchExceptionFactory#create(Throwable, Object...)} methods.
     * 
     * @param message Parameters for filling the exception with all necessary data.
     * @param cause the initial cause of the exception.
     * @param messageArgs arguments for the exception message.
     */
    SearchException(final ErrorMessage message, final Throwable cause, final Object... messageArgs) {
        super(message, cause);
        setMessageArgs(messageArgs);
    }
}
