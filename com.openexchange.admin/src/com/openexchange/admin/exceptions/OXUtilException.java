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

package com.openexchange.admin.exceptions;


/**
 * OXUtil exception class
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> ,
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> ,
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXUtilException extends Exception {


    private static final long serialVersionUID = 5040236157527189890L;



    /**
     * If requested reason id does not exists
     */
    public static final String NO_SUCH_REASON  = "Reason ID does not exist";

    /**
     * If requested reason already exists
     */
    public static final String REASON_EXISTS  = "Reason already exists";



    /**
     * If database already exists
     */
    public static final String DATABASE_EXISTS = "Database already exists";

    /**
     * If database does not exist
     */
    public static final String NO_SUCH_DATABASE = "Database does not exist";



    /**
     * If server already exists
     */
    public static final String SERVER_EXISTS = "Server already exists";

    /**
     * If server does not exists
     */
    public static final String NO_SUCH_SERVER = "Server does not exist";

    /**
     * If store already exists
     */
    public static final String STORE_EXISTS = "Store already exists";

    /**
     * If requested store id does not exists
     */
    public static final String NO_SUCH_STORE  = "Store ID does not exist";

    /**
     * If store is still in use
     */
    public static final String STORE_IN_USE  = "Store is still in use";

    /**
     * If pool is still in use
     */
    public static final String POOL_IN_USE  = "Pool is still in use";

    /**
     * If pool is still in use
     */
    public static final String SERVER_IN_USE  = "Server is still in use";

    /**
     * OX exceptions for OXUtil
     *
     */
    public OXUtilException( String s ) {
        super( s );
    }

}


