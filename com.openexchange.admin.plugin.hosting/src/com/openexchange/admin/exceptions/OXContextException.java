
package com.openexchange.admin.exceptions;


/**
 * OXContext exception class
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> , 
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> , 
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXContextException extends Exception {
    
    
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
}
