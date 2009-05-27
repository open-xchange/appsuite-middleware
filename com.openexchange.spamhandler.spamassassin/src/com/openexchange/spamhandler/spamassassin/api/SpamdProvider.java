package com.openexchange.spamhandler.spamassassin.api;


/**
 * This interface is used to write a provider which provides all information needed to
 * access a spamd installation. This are hostname, port and the username
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface SpamdProvider {

    /**
     * Get the hostname of the system where spamd is running
     * 
     * @return the hostname of the spamd system
     */
    public String getHostname();
    
    /**
     * Get the port of the spamd daemon
     * 
     * @return -1 if no special port is required, the default one 783 will be used then
     */
    public int getPort();
    
    /**
     * The username for spamd.
     * 
     * @return
     */
    public String getUsername();
}
