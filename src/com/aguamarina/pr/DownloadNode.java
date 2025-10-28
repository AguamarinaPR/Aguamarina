package cm.aptoide.pt;

public class DownloadNode extends Object{
	
	private String repo;
	private String remotePath;
	private String md5sum;
	private int size;
	private String localPath;
	private String appName;
	private String packageName;
	private String[] logins;
	private boolean isRepoPrivate;
	private boolean isUpdate;

	
	public DownloadNode(String repo, String remotePath, String md5sum, int size) {
		super();
		this.repo = repo;
		this.remotePath = remotePath;
		this.md5sum = md5sum;
		this.size = size;
		this.isRepoPrivate = false;
		this.isUpdate = false;
	}


	public DownloadNode(String remotePath, String md5sum, int size, String localPath, String packageName) {
		super();
		this.remotePath = remotePath;
		this.md5sum = md5sum;
		this.size = size;
		this.localPath = localPath;
		this.packageName = packageName;
		this.isRepoPrivate = false;
		this.isUpdate = false;
	}

	
	public String getRepo() {
		return repo;
	}

	public String getRemotePath() {
		return remotePath;
	}
	
	public String getMd5sum() {
		return md5sum;
	}
	
	public int getSize() {
		return size;
	}

	public String getLocalPath() {
		return localPath;
	}
	
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String apkName) {
		this.packageName = apkName;
	}

	public String[] getLogins() {
		return logins;
	}
	
	public void setLogins(String[] logins) {
		if(logins != null){
			this.logins = logins;
			this.isRepoPrivate = true;
		}
	}
	
	public boolean isRepoPrivate(){
		return isRepoPrivate;
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}
	
		
	/*Changed by Rafael Campos*/
	public String version; 
}
