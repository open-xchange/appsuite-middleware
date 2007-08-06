package com.openexchange.admin.rmi.dataobjects;

/**
 * This interface forces 4 Methods for a data object so that we can easily combine some operations
 * 
 * @author d7
 *
 */
public interface NameAndIdObject {

    abstract void setId(final Integer id);
    
    abstract Integer getId();
    
    abstract void setName(final String name);
    
    abstract String getName();
}
