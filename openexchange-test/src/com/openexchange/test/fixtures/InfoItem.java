
package com.openexchange.test.fixtures;

import java.util.Date;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;

public class InfoItem extends DocumentMetadataImpl {

    private static final long serialVersionUID = 1L;
    private int numberOfLinks;
    private Document[] versions;

    public InfoItem() {
        super();
    }

    public int getLabel() {
        return super.getColorLabel();
    }

    public void setLabel(final int label) {
        super.setColorLabel(label);
    }

    /**
     * @param numberOfLinks the numberOfLinks to set
     */
    public void setNumberOfLinks(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
    }

    /**
     * @return the numberOfLinks
     */
    public int getNumberOfLinks() {
        return numberOfLinks;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        super.setLockedUntil(locked ? new Date(3600000) : new Date(0));
    }

    /**
     * @return the locked
     */
    public boolean isLocked() {
        return (new Date()).after(super.getLockedUntil());
    }

    /**
     * @param versions the versions to set
     */
    public void setVersions(final Document[] versions) {
        this.versions = versions;
    }

    /**
     * @return the versions
     */
    public Document[] getVersions() {
        return versions;
    }

    public boolean containsVersions() {
        return null != this.versions && 0 < this.versions.length;
    }

    public Document getCurrentVersion() {
        if (this.containsVersions()) {
            if (super.getVersion() < this.versions.length) {
                return this.versions[super.getVersion()];
            } else {
                return this.versions[0];
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the remote path associated with the current document version of this
     * infoitem, pointing to a file on the remote filesystem.
     * 
     * @return the path, or null, if it's undefined or no versions exist
     */
    public String getCurrentRemotePath() {
        final Document currentVersion = getCurrentVersion();
        return null == currentVersion ? null : currentVersion.getRemotePath();
    }

    /*
     * overrides
     */

    @Override
    public String getFileName() {
        final Document currentVersion = getCurrentVersion();
        return null == currentVersion ? null : currentVersion.getFile().getName();
    }

    @Override
    public long getFileSize() {
        final Document currentVersion = getCurrentVersion();
        return null == currentVersion ? 0 : currentVersion.getSize();
    }

    @Override
    public int getVersion() {
        return containsVersions() ? 1 + super.getVersion() : 0;
    }

    @Override
    public void setVersion(int version) {
        super.setVersion(version - 1);
    }

    @Override
    public String getFileMIMEType() {
        final Document currentVersion = getCurrentVersion();
        return null == currentVersion ? null : currentVersion.getMimeType();
    }
}
