package cm.aptoide.pt.multiversion;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author rafael
 * @since 2.5.3
 * 
 * A description of a application version that enables to work easily with them.
 */
public class VersionApk implements Comparable<VersionApk>, Parcelable{
	
	/**
	 * Formated version
	 */
	private String version; 
	/**
	 * A field to easily compare versions, this is the same string with out the dots
	 */
	private int versionCode;
	/**
	 * 
	 */
	private String apkId;
	
	/**
	 * 
	 */
	private int size;
	
	/**
	 * 
	 */
	private int downloads;
	
	/**
	 * 
	 * @param version
	 * @param apkId
	 * @param versionLabel
	 */
	public VersionApk(String version, int versionCode, String apkId, int size, int downloads) {
		
		this.size 			= size;
		this.version 		= version;
		this.versionCode 	= versionCode;
		this.apkId 			= apkId;
		this.downloads 		= downloads;
		
	}
	
	/**
	 * 
	 * @return Formated version
	 */
	public String getVersion() { return version; }
	
	/**
	 * 
	 * @return A field to easily compare versions, this is the same string as getVersionString() in with out the dots
	 */
	public int getVersionCode() { return versionCode; }
	
	/**
	 * 
	 * @return
	 */
	public String getApkId() { return apkId; }
	
	/**
	 * 
	 * @return
	 */
	public int getSize() { return size; }
	
	public int getDownloads() {
		return downloads;
	}
	
	/**
	 * 
	 * @param version
	 * @return A field to easily compare versions, this is the same string with out the dots
	 */
	public static String processRawVersion(final String version){
		return version.replace(".", "");	
	}
	
	@Override
	public String toString() { return "apkId: "+apkId+" version: "+version+" size:"+size; }
	
	@Override
	public boolean equals(Object version) {
		
		if(!(version instanceof VersionApk))
			throw new IllegalArgumentException("The given argument is not a instance of Version class.");
		
		VersionApk versionApk = (VersionApk)version;
		
		return versionCode == versionApk.getVersionCode() && this.apkId.equals(versionApk.getApkId());
		
	}
	
	public int compareTo(VersionApk version) {
		return versionCode-version.getVersionCode();
	}
	
	public static String getStringFromVersionApkList(ArrayList<VersionApk> versionsApk){
		StringBuilder strBuilder = new StringBuilder("");
		Iterator<VersionApk> iteratorVersions = versionsApk.iterator();
		while(iteratorVersions.hasNext()){
			strBuilder.append(iteratorVersions.next().getVersion());
			if(iteratorVersions.hasNext()){
				strBuilder.append(", ");
			}
		}
		return strBuilder.toString();
	}
	
	/*
	 * Necessary to parcellation...
	 */

	/**
	 * 
	 * @param in
	 */
	public VersionApk(Parcel in) { 
		this(in.readString(),in.readInt(),in.readString(), in.readInt(), in.readInt());
	}
	
	/**
	 * It may happen that your class will have child classes, so each of child in this case can 
	 * return in describeContent() different values, 
	 * so you would know which particular object type to create from Parcel.
	 * 
	 * In this case one doesn't need to implement all 
	 * Parcelable methods in child classes - except describeContent(), this one.
	 */
	public int describeContents() { return 0; }

	/**
	 * 
	 */
	public void writeToParcel(Parcel dest, int flags) { 
		dest.writeString(this.getVersion()); 
		dest.writeInt(this.getVersionCode());
		dest.writeString(this.getApkId());
		dest.writeInt(this.size);
		dest.writeInt(this.downloads);
	}
	
	/**
	 * 
	 */
	public static final Parcelable.Creator<VersionApk> CREATOR = new Parcelable.Creator<VersionApk>() {
		public VersionApk createFromParcel(Parcel in) { return new VersionApk(in); }	
		public VersionApk[] newArray(int size) { return new VersionApk[size]; }
	};
	
}
