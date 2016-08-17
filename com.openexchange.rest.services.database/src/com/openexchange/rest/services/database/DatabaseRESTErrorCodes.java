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

package com.openexchange.rest.services.database;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;


/**
 * {@link DatabaseRESTErrorCodes}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum DatabaseRESTErrorCodes implements OXExceptionCode {
    // There is a problem with the database, please try again later.
    SQL_ERROR(1, Category.CATEGORY_SERVICE_DOWN, DatabaseRESTErrorMessages.SQL_ERROR), 
    // You have exceeded the query limit for one batch. Maximum %1$d, you sent %2$d queries.
    QUERY_LIMIT_EXCEEDED(2, Category.CATEGORY_CAPACITY, DatabaseRESTErrorMessages.QUOTA_LIMIT_EXCEEDED),
    // Version should be known but is unknown for module %1$s
    VERSION_MUST_BE_KNOWN(3, Category.CATEGORY_ERROR, DatabaseRESTErrorMessages.VERSION_MUST_BE_KNOWN);
    
    ;
    
    public static final String PREFIX = "REST-DB";
    
    private int number;
    private Category category;
    private String message;
    
    DatabaseRESTErrorCodes(int number, Category category, String message) {
        this.number = number;
        this.category = category;
        this.message = message;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(OXException e) {
        return e.getPrefix().equals(PREFIX) && e.getCode() == number;
    }

    public OXException create(Object...args) {
        return new OXException(number, message, args);
    }

}
