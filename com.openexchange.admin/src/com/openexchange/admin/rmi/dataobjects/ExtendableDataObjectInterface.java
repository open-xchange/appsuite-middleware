package com.openexchange.admin.rmi.dataobjects;

import java.util.ArrayList;

import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;

public interface ExtendableDataObjectInterface {
    public boolean isExtensionsok();
    
    public void setExtensionsok(boolean extensionsok);
    
    public ArrayList<OXCommonExtensionInterface> getAllExtensions();
    
//    public OXCommonExtensionInterface getFirstExtensionbyName(final String extname);
    
    public void addExtension(final OXCommonExtensionInterface extension);
}
