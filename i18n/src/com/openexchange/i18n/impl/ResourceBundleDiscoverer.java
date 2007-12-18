package com.openexchange.i18n.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class ResourceBundleDiscoverer {

    private static final Log LOG = LogFactory.getLog(ResourceBundleDiscoverer.class);
    private File dir;

    public ResourceBundleDiscoverer(File dir) throws FileNotFoundException {
        if (dir.isFile()){
            throw new FileNotFoundException("Unable to load language files."+ dir +" is not a directory");
        }
        this.dir = dir;
    }

    public List<ResourceBundle> getResourceBundles() throws java.util.MissingResourceException {
        String[] files = getFilesFromLanguageFolder();
        if(files.length == 0) {
            return Collections.EMPTY_LIST;
        }
        List<ResourceBundle> list = new ArrayList<ResourceBundle>(files.length);
		for (String file : files){
			Locale l = null;

			try{
				if (file.indexOf("_") != -1){
					l = new Locale(file.substring(0, file.indexOf("_")), file.substring(file.indexOf("_")+1,file.indexOf(".")));
				}

				URLClassLoader ul = new URLClassLoader(new URL[]{new URL("file:"+ dir +File.separator+file)});
				ResourceBundle rc = ResourceBundle.getBundle("com.openexchange.groupware.i18n.ServerMessages", l , ul);

                list.add(rc);

            } catch (java.util.MissingResourceException mr){
				LOG.error("Unable to init Language Bundle! This file seems to be broken: "+file);
				throw mr;
			} catch (MalformedURLException e) {
                LOG.error("Cannot load file: "+file);
            }
        }
        return list;
    }

    public String[] getFilesFromLanguageFolder(){
		String[] files = dir.list(new FilenameFilter() {
		    public boolean accept(File d, String f) {
		       return f.endsWith(".jar");
		    }
		});
		return files;
	}
}
