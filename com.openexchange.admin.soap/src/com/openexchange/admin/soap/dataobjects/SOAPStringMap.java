package com.openexchange.admin.soap.dataobjects;

import java.util.HashMap;
import java.util.Map;

public class SOAPStringMap {

    private Entry[] entries;

    public static SOAPStringMap convertFromMap(final Map<String, String> map) {
        final SOAPStringMap retval = new SOAPStringMap();
        if (null != map) {
            final Entry[] entries = new Entry[map.size()];
            int i = 0;
            for (final Map.Entry<String, String> entry : map.entrySet()) {
                entries[i] = new Entry(entry.getKey(), entry.getValue());
                i++;
            }
            retval.setEntries(entries);
            return retval;
        } else {
            return null;
        }
    }

    public static Map<String, String> convertToMap(final SOAPStringMap map) {
        final Map<String, String> retval = new HashMap<String, String>();
        if (null != map && null != map.getEntries()) {
            for (final Entry entry : map.getEntries()) {
                retval.put(entry.getKey(), entry.getValue());
            }
        }
        return retval;
    }
    
    public final Entry[] getEntries() {
        return entries;
    }
    
    public final void setEntries(Entry[] entries) {
        this.entries = entries;
    }
    
}
