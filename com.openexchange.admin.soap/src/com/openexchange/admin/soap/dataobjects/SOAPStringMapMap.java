package com.openexchange.admin.soap.dataobjects;

import java.util.HashMap;
import java.util.Map;

public class SOAPStringMapMap {

    private SOAPMapEntry[] entries;

    public static SOAPStringMapMap convertFromMapMap(final Map<String,Map<String, String>> map) {
        final SOAPStringMapMap retval = new SOAPStringMapMap();
        if (null != map) {
            final SOAPMapEntry[] entries = new SOAPMapEntry[map.size()];
            int i = 0;
            for (final Map.Entry<String, Map<String,String>> entry : map.entrySet()) {
                entries[i] = new SOAPMapEntry(entry.getKey(), SOAPStringMap.convertFromMap(entry.getValue()));
                i++;
            }
            retval.setEntries(entries);
            return retval;
        } else {
            return null;
        }
    }

    public static Map<String, Map<String, String>> convertToMapMap(final SOAPStringMapMap map) {
        final Map<String, Map<String, String>> retval = new HashMap<String, Map<String, String>>();
        if (null != map && null != map.getEntries()) {
            for (final SOAPMapEntry entry : map.getEntries()) {
                retval.put(entry.getKey(), SOAPStringMap.convertToMap(entry.getValue()));
            }
        }
        return retval;
    }
    
    public final SOAPMapEntry[] getEntries() {
        return entries;
    }
    
    public final void setEntries(SOAPMapEntry[] entries) {
        this.entries = entries;
    }
    
}
