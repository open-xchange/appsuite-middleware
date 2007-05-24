/**
 * 
 */
package com.openexchange.admin.rmi.dataobjects;

import java.util.ArrayList;

/**
 * @author choeger
 *
 */
public class DataObjectResponse {
    
    private boolean objectOkay = false;
 
    private ArrayList<String> invalidFields = null;

    /**
     * @return the invalidFields
     */
    public final ArrayList<String> getInvalidFields() {
        return invalidFields;
    }

    /**
     * @param invalidFields the invalidFields to set
     */
    public final void setInvalidFields(ArrayList<String> invalidFields) {
        this.invalidFields = invalidFields;
    }

    /**
     * @return the objectOkay
     */
    public final boolean isObjectOkay() {
        return objectOkay;
    }

    /**
     * @param objectOkay the objectOkay to set
     */
    public final void setObjectOkay(boolean objectOkay) {
        this.objectOkay = objectOkay;
    }
    
}
