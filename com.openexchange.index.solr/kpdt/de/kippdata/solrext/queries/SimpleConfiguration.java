package de.kippdata.solrext.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;

/**
 * This class encapsulates a simple file-based dictionary for the field-mappings. The syntax for the
 * mapping is <br>
 * <code>symb_name = real_index_field</code><br>
 * where <code>real_index_field</code> is either of the form of the name of a field in the Solr schema
 * or of the form<br>
 * <code>name{suffix1,suffix2,...suffixn}</code><br>
 * The latter form is expanded into a series of fields of the form<br>
 * <code>name_suffix1</code> to <code>name_suffixn</code>.
 * 
 * @author Sven Maurmann
 */
public class SimpleConfiguration implements Configuration {
  private Map<String,String> rawMapping;
  private Map<String,List<String>> dictionary;
  private Map<String,String> translators;

  private static Log log = com.openexchange.log.Log.loggerFor(SimpleConfiguration.class);


  public SimpleConfiguration(String configPath) throws BuilderException {
    dictionary  = new HashMap<String,List<String>>();
    rawMapping  = new HashMap<String,String>();
    translators = new HashMap<String,String>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(new File(configPath)));
      int lineCount = 0;
      while (reader.ready()) {
        String line = reader.readLine();
        lineCount++;
        String[] parts = line.split("=");
        if (parts.length != 2) {
          log.warn("[SimpleConfiguration]: Invalid line " + lineCount + ": " + line);
          continue;
        }
        rawMapping.put(parts[0].trim(), parts[1].trim());

        if (parts[0].startsWith("translator.")) {
          log.info("[SimpleConfiguration]: Extracting translator ...");
          String handlerName = parts[0].substring(parts[0].indexOf(".") + 1);
          log.debug("[SimpleConfiguration]: Handler is " + handlerName);
          log.debug("[SimpleConfiguration]: Translator is " + parts[1].trim());
          translators.put(handlerName, parts[1].trim());
          continue;
        }
        if (parts[1].contains("{")) {
          log.debug("[SimpleConfiguration]: found a compound field");
          dictionary.put(parts[0].trim(), this.assembleFieldList(parts[1].trim()));
        }
        else {
          log.debug("[SimpleConfiguration]: found a simple field");
          List<String> val = new ArrayList<String>();
          val.add(parts[1].trim());
          dictionary.put(parts[0].trim(), val);
        }
      }
    }
    catch (FileNotFoundException e) {
      log.error("[SimpleConfiguration]: Error during instantiation: " + e.getMessage());
      throw new BuilderException(e);
    }
    catch (IOException e) {
      log.error("[SimpleConfiguration]: Error during instantiation: " + e.getMessage());
      throw new BuilderException(e);
    }
  }

  public List<String> getIndexFields(String key) {
    return dictionary.get(key);
  }

  public Set<String> getKeys() {
    return dictionary.keySet();
  }

  public Set<String> getKeys(String handlerName) {
    Set<String> dictKeys = new HashSet<String>();
    for (String key : dictionary.keySet()) {
      if (key.startsWith(handlerName)) dictKeys.add(key);
    }
    return dictKeys;
  }

  public Map<String,String> getRawMapping() {
    return this.rawMapping;
  }

  public Map<String,String> getTranslatorMap() {
    return this.translators;
  }

  public boolean haveTranslatorForHandler(String handler) {
    return translators.containsKey(handler);
  }

  public String getTranslatorForHandler(String handler) {
    return translators.get(handler);
  }

  public Set<String> getHandlers() {
    return translators.keySet();
  }


// -------------------------- private methods below ----------------------------------- //

  private List<String> assembleFieldList(String input) {
    List<String> fieldList = new ArrayList<String>();

    int prefixIdx = input.indexOf("{");
    String prefix = input.substring(0, prefixIdx).trim();
    String suffixes = input.substring(prefixIdx+1,input.length()-1);
    String[] suffixArray = suffixes.split(",");

    for (int i=0; i<suffixArray.length; i++) {
      fieldList.add(prefix + "_" + suffixArray[i].trim());
    }

    return fieldList;
  }
}
