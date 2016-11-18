
package com.openexchange.test.fixtures;

import java.io.File;

/**
 * Documents are intended to be used as attachments for groupware objects.
 * This could either be a regular attachment to an appointment, a task or
 * a contact, or one specific version of an infoitem.
 *
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class Document {

    private String path;
    private File file;
    private String mimeType;
    private String comments;
    private SimpleCredentials createdBy;
    private InfoItem parent;
    private final File datapath;
    private Object seleniumDataPath;
    private Object seleniumSeparator;

    public Document(File datapath) {
        this(null, datapath);
    }

    public Document(final InfoItem parent, File datapath) {
        this.datapath = datapath;
        this.setParent(parent);
    }

    public boolean containsFile() {
        return null != this.getFile();
    }

    /**
     * Gets the filename of this document's file. This is just the last name in
     * the pathname's name sequence.
     * 
     * @return the filename, or null if the document doesn't contain a file
     */
    public String getName() {
        if (this.containsFile()) {
            return this.getFile().getName();
        } else {
            return null;
        }
    }

    /**
     * @return the file
     */
    public File getFile() {
        if (null == this.file) {
            final String localPath = this.getLocalPath();
            if (null != localPath) {
                this.file = new File(localPath).getAbsoluteFile();
            }
        }
        return file;
    }

    public boolean containsMimeType() {
        return null != this.mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the size
     */
    public long getSize() {
        final File file = this.getFile();
        return null == file ? 0 : file.length();
    }

    public boolean containsComments() {
        return null != this.comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(final String comments) {
        this.comments = comments;
    }

    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    public boolean containsCreatedBy() {
        return null != this.createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(final SimpleCredentials createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the createdBy
     */
    public SimpleCredentials getCreatedBy() {
        return createdBy;
    }

    public void setSeleniumConfiguration(String seleniumDataPath, String seleniumSeparator) {
        this.seleniumDataPath = seleniumDataPath;
        this.seleniumSeparator = seleniumSeparator;
    }

    public boolean containsParent() {
        return null != parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(final InfoItem parent) {
        this.parent = parent;
    }

    /**
     * @return the parent
     */
    public InfoItem getParent() {
        return parent;
    }

    public boolean isCurrent() {
        if (this.containsParent()) {
            return this.equals(this.parent.getCurrentVersion());
        } else {
            return false;
        }
    }

    public int getVersionNumber() {
        if (this.containsParent()) {
            final Document[] versions = this.parent.getVersions();
            for (int i = 0; i < versions.length; i++) {
                if (this.equals(versions[i])) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * Returns a path to this document's file on the local filesystem, using the current
     * relative path in the local testdata directory.
     *
     * @return the path to the local file, if a relative path is set, or null, otherwise
     */
    public String getLocalPath() {
        if (this.containsPath()) {
            return String.format("%s%s%s", datapath, File.separatorChar, this.getPath().replaceAll("[/\\\\]+", "\\" + File.separatorChar));
        } else {
            return null;
        }
    }

    /**
     * Returns a path to this document's file on the remote filesystem, using the current
     * relative path in the remote testdata directory.
     *
     * @return the path to the local file, if a relative path is set, or null, otherwise
     */
    public String getRemotePath() {
        if (seleniumDataPath == null) {
            throw new UnsupportedOperationException("This object is not configured for selenium operations.");
        }
        if (this.containsPath()) {
            return String.format("%s%s%s", seleniumDataPath, seleniumSeparator, this.getPath().replaceAll("[/\\\\]+", "\\" + seleniumSeparator));
        } else {
            return null;
        }
    }

    public boolean containsPath() {
        return null != this.path;
    }

    /**
     * @param relativePath the relativePath to set
     */
    public void setPath(final String relativePath) {
        this.path = relativePath;
    }

    /**
     * @return the relativePath
     */
    public String getPath() {
        return path;
    }

    public File getDatapath() {
        return datapath;
    }

    @Override
    public String toString() {
        return containsPath() ? getPath() : super.toString();
    }

}
