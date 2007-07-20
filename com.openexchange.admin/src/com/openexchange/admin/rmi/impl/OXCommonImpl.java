package com.openexchange.admin.rmi.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * General abstraction class used by all impl classes
 * 
 * @author d7
 *
 */
public abstract class OXCommonImpl {
    private final static Log log = LogFactory.getLog(OXCommonImpl.class);
    
    protected final void contextcheck(final Context ctx) throws InvalidCredentialsException {
        if (null == ctx || null == ctx.getIdAsInt()) {
            final InvalidCredentialsException e = new InvalidCredentialsException("Client sent invalid context data object");
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * @param objects
     * @throws InvalidDataException
     */
    protected final static void doNullCheck(final Object... objects) throws InvalidDataException {
        for (final Object object : objects) {
            if (object == null) {
                throw new InvalidDataException();
            }
        }
    }

}
