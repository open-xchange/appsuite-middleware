/**
 * 
 */
package com.openexchange.admin.contextrestore.dataobjects;

public class VersionInformation {
    private final int version;
    
    private final int locked;
    
    private final int gw_compatible;
    
    private final int admin_compatible;
    
    private final String server;
    
    /**
     * @param admin_compatible
     * @param gw_compatible
     * @param locked
     * @param server
     * @param version
     */
    public VersionInformation(final int admin_compatible, final int gw_compatible, final int locked, final String server, final int version) {
        this.admin_compatible = admin_compatible;
        this.gw_compatible = gw_compatible;
        this.locked = locked;
        this.server = server;
        this.version = version;
    }

    public final int getVersion() {
        return version;
    }

    public final int getLocked() {
        return locked;
    }

    public final int getGw_compatible() {
        return gw_compatible;
    }

    public final int getAdmin_compatible() {
        return admin_compatible;
    }

    public final String getServer() {
        return server;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + admin_compatible;
        result = prime * result + gw_compatible;
        result = prime * result + locked;
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VersionInformation other = (VersionInformation) obj;
        if (admin_compatible != other.admin_compatible)
            return false;
        if (gw_compatible != other.gw_compatible)
            return false;
        if (locked != other.locked)
            return false;
        if (server == null) {
            if (other.server != null)
                return false;
        } else if (!server.equals(other.server))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

}
