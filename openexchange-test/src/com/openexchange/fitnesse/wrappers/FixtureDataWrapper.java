package com.openexchange.fitnesse.wrappers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * {@link FixtureDataWrapper} - wraps all data needed to set up a fixture and returns it
 * in all the shitty nested lists and maps that are required for FitNesse and the yaml
 * suite.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class FixtureDataWrapper {
    public static final String FIXTURE_NAME = "fixturename";

    private List<String> header;
    private List<String> values;
    private int length;

    public FixtureDataWrapper(List<List<String>> table) {
        super();
        header = table.get(0);
        values = table.get(1);
        length = header.size();
    }
    
    public FixtureDataWrapper(Map<String,String> map) {
        super();
        header = new LinkedList<String>();
        values = new LinkedList<String>();
        for(String key: map.keySet()){
            header.add(key);
            values.add( map.get(key) );
        }
        length = header.size();
    }
    
    public Map<String,String> asMap(){
        HashMap<String, String> map = new HashMap<String,String>();
        for(int i = 0; i < header.size(); i++){
            map.put(header.get(i), values.get(i));
        }
        return map;
    }
    
    public List<List<String>> asFitnesseList(){
        return Arrays.asList(header,values);
    }
    
    public Map<String, Map<String,String>> asFixtureMap(String key){
        Map<String, Map<String,String>> map = new HashMap<String, Map<String,String>>();
        map.put(key, asMap());
        return map;
    }
    
    public String getFixtureName(){
        for (int i = 0; i < header.size(); i++) {
            if(FIXTURE_NAME.equals( header.get(i) ) )
                return values.get(i);
        }
        return null;
    }
    
    public int size(){
        return length;
    }
    
    public String get(int pos){
        return values.get(pos);
    }
    
    public List<String> getHeader(){
        return header;
    }
    
}
