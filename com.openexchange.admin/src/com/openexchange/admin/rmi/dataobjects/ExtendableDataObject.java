package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;

public abstract class ExtendableDataObject extends EnforceableDataObject implements Serializable, Cloneable {

    private Hashtable<String, OXCommonExtension> extensions = null;

    private final boolean extensionsset = false;
    
    /**
     * This field is used to show if all extension have run fine and inserted their
     * data correctly
     */
    private boolean extensionsok = true;
    
    public void addExtension(final OXCommonExtension extension) throws DuplicateExtensionException {
        final String extensionName = extension.getClass().getName();
        if (this.extensions.containsKey(extensionName)) {
            throw new DuplicateExtensionException(extensionName);
        }
        this.extensions.put(extensionName, extension);
    }

    /**
     * @return
     * 
     * @deprecated Will be removed with next release. Please use getAllExtensionsAsHash instead
     */
    public ArrayList<OXCommonExtension> getAllExtensions() {
        return new ArrayList<OXCommonExtension>(this.extensions.values());
    }

    public Hashtable<String, OXCommonExtension> getAllExtensionsAsHash() {
        return this.extensions;
    }
    
    /**
     * This method is used to get the extensions through the name of the
     * extension. An Array with all extensions where the name fits will be returned, 
     * or an empty array if no fitting extension was found.
     * 
     * @param extname a String for the extension
     * @return the ArrayList of {@link OXCommonExtension} with extname
     * @deprecated Will be removed with next release
     */
    public ArrayList<OXCommonExtension> getExtensionsbyName(final String extname) {
        final ArrayList<OXCommonExtension> retval = new ArrayList<OXCommonExtension>();
        for (final OXCommonExtension ext : this.extensions.values()) {
            if (extname.equals(ext.getClass().getName())) {
                retval.add(ext);
            }
        }
        return retval;
    }
    
    /**
     * A convenience method for getting the first extension in a list of equal extension names. The
     * use of this method is not recommended because you won't get notifications how many extensions
     * of the same name exist.
     * 
     * @param extname
     * @return
     */
    public OXCommonExtension getFirstExtensionByName(final String extname) {
        return this.extensions.get(extname);
    }
    
    public boolean isExtensionsok() {
        return extensionsok;
    }

    public boolean isExtensionsset() {
        return extensionsset;
    }

    public boolean removeExtension(final OXCommonExtension o) {
        if (null == extensions.remove(o.getClass().getName())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param index
     * @return
     * @deprecated
     */
    public OXCommonExtension removeOneExtensionByIndex(final int index) {
        return extensions.remove(index);
    }
    
    public final void setExtensionsok(boolean extensionsok) {
        this.extensionsok = extensionsok;
    }
    
    protected void initExtendable() {
        this.extensions = new Hashtable<String, OXCommonExtension>(3);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        final ExtendableDataObject object = (ExtendableDataObject) super.clone();
        if( this.extensions != null ) {
            object.extensions = new Hashtable<String, OXCommonExtension>(this.extensions);
        }
        return object;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder(super.toString());
        for (final OXCommonExtension usrext : extensions.values()) {
            ret.append("  ");
            ret.append("Extension ");
            ret.append(usrext.getClass().getName());
            ret.append(" contains: \n");
            ret.append("  ");
            ret.append(usrext.toString());
            ret.append("\n");
        }

        return ret.toString();
    }

    
}
