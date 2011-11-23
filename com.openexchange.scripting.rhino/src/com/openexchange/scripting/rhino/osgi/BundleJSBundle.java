package com.openexchange.scripting.rhino.osgi;

import java.net.URL;

import org.osgi.framework.Bundle;

import com.openexchange.scripting.rhino.require.JSBundle;

public class BundleJSBundle implements JSBundle {

	private final Bundle bundle;

	public BundleJSBundle(Bundle bundle) {
		super();
		this.bundle = bundle;
	}

	@Override
	public String getSymbolicName() {
		return bundle.getSymbolicName();
	}

	@Override
	public URL getEntry(String path) {
		return bundle.getEntry(path);
	}

}
