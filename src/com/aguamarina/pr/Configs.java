package com.aguamarina.pr;

import java.text.SimpleDateFormat;

/**
 * @author rafael
 * @since summerinternship2011
 * 
 */
public class Configs {
	 
	public final static SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("d-M-y H:m:s");
	
	//public final static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/*public static final boolean INTERFACE_SILVER_TABS_ON	= false;
	public static final boolean INTERFACE_TABS_ON_BOTTOM 	= false;
	public static final boolean BACKGROUND_ON_TABS 			= false;*/
	
	public static final String TERMINAL_INFO = android.os.Build.MODEL + "("+ android.os.Build.PRODUCT + ")"
	+";v"+android.os.Build.VERSION.RELEASE+";"+System.getProperty("os.arch");
	
	private Configs(){}
	
}