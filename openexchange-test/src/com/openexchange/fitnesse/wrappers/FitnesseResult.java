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
    private int length;
    
    public FitnesseResult(FixtureDataWrapper givenData){
        this(givenData, NEUTRAL);
    }

    public FitnesseResult(FixtureDataWrapper givenData, String defaultValue){
        length = givenData.size();
        header = new LinkedList<String>();
        values = new LinkedList<String>();
        
        for (int i = 0; i < length; i++) {
            header.add(PASS);
            values.add(defaultValue);    
        }
    }
    
    public List<List<String>> toResult(){
        return Arrays.asList(header,values);
    }
    
    public void set(int pos, String value){
        values.set(pos, value);
    }
}
