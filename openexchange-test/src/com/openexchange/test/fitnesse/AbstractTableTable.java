package com.openexchange.test.fitnesse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.test.fixtures.FixtureException;


public class AbstractTableTable implements SlimTableTable{
    
    public Map<String,String> readAsMap(List<List<String>> input){
        HashMap<String, String> map = new HashMap<String,String>();
        List<String> header = readHeader(input);
        List<String> values = input.get(1);
        for(int i = 0; i < header.size(); i++){
            map.put(header.get(i), values.get(i));
        }
        return map;
    }
    
    public List<List<String>> readAsDoubleList(Map<String,String> input){
        List<String> header = new LinkedList<String>();
        List<String> values = new LinkedList<String>();
        for(String key: input.keySet()){
            header.add(key);
            values.add( input.get(key) );
        }
        return Arrays.asList(header, values);
    }
    
    public List<String> readHeader(List<List<String>> table){
        return table.get(0);
    }
    
    public List<List<String>> generateDoubleList(int size, String defaultValue){
        List<String> list = new LinkedList<String>();
        return Arrays.asList(list, list);
    }
    
    public List doTable(List<List<String>> table) throws Exception {
        return generateDoubleList( table.size(), "");
    }
}
