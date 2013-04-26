package org.quickcached.binary;

/**
 *
 * @author akshath
 */
public class OpCode {
	public static final String NOOP = "0A";
	public static final String VERSION = "0B";
	
	public static final String SET = "01";
	public static final String SET_Q = "11";
	
	public static final String ADD = "02";
	public static final String ADD_Q = "12";
	
	public static final String REPLACE = "03";
	public static final String REPLACE_Q = "13";
	
	public static final String APPEND = "0E";
	public static final String APPEND_Q = "19";
	
	public static final String PREPEND = "0F";
	public static final String PREPEND_Q = "1A";
	
	public static final String DELETE = "04";
	public static final String DELETE_Q = "14";
	
	public static final String INCREMENT = "05";
	public static final String INCREMENT_Q = "15";
	
	public static final String DECREMENT = "06";
	public static final String DECREMENT_Q = "16";
	
	public static final String GET = "00";
	public static final String GET_Q = "09";
	
	public static final String GET_K = "0C";
	public static final String GET_K_Q = "0D";
	
	public static final String GAT = "1D";
	public static final String GAT_Q = "1E";
	
	public static final String QUIT = "07";
	public static final String QUIT_Q = "17";
	
	public static final String FLUSH = "08";
	public static final String FLUSH_Q = "18";
	
	public static final String STAT = "10";
	
	public static final String TOUCH = "1C";
}
