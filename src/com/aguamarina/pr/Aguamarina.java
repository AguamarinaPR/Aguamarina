/*
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
/* Copyright (C) 2023-2025 TropicalBananas */

package com.aguamarina.pr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
//import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
//import java.util.concurrent.TimeoutException;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;*/
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
//import android.app.ProgressDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;


public class Aguamarina extends Activity { 
	

    
	private static final int LOAD_TABS = 0;
	private static final int UPDATE_SELF = 99;
    private static final String TMP_MYAPP_FILE = Environment.getExternalStorageDirectory().getPath() + "/.aguamarina/server";
	
	private WakeLock keepScreenOn;
	private Intent DownloadQueueServiceIntent;
    
    private Vector<String> server_lst = null;
    private Vector<String[]> get_apps = null;
    
    // Used for Aguamarina version update
	private DbHandler db = null;

	private SharedPreferences sPref;
	private SharedPreferences.Editor prefEdit;

	private PackageInfo pkginfo;	
	
	private ProgressBar mProgress;
	private int mProgressStatus = 0;
	
	private Context mctx;
	
	private DownloadQueueService downloadQueueService;
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        downloadQueueService = ((DownloadQueueService.DownloadQueueBinder)serviceBinder).getService();

	        downloadQueueService.setCurrentContext(mctx);
	        
	        Log.d("Aguamarina", "DownloadQueueService bound to Aguamarina");
	    }
	    
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        downloadQueueService = null;
	        
	        Log.d("Aguamarina","DownloadQueueService unbound from Aguamarina");
	    }

	};	
	
	private Bundle savedInstanceState;
	
	private Handler mHandler = new Handler();

	
	private Handler startHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case LOAD_TABS:
				Intent i = new Intent(Aguamarina.this, RemoteInTab.class);
				Intent get = getIntent();
				if(get.getData() != null){
					String uri = get.getDataString();
					if(uri.startsWith("aguamarinarepo")){
						Log.d("Aguamarina-startHandler", "aguamarinarepo-scheme");
						String repo = uri.substring(14);
						i.putExtra("newrepo", repo);
					}else if(uri.startsWith("aguamarinaxml")){
						Log.d("Aguamarina-startHandler", "aguamarinaxml-scheme");
						String repo = uri.substring(13);
						parseXmlString(repo);
						i.putExtra("repos", server_lst);
						if(get_apps.size() > 0){
							//i.putExtra("uri", TMP_SRV_FILE);
							i.putExtra("apps", get_apps);

						}
						//i.putExtra("linkxml", repo);
					}else{
						Log.d("Aguamarina-startHandler", "receiving a myapp file");
						downloadMyappFile(uri);
						try {
							parseMyappFile(TMP_MYAPP_FILE);
							i.putExtra("repos", server_lst);
							if(get_apps.size() > 0){
								//i.putExtra("uri", TMP_SRV_FILE);
								i.putExtra("apps", get_apps);
	
							}
						} catch (Exception e) {
							Toast.makeText(mctx, mctx.getString(R.string.failed_install), Toast.LENGTH_LONG);
							onCreate(savedInstanceState);
						}
					}
				}
				startActivityForResult(i,0);
				break;
			}
			super.handleMessage(msg);
		} 
    }; 
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mctx = this;

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keepScreenOn = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "Full Power"); 
    	DownloadQueueServiceIntent = new Intent(mctx, DownloadQueueService.class);
    	startService(DownloadQueueServiceIntent);

    	mctx.bindService(new Intent(mctx, DownloadQueueService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    	
//@dsilveira  #534 +10lines Check if Aguamarina is already running to avoid wasting time and showing the splash
    	ActivityManager activityManager = (ActivityManager)mctx.getSystemService(Context.ACTIVITY_SERVICE);
    	List<RunningTaskInfo> running = activityManager.getRunningTasks(Integer.MAX_VALUE);
    	for (RunningTaskInfo runningTask : running) {
			if(runningTask.baseActivity.getClassName().equals("com.aguamarina.pr.RemoteInTab")){	//RemoteInTab is the real Aguamarina Activity
				Message msg = new Message();
	            msg.what = LOAD_TABS;
	            startHandler.sendMessage(msg);
	            return;
			}
		}
			
        Log.d("Aguamarina","******* \n Downloads will be made to: " + Environment.getExternalStorageDirectory().getPath() + "\n ********");

        sPref = getSharedPreferences("aguamarina_prefs", MODE_PRIVATE);
		prefEdit = sPref.edit();
        
   		
   		PackageManager mPm = getPackageManager();
   		try {
			pkginfo = mPm.getPackageInfo("com.aguamarina.pr", 0);
   		} catch (NameNotFoundException e) {	}
   		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
   		/*try{
			if( pkginfo.versionCode < Integer.parseInt( getXmlElement("versionCode") ) ){
				Log.d("Aguamarina-VersionCode", "Using version "+pkginfo.versionCode+", suggest update!");
				requestUpdateSelf();
			}else{
				proceed();
			}
   		}catch(Exception e){
   			e.printStackTrace();
   			proceed();
   		}*/
		proceed();
    }
    
    private void proceed(){
   		db = new DbHandler(this);
   		
    	if(sPref.getInt("version", 0) < pkginfo.versionCode){
	   		db.UpdateTables();
	   		prefEdit.putBoolean("mode", true);
	   		prefEdit.putBoolean("hideincomp", true);
	   		prefEdit.putInt("version", pkginfo.versionCode);
	   		prefEdit.commit();
		}
		
    	// UB is short for "Unique Badge", which is just the term I'll be using for UUIDs.
		if(sPref.getString("myUB", null) == null){
			String rand_ub = UUID.randomUUID().toString();
			prefEdit.putString("myUB", rand_ub);
			prefEdit.commit();
		}
		
		if(sPref.getInt("scW", 0) == 0 || sPref.getInt("scH", 0) == 0){
			 DisplayMetrics dm = new DisplayMetrics();
		     getWindowManager().getDefaultDisplay().getMetrics(dm);
		     prefEdit.putInt("scW", dm.widthPixels);
		     prefEdit.putInt("scH", dm.heightPixels);
		     prefEdit.commit();
		}
		
		if(sPref.getString("icdown", null) == null){
			prefEdit.putString("icdown", "g3w");
			prefEdit.commit();
		}
    
        setContentView(R.layout.start);
        
        mProgress = (ProgressBar) findViewById(R.id.pbar);
       
        new Thread(new Runnable() {
            public void run() {
            	Vector<ApkNode> apk_lst = db.getAll("abc");
            	mProgress.setMax(apk_lst.size());
        		PackageManager mPm;
        		PackageInfo pkginfo;
        		mPm = getPackageManager();
        		
        		keepScreenOn.acquire();
        		
        		for(ApkNode node: apk_lst){ 
        			if(node.status == 0){
       				 try{
       					 pkginfo = mPm.getPackageInfo(node.apkid, 0);
       					 String vers = pkginfo.versionName;
       					 int verscode = pkginfo.versionCode;
       					 db.insertInstalled(node.apkid, vers, verscode);
       				 }catch(Exception e) {
       					 //Not installed anywhere... does nothing
       				 }
       			 }else{
       				 try{
       					 pkginfo = mPm.getPackageInfo(node.apkid, 0);
       					 String vers = pkginfo.versionName;
       					 int verscode = pkginfo.versionCode;
       					 db.UpdateInstalled(node.apkid, vers, verscode);
       				 }catch (Exception e){
       					 db.removeInstalled(node.apkid);
       				 }
       			 }
                    mProgressStatus++;
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                }
        		
        		keepScreenOn.release();
        		
                Message msg = new Message();
                msg.what = LOAD_TABS;
                startHandler.sendMessage(msg); 
            }
        }).start();
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode){
			case UPDATE_SELF:
				proceed();
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				try{
					db.clodeDb();
				}catch (Exception e) {Log.d("Aguamarina","Exit error: " + e.toString());	}
				finally{
					this.finish();
				}
			break;
		}
	}
    
	private void downloadMyappFile(String myappUri){
		try{
			keepScreenOn.acquire();
			
			BufferedInputStream getit = new BufferedInputStream(new URL(myappUri).openStream());

			File file_teste = new File(TMP_MYAPP_FILE);
			if(file_teste.exists())
				file_teste.delete();
			
			FileOutputStream saveit = new FileOutputStream(TMP_MYAPP_FILE);
			BufferedOutputStream bout = new BufferedOutputStream(saveit,1024);
			byte data[] = new byte[1024];
			
			int readed = getit.read(data,0,1024);
			while(readed != -1) {
				bout.write(data,0,readed);
				readed = getit.read(data,0,1024);
			}
			
			keepScreenOn.release();
			
			bout.close();
			getit.close();
			saveit.close();
		} catch(Exception e){
			AlertDialog p = new AlertDialog.Builder(this).create();
			p.setTitle(getText(R.string.top_error));
			p.setMessage(getText(R.string.aguamarina_error));
			p.setButton(getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			          return;
			        } });
			p.show();
		}
	}
	
	private void parseMyappFile(String file){
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    try {
	    	keepScreenOn.acquire();
	    	
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	NewServerRssHandler handler = new NewServerRssHandler(this);
	    	xr.setContentHandler(handler);
	    	
	    	InputStreamReader isr = new FileReader(new File(file));
	    	InputSource is = new InputSource(isr);
	    	xr.parse(is);
	    	File xml_file = new File(file);
	    	xml_file.delete();
	    	server_lst = handler.getNewSrvs();
	    	get_apps = handler.getNewApps();
	    	
	    	keepScreenOn.release();
	    	
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (SAXException e) {
	    	e.printStackTrace();
	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private void parseXmlString(String file){
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    try {
	    	keepScreenOn.acquire();
	    	
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	NewServerRssHandler handler = new NewServerRssHandler(this);
	    	xr.setContentHandler(handler);
	    	
	    	InputSource is = new InputSource();
	    	is.setCharacterStream(new StringReader(file));
	    	xr.parse(is);
	    	server_lst = handler.getNewSrvs();
	    	get_apps = handler.getNewApps();
	    	
	    	keepScreenOn.release();
	    	
	    } catch (IOException e) {
	    } catch (SAXException e) {
	    } catch (ParserConfigurationException e) {
		}
	}
	
	/*private String getXmlElement(String name) throws ParserConfigurationException, MalformedURLException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse( new InputSource(new URL(LATEST_VERSION_CODE_URI).openStream()) );
        dom.getDocumentElement().normalize();
        NodeList items = dom.getElementsByTagName(name);
        for(int i=0;i<items.getLength();){
        	Node item = items.item(i);
        	Log.d("Aguamarina-XmlElement Name", item.getNodeName());
        	Log.d("Aguamarina-XmlElement Value", item.getFirstChild().getNodeValue().trim());
        	return item.getFirstChild().getNodeValue().trim();
        }
        return "0";
	}*/
	
	/*private void requestUpdateSelf(){
		
    	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
    	alertBuilder.setCancelable(false)
    				.setPositiveButton(R.string.dialog_yes , new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    						setContentView(R.layout.auto_updating);
    						new DownloadSelfUpdate().execute();
    					}
    				})    	
    				.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    						proceed();
    					}
    				})
    				.setMessage(R.string.update_self_msg)
    				;
    	
    	AlertDialog alert = alertBuilder.create();
    	
    	alert.setTitle(R.string.update_self_title);
    	alert.setIcon(R.drawable.icon);
    	
    	alert.show();
	}*/
	
	/*private class DownloadSelfUpdate extends AsyncTask<Void, Void, Void>{
		private final ProgressDialog dialog = new ProgressDialog(Aguamarina.this);
	
		String latestVersionUri;
		String referenceMd5;
		
		
		void retrieveUpdateParameters(){
			try{
				latestVersionUri = getXmlElement("uri");
				referenceMd5 = getXmlElement("md5");
			}catch (Exception e) {
				e.printStackTrace();
				Log.d("Aguamarina-Auto-Update", "Update connection failed!  Keeping current version.");
			}
		}
		
		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Retrieving update...");
			this.dialog.show();
			super.onPreExecute();
			retrieveUpdateParameters();
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			try{
				if(latestVersionUri==null){
					retrieveUpdateParameters();
				}
//				Message msg_al = new Message();
				// If file exists, removes it...
				 File f_chk = new File(TMP_UPDATE_FILE);
				 if(f_chk.exists()){
					 f_chk.delete();
				 }
				 f_chk = null;
				
				FileOutputStream saveit = new FileOutputStream(TMP_UPDATE_FILE);
				DefaultHttpClient mHttpClient = new DefaultHttpClient();
				HttpGet mHttpGet = new HttpGet(latestVersionUri);
	
				HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
				
				if(mHttpResponse == null){
					 Log.d("Aguamarina","Problem in network... retry...");	
					 mHttpResponse = mHttpClient.execute(mHttpGet);
					 if(mHttpResponse == null){
						 Log.d("Aguamarina","Major network exception... Exiting!");
						 //msg_al.arg1= 1;
						 //download_error_handler.sendMessage(msg_al);
						 throw new TimeoutException();
					 }
				 }
				
				if(mHttpResponse.getStatusLine().getStatusCode() == 401){
					throw new TimeoutException();
				}else{
					InputStream getit = mHttpResponse.getEntity().getContent();
					byte data[] = new byte[8096];
					int bytesRead;
					bytesRead = getit.read(data, 0, 8096);
					while(bytesRead != -1) {
	//							download_tick.sendEmptyMessage(readed);
						saveit.write(data,0,bytesRead);
						bytesRead = getit.read(data, 0, 8096);
					}
					Log.d("Aguamarina","Download done!");
					saveit.flush();
					saveit.close();
					getit.close();
				}
			}catch (Exception e) { 
//						download_error_handler.sendMessage(msg_al);
				e.printStackTrace();
				Toast.makeText(mctx, mctx.getString(R.string.network_auto_update_error), Toast.LENGTH_LONG);
				Log.d("Aguamarina-Auto-Update", "Update connection failed!  Keeping current version.");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			super.onPostExecute(result);
			
			if(!(referenceMd5==null)){
				try{
					File apk = new File(TMP_UPDATE_FILE);
					Md5Handler hash = new Md5Handler();
					if( referenceMd5.equalsIgnoreCase(hash.md5Calc(apk))){
		//				msg_al.arg1 = 1;
		//						download_handler.sendMessage(msg_al);
						
						doUpdateSelf();
				    	
					}else{
						Log.d("Aguamarina",referenceMd5 + " VS " + hash.md5Calc(apk));
		//				msg_al.arg1 = 0;
		//						download_error_handler.sendMessage(msg_al);
						throw new Exception(referenceMd5 + " VS " + hash.md5Calc(apk));
					}
				}catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(mctx, mctx.getString(R.string.md5_auto_update_error), Toast.LENGTH_LONG);
					Log.d("Aguamarina-Auto-Update", "Update package checksum failed!  Keeping current version.");
					if (this.dialog.isShowing()) {
						this.dialog.dismiss();
					}
					proceed();
					super.onPostExecute(result);
					
				}
			}
			
		}
		
	}
	
	private void doUpdateSelf(){
		Intent intent = new Intent();
    	intent.setAction(android.content.Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.parse("file://" + TMP_UPDATE_FILE), "application/vnd.android.package-archive");
    	
    	startActivityForResult(intent, UPDATE_SELF);
	}*/

	@Override
	protected void onDestroy() {
		mctx.unbindService(serviceConnection);
		stopService(DownloadQueueServiceIntent);
		super.onDestroy();
	}
	
	
	
}
