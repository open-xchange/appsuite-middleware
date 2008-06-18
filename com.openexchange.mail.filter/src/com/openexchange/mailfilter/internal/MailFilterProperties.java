package com.openexchange.mailfilter.internal;

public class MailFilterProperties {
    
    public enum LoginTypes {
        GLOBAL("global"),
        USER("user");
        
        public final String name;
        
        private LoginTypes(final String name) {
            this.name = name;
        }
    }
    
    public enum Values {
        SIEVE_CREDSRC("SIEVE_CREDSRC", CredSrc.SESSION.name),
        SIEVE_LOGIN_TYPE("SIEVE_LOGIN_TYPE", LoginTypes.GLOBAL.name),
        SIEVE_SERVER("SIEVE_SERVER", "localhost"),
        SIEVE_PORT("SIEVE_PORT", "2000");
        
        public final String property;
        
        public final String def;
        
        private Values(final String property, final String def) {
            this.property = property;
            this.def = def;
        }
        
    }

    public enum CredSrc {
        SESSION("session"),
        IMAP_LOGIN("imapLogin");
        
        public final String name;
        
        private CredSrc(final String name) {
            this.name = name;
        }
    }

}
