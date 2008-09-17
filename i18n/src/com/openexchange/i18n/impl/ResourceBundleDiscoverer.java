package com.openexchange.i18n.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ResourceBundleDiscoverer {

    private static final Log LOG = LogFactory.getLog(ResourceBundleDiscoverer.class);
    private final File dir;

    public ResourceBundleDiscoverer(final File dir) throws FileNotFoundException {
        if (dir.exists()){
            throw new FileNotFoundException("Unable to load language files. Directory does not exist: "+ dir);
        } else if (dir.isFile())  {
        	throw new FileNotFoundException("Unable to load language files."+ dir +" is not a directory");
        }
        this.dir = dir;
    }

    public List<ResourceBundle> getResourceBundles() throws java.util.MissingResourceException {
        final String[] files = getFilesFromLanguageFolder();
        if(files.length == 0) {
        	Collections.emptyList();
        }
        final List<ResourceBundle> list = new ArrayList<ResourceBundle>(files.length);
		for (final String file : files){
			Locale l = null;

			try{
				if (file.indexOf("_") != -1){
					l = new Locale(file.substring(0, file.indexOf("_")), file.substring(file.indexOf("_")+1,file.indexOf(".")));
				}

				final URLClassLoader ul = new URLClassLoader(new URL[]{new URL("file:"+ dir +File.separator+file)});
				final ResourceBundle rc = ResourceBundle.getBundle("com.openexchange.groupware.i18n.ServerMessages", l , ul);

                list.add(rc);

            } catch (final java.util.MissingResourceException mr){
				LOG.error("Unable to init Language Bundle! This file seems to be broken: "+file);
				throw mr;
			} catch (final MalformedURLException e) {
                LOG.error("Cannot load file: "+file);
            }
        }
        return list;
    }

    public String[] getFilesFromLanguageFolder(){
		final String[] files = dir.list(new FilenameFilter() {
		    public boolean accept(final File d, final String f) {
		       return f.endsWith(".jar");
		    }
		});
		return files;
	}
}
