
package com.openexchange.fitnesse.wrappers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link FixtureDataWrapper} - wraps all data needed to set up a fixture and returns it in all the shitty nested lists and maps that are
 * required for FitNesse and the yaml suite.
 * 
 * Note: This implementation contains the order of the given lists.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FixtureDataWrapper {

    public static final String FIXTURE_NAME = "fixturename";

    public static final String EXPECTED_ERROR = "expectedError";

    private List<String> header;

    private List<String> values;

    private int length;
    
    private String fixtureName = null;
    
    private String expectedError = null;

    public FixtureDataWrapper(List<List<String>> table) {
        super();
        header = table.get(0);
        values = table.get(1);
        length = header.size();
        for (int i = 0; i < header.size(); i++) {
            if(FIXTURE_NAME.equals( header.get(i) ) ){
                fixtureName = values.get(i);
            }
            if (EXPECTED_ERROR.equals(header.get(i))){
                expectedError = values.get(i);
            }
        }
    }

    public FixtureDataWrapper(Map<String, String> map) {
        this(
            Arrays.asList(
                ((List<String>)new LinkedList<String>(map.keySet())), 
                ((List<String>) new LinkedList<String>(map.values()))
            )
        );
    }

    public Map<String, String> asMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < header.size(); i++) {
            map.put(header.get(i), values.get(i));
        }
        return map;
    }

    public List<List<String>> asFitnesseList() {
        return Arrays.asList(header, values);
    }

    public Map<String, Map<String, String>> asFixtureMap(String key) {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put(key, asMap());
        return map;
    }

    public String getFixtureName() {
        return fixtureName;
    }

    /**
     * @return the expected error id (like TSK-0001) or null if not found
     */
    public String getExpectedError() {
        return expectedError;
    }

    /**
     * @return the amount of fields in the data given, including fixture name and expected error if applicable
     */
    public int size() {
        return length;
    }

    /**
     * 
     * @param pos - position in the list this wrapper holds
     * @return the value at that position
     */
    public String get(int pos) {
        return values.get(pos);
    }
    
    public List<String> getValues(){
        return values;
    }

    public List<String> getHeader() {
        return header;
    }

}
