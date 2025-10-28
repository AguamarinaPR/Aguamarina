package cm.aptoide.pt;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

public class FetchIconsService extends Service{
	

	private List<ServiceIcon> parsedList = null;
	private Thread workingPool = null;
	
	private Context mctx = null;
	
	private boolean alive = true;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mctx = this;
		
		parsedList = new ArrayList<ServiceIcon>();
		workingPool = new Thread(new WorkThread(), "T1");
		workingPool.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
		//workingPool.start();
		
		Log.d("Aptoide","......................... onCreate FetchIcons Service");
	}
	

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Bundle intent_info = intent.getExtras();
		ServiceIcon tmp = new ServiceIcon(intent_info.getString("srv"), intent_info.getStringArray("login"), (ArrayList<IconNode>)intent_info.getSerializable("icons"));
		parsedList.add(tmp);
		/*if(!workingPool.isAlive())
			workingPool.start();*/
		//workingPool.interrupt();
		if(workingPool.getState() == State.NEW){
			workingPool.start();
		}
		Log.d("Aptoide","......................... onStart FetchIcons Service go!");
	}

	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(workingPool.isAlive()){
			alive = false;
		}
		Log.d("Aptoide","......................... onDestroy FetchIcons Service");
	}

	
	private class WorkThread implements Runnable {
		public WorkThread() {	}
		
		public void run() {
			try{
				int chk = 10;
				while(chk > 0){
					chk--;
					Log.d("Aptoide","Count in: " + chk);
					while((parsedList.size() > 0) && alive){
						chk = 10;
						Log.d("Aptoide","We are fetching icons...");
						FetchIcons(parsedList.remove(0));
					}
					Thread.sleep(2000);
				}
				Log.d("Aptoide","Bye bye...");
				stopSelf();
			}catch (Exception e){ 	}
		}
	}
	
	/*private class FetchIcons implements Runnable {
		private String server;
		private String user;
		private String pswd;
		private List<IconNode> lst;
		
		public FetchIcons(ServiceIcon node) {	
			server = node.mserver;
			user = node.usern;
			pswd = node.passwd;
			lst = node.iconsLst;
		}
		
		public void run() {
			try{
				for(IconNode node : lst){
					String test_file = mctx.getString(R.string.icons_path) + node.name;
					
					File exists = new File(test_file);
					if(!exists.exists())
						getIcon(node.url, node.name, server, user, pswd);
				}
			}catch (Exception e){ 
				Log.d("Aptoide", "Wash exception? " + e.toString());
			}
		}
	}*/
	
	private void FetchIcons(ServiceIcon fetch) {
		String server = fetch.mserver;
		String user = fetch.usern;
		String pswd = fetch.passwd;
		List<IconNode> lst = fetch.iconsLst;
		
		try{
			for(IconNode node : lst){
				if(!alive){
					Log.d("Aptoide-FetchIconService","not alive");
					break;
				}
				Log.d("Aptoide","On icon: " + node.name);
				String test_file = mctx.getString(R.string.icons_path) + node.name;
				
				File exists = new File(test_file);
				if(!exists.exists()){
					getIcon(node.url, node.name, server, user, pswd);
				}else{
					Log.d("Aptoide","FILE EXISTS: " + node.name);
				}
			}
		}catch (Exception e){ 
			Log.d("Aptoide", "Wash exception? " + e.toString());
		}
	}
	
	private void getIcon(String uri, String name, String mserver, String usern, String passwd){
		String url = mserver + "/" + uri;
		String file = mctx.getString(R.string.icons_path) + name;
		
		
		try {
			FileOutputStream saveit = new FileOutputStream(file);
			
			HttpResponse mHttpResponse = NetworkApis.getHttpResponse(url, usern, passwd, mctx);
			
			if(mHttpResponse.getStatusLine().getStatusCode() == 401){
				return;
			}else if(mHttpResponse.getStatusLine().getStatusCode() == 403){
				return;
			}else{
				byte[] buffer = EntityUtils.toByteArray(mHttpResponse.getEntity());
				saveit.write(buffer);
			}
			
			Log.d("Aptoide","getIcon done: " + uri + "/" + name + " - " + mserver);

			
		}catch (Exception e){
			Log.d("Aptoide","Error fetching icon.");
		}
	}
	
	private class ServiceIcon extends Object{
		
		public String mserver;
		public String usern;
		public String passwd;
		public List<IconNode> iconsLst;
		
		public ServiceIcon(String srv, String[] login, ArrayList<IconNode> lst){
			this.mserver = srv;
			this.usern = login[0];
			this.passwd = login[1];
			this.iconsLst = lst;
		}
	}
	
	
}
