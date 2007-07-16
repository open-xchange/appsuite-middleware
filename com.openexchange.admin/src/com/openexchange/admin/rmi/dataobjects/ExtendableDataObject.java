package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;

import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;
import com.openexchange.admin.rmi.extensions.OXUserExtensionInterface;

public abstract class ExtendableDataObject implements ExtendableDataObjectInterface, Serializable {

    protected ArrayList<OXCommonExtensionInterface> extensions = null;

    protected final boolean extensionsset = false;
    
    protected void initExtendeable() {
        this.extensions = new ArrayList<OXCommonExtensionInterface>();
    }
    /**
     * This field is used to show if all extension have run fine and inserted their
     * data correctly
     */
    protected boolean extensionsok = true;

    public void addExtension(final OXCommonExtensionInterface extension) {
        this.extensions.add(extension);
    }

    public ArrayList<OXCommonExtensionInterface> getAllExtensions() {
        return this.extensions;
    }

    public boolean removeExtension(final OXCommonExtensionInterface o) {
        return extensions.remove(o);
    }

    public OXCommonExtensionInterface removeOneExtensionByIndex(final int index) {
        return extensions.remove(index);
    }
    
    /**
     * This method is used to get the extensions through the name of the
     * extension. An Array with all extensions where the name fits will be returned, 
     * or an empty array if no fitting extension was found.
     * 
     * @param extname a String for the extension
     * @return the ArrayList of {@link OXUserExtensionInterface} with extname
     */
    public ArrayList<OXCommonExtensionInterface> getExtensionsbyName(final String extname) {
        final ArrayList<OXCommonExtensionInterface> retval = new ArrayList<OXCommonExtensionInterface>();
        for (final OXCommonExtensionInterface ext : this.extensions) {
            if (ext.getExtensionName().equals(extname)) {
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
    public OXCommonExtensionInterface getOnlyFirstExtensionbyName(final String extname) {
        final ArrayList<OXCommonExtensionInterface> list = getExtensionsbyName(extname);
        if (!list.isEmpty() && list.size() == 1) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
