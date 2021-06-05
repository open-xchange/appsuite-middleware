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

package com.openexchange.admin.rmi.exceptions;


/**
 * OXContext exception class
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> ,
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> ,
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXContextException extends AbstractAdminRmiException {


    private static final long serialVersionUID = 7673005697667470880L;



    /**
     * If context already exists
     */
    public static final String CONTEXT_EXISTS   = "Context already exists";



    /**
     * If requested context not exists
     */
    public static final String NO_SUCH_CONTEXT  = "Context does not exist";



    /**
     * If context is already disabled
     */
    public static final String CONTEXT_DISABLED    = "Context already disabled";


    /**
     *  server2db_pool does not contain requested server name
     */
    public static final String NO_SUCH_SERVER_IN_DBPOOL = "server is not linked to dbpool";


    /**
     * @param cause
     */
    public OXContextException(Throwable cause) {
        super(cause);
    }

    /**
     * OX exceptions for context handling with various messages
     *
     * @see #CONTEXT_EXISTS
     * @see #NO_SUCH_CONTEXT
     */
    public OXContextException( String s ) {
        super( s );
    }

    public OXContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
