/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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


