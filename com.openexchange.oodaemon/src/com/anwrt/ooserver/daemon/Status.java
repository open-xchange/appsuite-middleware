
package com.anwrt.ooserver.daemon;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Vector;
import com.sun.star.uno.Type;
import com.sun.star.beans.NamedValue;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.container.XNameAccess;

/**
 * Kind of interface structure to exchange status informations between the main daemon process and an admin daemon process. <br>
 * creation : 30 août 07
 *
 * @author <a href="mailto:oodaemon@extraserv.net">Jounayd Id Salah</a>
 */
public class Status extends ComponentBase implements XNameAccess {

    private final HashMap _map;

    private int _available;

    private final Vector _workers;

    /**
     * Creates a structure that represents a fixed office process list
     *
     * @param processList the fixed process list
     */
    public Status(final OfficeProcess[] processList) {
        _map = new HashMap();
        _map.put("poolsize", new Integer(processList.length));
        _available = 0;
        _workers = new Vector();

        for (int j = 0; j < processList.length; j++) {
            final OfficeProcess p = processList[j];
            NamedValue v = null;
            if (p.getTimestamp() == null) {
                v = new NamedValue("usage-time", new Long(0));
                _available++;
            } else {
                v = new NamedValue("usage-time", new Long(System.currentTimeMillis() - p.getTimestamp().longValue()));
            }
            final NamedValue[] t =
                {
                    new NamedValue("usage", p.getUsage()), v, new NamedValue("user-dir", p.getUserId()),
                    new NamedValue("index", p.getIndex()), };
            _workers.add(t);
        }
        _map.put("workers", _workers.toArray());
        _map.put("available", new Integer(_available));
    }

    @Override
    public Object getByName(final String name) throws NoSuchElementException {
        if (_map.containsKey(name)) {
            return _map.get(name);
        }
        throw new NoSuchElementException("Unknown element " + name);
    }

    @Override
    public String[] getElementNames() {
        return (String[]) _map.keySet().toArray();
    }

    @Override
    public boolean hasByName(final String name) {
        return _map.containsKey(name);
    }

    @Override
    public boolean hasElements() {
        return true;
    }

    @Override
    public Type getElementType() {
        return new Type();
    }
}
