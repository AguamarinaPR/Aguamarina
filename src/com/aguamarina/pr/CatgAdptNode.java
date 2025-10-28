package cm.aptoide.pt;

import android.widget.SimpleAdapter;

public class CatgAdptNode extends Object{

	private String name;
	private SimpleAdapter adpt;
	private int count;
	
	public CatgAdptNode(String name){
		this.name = name;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setAdpt(SimpleAdapter adpt){
		this.adpt = adpt;
	}
	
	public SimpleAdapter getAdpt(){
		return adpt;
	}

	@Override
	public boolean equals(Object o) {
		if(o != null){
			CatgAdptNode tmp = (CatgAdptNode)o;
			if(this.name.equals(tmp.name))
				return true;
		}
		return false;
	}
	
	
}
