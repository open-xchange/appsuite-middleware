
package com.openexchange.spamsettings.generic.service;

import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.datatypes.genericonf.FormElement;


public class ExtendedFormElement extends FormElement {

    private final Map<Integer, String> newmap = new  LinkedHashMap<Integer, String>();

    public void addOptionsNew(final Integer key, final String value) {
        this.newmap.put(key, value);
    }

    public Map<Integer, String> getOptionsNew() {
        return this.newmap;
    }
}
