package com.openexchange.contacts.ldap.contacts;

import com.openexchange.contacts.ldap.exceptions.LdapException;


public interface UidInterface {
    
    public Integer getUid(final String uid) throws LdapException;
}
