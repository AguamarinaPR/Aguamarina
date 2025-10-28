package cm.aptoide.pt;

public class ApkNodeFull extends Object{
	
	public String name;
	public String apkid;
	public String path;
	public String ver;
	public int vercode;
	public String date;
	public float rat;
	public int down;
	public String md5hash;
	public String catg;
	public int catg_type;
	public int size;
	
	public boolean isnew;
	
	public ApkNodeFull(String apkid){
		this.apkid = apkid;
	}
	
	public ApkNodeFull(){	}
	
	@Override
	public boolean equals(Object o) {
		if(o != null){
			ApkNodeFull node = (ApkNodeFull)o;
			if(this.apkid.equals(node.apkid))
				return true;
		}
		return false;
	}

}
