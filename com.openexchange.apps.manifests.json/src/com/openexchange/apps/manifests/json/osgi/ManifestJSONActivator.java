package com.openexchange.apps.manifests.json.osgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.json.JSONArray;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;

public class ManifestJSONActivator extends AJAXModuleActivator {
	
	private static final Log LOG = LogFactory.getLog(ManifestJSONActivator.class);
	
	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{ConfigurationService.class, CapabilityService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		
		registerModule(new ManifestActionFactory(this, readManifests()), "apps/manifests");
		
	}

	private JSONArray readManifests() {
		File file = new File(getService(ConfigurationService.class).getProperty("com.openexchange.apps.manifestPath"));
		
		JSONArray array = new JSONArray();
		if (file.exists()) {
			for(File f: file.listFiles()) {
				read(f, array);
			}
		}
		return array;
	}

	private void read(File f, JSONArray array) {
		BufferedReader r = null;
		StringBuilder b = new StringBuilder();
		try {
			r = new BufferedReader(new FileReader(f));
			int c = -1;
			while((c = r.read()) != -1) {
				b.append((char) c);
			}
			JSONArray fileContent = new JSONArray(b.toString());
			for(int i = 0, size = fileContent.length(); i < size; i++) {
				array.put(fileContent.get(i));
			}
		} catch (Throwable e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e1) {
				}
			}
		}
	}


}
