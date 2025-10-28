/* Copyright (C) 2023-2025 TropicalBananas */

package com.aguamarina.pr;

import java.io.Serializable;

public class IconNode extends Object implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String url;
	public String name;

	public IconNode(String url, String name) {
		this.url = url;
		this.name = name;
	}
	
}
