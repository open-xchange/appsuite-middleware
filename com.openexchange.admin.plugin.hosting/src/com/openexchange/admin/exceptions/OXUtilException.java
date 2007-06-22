
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


