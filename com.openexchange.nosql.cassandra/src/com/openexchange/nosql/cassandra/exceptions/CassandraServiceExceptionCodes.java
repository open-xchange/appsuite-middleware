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

package com.openexchange.nosql.cassandra.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link CassandraServiceExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CassandraServiceExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * None of the Cassandra contact points '%1%s' is reachable
     */
    CONTACT_POINTS_NOT_REACHABLE("None of the Cassandra contact points '%1%s' is reachable", Category.CATEGORY_CONNECTIVITY, 1),
    /**
     * An authentication error while contacting the initial contact points '%1%s'
     */
    AUTHENTICATION_ERROR("An authentication error while contacting the initial contact points '%1%s'", Category.CATEGORY_ERROR, 2),
    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", Category.CATEGORY_ERROR, 3);
    ;

    private final int number;
    private final Category category;
    private static final String PREFIX = "CASSANDRA-SERVICE";
    private final String message;
    private final String displayMessage;

    /**
     * Initialises a new {@link CassandraServiceExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param number
     */
    private CassandraServiceExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.displayMessage = message;
        this.category = category;
        this.number = number;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#equals(com.openexchange.exception.OXException)
     */
    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getNumber()
     */
    @Override
    public int getNumber() {
        return number;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getCategory()
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getPrefix()
     */
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getMessage()
     */
    @Override
    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.DisplayableOXExceptionCode#getDisplayMessage()
     */
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
