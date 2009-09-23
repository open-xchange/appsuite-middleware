package com.openexchange.contacts.ldap.ldap;

import java.util.Set;
import com.openexchange.contacts.ldap.exceptions.LdapException;


public interface LdapInterface {

    
    public interface FillClosure {

        public void execute(LdapGetter ldapGetter) throws LdapException;

    }

    public void search(final String ownBaseDN, final String ownFilter, final boolean distributionslist, final Set<Integer> columns, final FillClosure closure) throws LdapException;

    public void close() throws LdapException;
}
