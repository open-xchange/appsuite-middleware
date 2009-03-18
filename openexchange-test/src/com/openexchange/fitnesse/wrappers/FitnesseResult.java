package com.openexchange.fitnesse.wrappers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * {@link FitnesseResult} - FitNesse expects a rather convoluted set of nested lists
 * as a result. This class makes accessing the values easier.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class FitnesseResult {

    public static final String NEUTRAL = "";
    public static final String PASS = "pass";
    public static final String ERROR = "error:";
    
    private List<String> header;
    private List<String> values;
    private int length, height;
    
    public FitnesseResult(FixtureDataWrapper givenData){
        this(givenData, NEUTRAL);
    }

    public FitnesseResult(FixtureDataWrapper givenData, String defaultValue){
    }
    
    public FitnesseResult(int length, int height, String defaultValue) {
        this.length = length;
        header = new LinkedList<String>();
        this.values = new LinkedList<String>();
        this.height = height;
        for (int i = 0; i < length; i++) {
            header.add(NEUTRAL);
            values.add(defaultValue);    
        }
        
    }
    
    public List<List<String>> toResult(){
        LinkedList<List<String>> result = new LinkedList<List<String>>();
        
        if(height == 1){
            result.add(values);
        }
        if(height == 2){
            result.add(header);
            result.add(values);
        }
        if(height == 3){
            result.add(new LinkedList<String>());
            result.add(header);
            result.add(values);
        }
        
        return result;
    }
    
    public void set(int pos, String value){
        values.set(pos, value);
    }
}
