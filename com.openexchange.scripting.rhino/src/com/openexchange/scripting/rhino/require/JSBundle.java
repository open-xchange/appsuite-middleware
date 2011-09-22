package com.openexchange.scripting.rhino.require;

import java.net.URL;

public interface JSBundle {

	String getSymbolicName();

	URL getEntry(String string);

}
