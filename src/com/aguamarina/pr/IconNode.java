package cm.aptoide.pt;

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
