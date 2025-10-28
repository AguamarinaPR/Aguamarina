/* Copyright (C) 2023-2025 TropicalBananas */

package com.aguamarina.pr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter.ViewBinder;

public class BaseManagement extends Activity {

	static private PackageManager mPm;
	static private PackageInfo pkginfo;
	
	static private DbHandler db = null;
	static private Context mctx = null;
	
	static protected SharedPreferences sPref;
	static protected SharedPreferences.Editor prefEdit;

	static private ProgressDialog pd;
	
	static protected Vector<ApkNode> apk_lst = null;
	
	static private boolean pop_change = false;
	
	static private String order_lst = "abc";
	
	static protected SimpleAdapter availAdpt = null;
	static protected SimpleAdapter instAdpt = null;
	static protected SimpleAdapter updateAdpt = null;
	
	protected static final int INSTALL = 127;
	protected static final int REMOVE = 128;
	protected static final int UPDATE = 129;
	protected static final String LOCAL_APK_PATH = Environment.getExternalStorageDirectory().getPath()+"/.aguamarina/";
	
	
	private static SimpleAdapter main_catg_adpt = null;
	private static SimpleAdapter app_catg_adpt = null;
	private static SimpleAdapter game_catg_adpt = null;
	
	
	private static final String[] main_ctg = {"Games", "Applications", "Others"};
	private static final String[] app_ctg = {"Comics", "Communication", "Entertainment", "Finance", "Health", "Lifestyle", "Multimedia", 
   		 "News & Weather", "Productivity", "Reference", "Shopping", "Social", "Sports", "Themes", "Tools", 
		 "Travel", "Demo", "Software Libraries", "Other"};
	private static final String[] game_ctg = {"Arcade & Action", "Brain & Puzzle", "Cards & Casino", "Casual", "Other"};
	

	private DownloadQueueService downloadQueueService;
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        downloadQueueService = ((DownloadQueueService.DownloadQueueBinder)serviceBinder).getService();

	        Log.d("Aguamarina-BaseManagement", "DownloadQueueService bound to a Tab");
	    }
	    
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        downloadQueueService = null;
	        
	        Log.d("Aguamarina-BaseManagement","DownloadQueueService unbound from a Tab");
	    }

	};	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getApplicationContext().bindService(new Intent(getApplicationContext(), DownloadQueueService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		
		mPm = getPackageManager();
		db = new DbHandler(this);
		mctx = this;
		sPref = getSharedPreferences("aguamarina_prefs", MODE_PRIVATE);
		prefEdit = sPref.edit();
		order_lst = sPref.getString("order_lst", "abc");
		redrawCatgList();
	}
	
	private void redrawCatgList(){
		int[] main_ctg_count = db.getCountMainCtg();
        Map<String, Object> count_lst_app = db.getCountSecCatg(1);
        Map<String, Object> count_lst_games = db.getCountSecCatg(0);
		
		List<Map<String, Object>> main_catg = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> app_catg = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> game_catg = new ArrayList<Map<String, Object>>();
        Map<String, Object> apk_line = null;
        
        int p = 0;
        for (String node : main_ctg) {
        	apk_line = new HashMap<String, Object>();
        	apk_line.put("name", node);
        	if(main_ctg_count == null)
        		apk_line.put("cat_count","0 available");
        	else
        		apk_line.put("cat_count",main_ctg_count[p] + " available");
            main_catg.add(apk_line);
            p++;
		}
        main_catg_adpt = new SimpleAdapter(mctx, main_catg, R.layout.catglist, 
         		new String[] {"name", "name", "cat_count"}, new int[] {R.id.cntrl, R.id.name, R.id.cat_count});
        
        
        for (String node : app_ctg) {
        	Integer count = new Integer(0);
        	if(count_lst_app != null){
        		count = (Integer)count_lst_app.get(node);
        		if(count == null)
        			count = 0;
        	}
        	apk_line = new HashMap<String, Object>();
        	apk_line.put("cntrl", "apps");
        	apk_line.put("name", node);
            apk_line.put("cat_count",count + " available");
            app_catg.add(apk_line);
		}
        app_catg_adpt = new SimpleAdapter(mctx, app_catg, R.layout.catglist, 
         		new String[] {"cntrl", "name", "cat_count"}, new int[] {R.id.cntrl, R.id.name, R.id.cat_count});
        
        
        for (String node : game_ctg) {
        	Integer count = new Integer(0);
        	if(count_lst_games != null){
        		count = (Integer)count_lst_games.get(node);
        		if(count == null)
        			count = 0;
        	}
        	
        	apk_line = new HashMap<String, Object>();
        	apk_line.put("cntrl", "games");
        	apk_line.put("name", node);
        	apk_line.put("cat_count",count + " files");
        	game_catg.add(apk_line);
        }
        
       game_catg_adpt = new SimpleAdapter(mctx, game_catg, R.layout.catglist, 
        		new String[] {"cntrl", "name", "cat_count"}, new int[] {R.id.cntrl, R.id.name, R.id.cat_count});
	}
	
	
	protected AlertDialog resumeMe(){
		//String istype;
		
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.orderpopup, null);
		Builder alrt = new AlertDialog.Builder(this).setView(view);
		final AlertDialog p = alrt.create();
		p.setIcon(android.R.drawable.ic_menu_sort_by_size);
		p.setTitle(getString(R.string.order_popup_title));
		
		p.setButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				if(pop_change){
					prefEdit.putBoolean("pop_changes", true);
					prefEdit.commit();
					pop_change = false;
				}
				p.dismiss();
			}
		});
		
		// ***********************************************************
		// Categories
		final RadioButton btn1 = (RadioButton) view.findViewById(R.id.shw_ct);
		final RadioButton btn2 = (RadioButton) view.findViewById(R.id.shw_all);
		if(sPref.getBoolean("mode", false)){
			btn1.setChecked(true);
		}else{
			btn2.setChecked(true);
		}
		final RadioGroup grp2 = (RadioGroup) view.findViewById(R.id.groupshow);
		grp2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == btn1.getId()){
					pop_change = true;
					prefEdit.putBoolean("mode", true);
				}else{
					pop_change = true;
					prefEdit.putBoolean("mode", false);
				}
				
			}
		});

		// ***********************************************************
		
		// ***********************************************************
		// Order
		final RadioButton ord_rct = (RadioButton) view.findViewById(R.id.org_rct);
		final RadioButton ord_abc = (RadioButton) view.findViewById(R.id.org_abc);
		final RadioButton ord_rat = (RadioButton) view.findViewById(R.id.org_rat);
		final RadioButton ord_dwn = (RadioButton) view.findViewById(R.id.org_dwn);
		
		if(order_lst.equals("abc"))
			ord_abc.setChecked(true);
		else if(order_lst.equals("rct"))
			ord_rct.setChecked(true);
		else if(order_lst.equals("rat"))
			ord_rat.setChecked(true);
		else if(order_lst.equals("dwn"))
			ord_dwn.setChecked(true);
		
		final RadioGroup grp1 = (RadioGroup) view.findViewById(R.id.groupbtn);
		grp1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == ord_rct.getId()){
					pop_change = true;
					order_lst = "rct";
				}else if(checkedId == ord_abc.getId()){
					pop_change = true;
					order_lst = "abc";
				}else if(checkedId == ord_rat.getId()){
					pop_change = true;
					order_lst = "rat";
				}else if(checkedId == ord_dwn.getId()){
					pop_change = true;
					order_lst = "dwn";
				}
			}
		});
		
		// ***********************************************************
		
		return p;
	}
		
	@Override
	protected void onResume() {
		super.onResume();
		if(sPref.getBoolean("search_updt", false)){
			prefEdit.remove("search_updt");
			prefEdit.commit();
			redraw();
			redrawCatgList();
		}else if(sPref.getBoolean("update", false)){
			prefEdit.remove("update");
			prefEdit.commit();
			/*if(sPref.contains("order_lst")){
				Log.d("Aguamarina","************************** This 1 ********************************");
				order_lst = sPref.getString("order_lst", "abc");
				prefEdit.remove("order_lst");
				prefEdit.commit();
			}*/
			redraw();
			redrawCatgList();
			
		}/*else if(sPref.contains("order_lst")){
			Log.d("Aguamarina","*********************** This 2 ******************************");
			order_lst = sPref.getString("order_lst", "abc");
			prefEdit.remove("order_lst");
			prefEdit.commit();
			redraw();
		}*/
	}

	protected void removeApk(String apk_pkg){
		try {
			pkginfo = mPm.getPackageInfo(apk_pkg, 0);
		} catch (NameNotFoundException e) {	}
		Uri uri = Uri.fromParts("package", pkginfo.packageName, null);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		prefEdit.putString("pkg", pkginfo.packageName);
		prefEdit.commit();
		startActivityForResult(intent,REMOVE); 
	}
	
	protected void installApk(String apk_path, String ver){

		try {
			pkginfo = mPm.getPackageArchiveInfo(apk_path, 0);
			if(pkginfo == null){
				// Ficheiro est� corrupto, temos de verificar!
			}else{
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + apk_path), "application/vnd.android.package-archive");
				prefEdit.putString("ver", ver);
				prefEdit.putString("pkg", pkginfo.packageName);
				prefEdit.commit();
				startActivityForResult(intent,INSTALL);
				Log.d("Aguamarina-BaseManagement", "Installing Apk: "+apk_path);
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.failed_install), Toast.LENGTH_LONG);
		}
	}
	
	protected void updateApk(String apk_path, String apk_id, String ver){	

		try {		
			pkginfo = mPm.getPackageArchiveInfo(apk_path, 0);
			Intent intent = new Intent();
	    	intent.setAction(android.content.Intent.ACTION_VIEW);
	    	intent.setDataAndType(Uri.parse("file://" + apk_path), "application/vnd.android.package-archive");
	//    	prefEdit.putString("pkg", pkginfo.packageName);
	    	prefEdit.putString("pkg", apk_id);
    		prefEdit.putString("ver", ver);
	    	prefEdit.commit();
	    	startActivityForResult(intent,UPDATE);
			Log.d("Aguamarina-BaseManagement", "Updating Apk: "+apk_path);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.failed_update), Toast.LENGTH_LONG);
		}
	}
	
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == INSTALL){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
				db.insertInstalled(apkid, sPref.getString("ver", null));
				prefEdit.remove("pkg"); prefEdit.remove("ver");
				prefEdit.commit();
				redrawCatgList();
				redraw();
			} catch (NameNotFoundException e) {	}
		}else if(requestCode == REMOVE){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
			} catch (NameNotFoundException e) {	
				db.removeInstalled(apkid);
				prefEdit.remove("pkg"); 
				prefEdit.remove("ver"); 
				prefEdit.commit();
				redrawCatgList();
				redraw();
			}
		}else if(requestCode == UPDATE){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
			} catch (NameNotFoundException e) {	}
			int vercode = pkginfo.versionCode;
			String version = sPref.getString("ver", null);
			if(db.wasUpdateOrDowngrade(apkid, vercode)){
				db.removeInstalled(apkid);
				db.insertInstalled(apkid, version);
				prefEdit.remove("pkg"); prefEdit.remove("ver");
				prefEdit.commit();
				redrawCatgList();
				redraw();
			}
		}
	}

	protected void redraw(){
		
		Log.d("Aguamarina","======================= I REDRAW");
		prefEdit.putBoolean("redrawis", true);
		prefEdit.commit();

		pd = ProgressDialog.show(mctx, getText(R.string.top_please_wait), getText(R.string.updating_msg), true);
		pd.setIcon(android.R.drawable.ic_dialog_info);


		new Thread() {

			public void run(){

				try{

					List<Map<String, Object>> availMap = new ArrayList<Map<String, Object>>();
					List<Map<String, Object>> instMap = new ArrayList<Map<String, Object>>();
					List<Map<String, Object>> updtMap = new ArrayList<Map<String, Object>>();
					Map<String, Object> apk_line;

					if(apk_lst != null)
						apk_lst.clear();
					apk_lst = db.getAll(order_lst);
					
					/*
					 * status
					 * 0 - not installed
					 * 1 - installed
					 * 2 - installed need update
					 * 3 - installed don't need update but downgrade possible
					 * 
					 */
					for(ApkNode node: apk_lst){
						
						apk_line = new HashMap<String, Object>();
						apk_line.put("pkg", node.apkid);
						String iconpath = new String(getString(R.string.icons_path)+node.apkid);
						apk_line.put("icon", iconpath);
						apk_line.put("rat", node.rat);
						
						if(node.down >= 0)
							apk_line.put("down", node.down + " Down.");
						
						if(node.status == 1){
							
							apk_line.put("status", getString(R.string.installed) + " " + node.ver);
							apk_line.put("name", node.name);
//							apk_line.put("statusSort", 1);
							instMap.add(apk_line);
							
						}else if(node.status == 2){
							
							apk_line.put("status2", getString(R.string.installed_update) + " " + node.ver);
							apk_line.put("name2", node.name);
//							apk_line.put("statusSort", 2);
							updtMap.add(apk_line);
							instMap.add(apk_line);
							
						}else if(node.status == 3){
							
							apk_line.put("status", getString(R.string.installed) + " " + node.ver);
							apk_line.put("status3", ", "+getString(R.string.downgrade_available));
							apk_line.put("name", node.name);
//							apk_line.put("statusSort", 3);
//							updtMap.add(apk_line);
							instMap.add(apk_line);

							
						}else{
							apk_line.put("status", "Version: " + node.ver);
							apk_line.put("name", node.name);
							availMap.add(apk_line);
						}
						
					}
					
					Comparator<Map<String, Object>> alphabeticComp = new Comparator<Map<String,Object>>(){
						//Order alphabetic ins
						public int compare(Map<String, Object> map1, Map<String, Object> map2) {	
							
							
							String name1 = ((String)map1.get("name2"))!=null?((String)map1.get("name2")):((String)map1.get("name"));
							String name2 = ((String)map2.get("name2"))!=null?((String)map2.get("name2")):((String)map2.get("name"));
							
							if(name1!=null && name1.length()>0){
								
								if(name2!=null && name2.length()>0){
									return ((int)(name1.charAt(0)))-((int)(name2.charAt(0)));
								}
								
								return 1;
								
							}
							
							return (name2==null || name2.length()==0)?0:-1;
							
						}
					};
					
					Collections.sort(instMap, alphabeticComp);
					Collections.sort(updtMap, alphabeticComp);
					
					for(Map<String, Object> map:instMap){ map.remove("statusSort"); }
					
					availAdpt = new SimpleAdapter(mctx, availMap, R.layout.listicons, 
							new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat", "down"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating, R.id.dwn});

					availAdpt.setViewBinder(new LstBinder());

					instAdpt = new SimpleAdapter(mctx, instMap, R.layout.listicons, 
							new String[] {	"pkg", 		"name", 	"name2", 		"status", 		"status2", 		"status3", 						"icon", 		"rat"}, 
							new int[] {		R.id.pkg, 	R.id.name, 	R.id.nameup, 	R.id.isinst, 	R.id.isupdt, 	R.id.isDowngradeAvailable, 		R.id.appicon, 	R.id.rating});
  
					instAdpt.setViewBinder(new LstBinder());

					updateAdpt = new SimpleAdapter(mctx, updtMap, R.layout.listicons, 
							new String[] {	"pkg", 		"name", 	"name2", 		"status", 		"status2", 		"status3",					"icon", 		"rat"}, 
							new int[] {		R.id.pkg, 	R.id.name, 	R.id.nameup, 	R.id.isinst, 	R.id.isupdt, 	R.id.isDowngradeAvailable,	R.id.appicon, 	R.id.rating});

					updateAdpt.setViewBinder(new LstBinder());
				
					//Log.d("Aguamarina", e.getMessage()+"");
				}finally{
					
					Log.d("Aguamarina","======================= I REDRAW SAY KILL");
					stop_pd.sendEmptyMessage(0);
					
				}
			}
			
			
			
		}.start();
		 
	}
	
	class LstBinder implements ViewBinder
	{
		public boolean setViewValue(View view, Object data, String textRepresentation)
		{
			if(view.getClass().toString().equalsIgnoreCase("class android.widget.RatingBar")){
				RatingBar tmpr = (RatingBar)view;
				tmpr.setRating(new Float(textRepresentation));
			}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.TextView")){
				TextView tmpr = (TextView)view;
				tmpr.setText(textRepresentation);
			}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.ImageView")){
				ImageView tmpr = (ImageView)view;	
				File icn = new File(textRepresentation);
				if(icn.exists() && icn.length() > 0){
					new Uri.Builder().build();
					tmpr.setImageURI(Uri.parse(textRepresentation));
				}else{
					tmpr.setImageResource(android.R.drawable.sym_def_app_icon);
				}
			}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.LinearLayout")){
				LinearLayout tmpr = (LinearLayout)view;
				tmpr.setTag(textRepresentation);
			}else{
				return false;
			}
			return true;
		}
	}

	class SimpeLstBinder implements ViewBinder
	{
		public boolean setViewValue(View view, Object data, String textRepresentation)
		{
			if(view.getClass().toString().equalsIgnoreCase("class android.widget.TextView")){
				TextView tmpr = (TextView)view;
				tmpr.setText(textRepresentation);
			}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.LinearLayout")){
				LinearLayout tmpr = (LinearLayout)view;
				tmpr.setTag(textRepresentation);
			}else{
				return false;
			}
			return true;
		}

	}

	protected void queueDownload(String packageName, String ver, boolean isUpdate){


		Vector<DownloadNode> tmp_serv = new Vector<DownloadNode>();	

		try{
			
			tmp_serv = db.getPathHash(packageName, ver);

			String localPath = new String(LOCAL_APK_PATH+packageName+".apk");
			String appName = packageName;
			for(ApkNode node: apk_lst){
				if(node.apkid.equals(packageName)){
					appName = node.name;
					break;
				}
			}

			//if(tmp_serv.size() > 0){
			DownloadNode downloadNode = tmp_serv.firstElement();
			downloadNode.setPackageName(packageName);
			downloadNode.setAppName(appName);
			downloadNode.setLocalPath(localPath);
			downloadNode.setUpdate(isUpdate);
			String remotePath = downloadNode.getRemotePath();
			//}

			if(remotePath.length() == 0)
				throw new TimeoutException();

			String[] logins = null; 
			logins = db.getLogin(downloadNode.getRepo());
//			downloadNode.getRemotePath()
			downloadNode.setLogins(logins);
			Log.d("Aguamarina-BaseManagement","queueing download: "+packageName +" "+downloadNode.getSize());	

			downloadQueueService.startDownload(downloadNode);

		} catch(Exception e){	}
	}
	 
	 protected SimpleAdapter getRootCtg(){
		 main_catg_adpt.setViewBinder(new SimpeLstBinder());
		 return main_catg_adpt;
	 }
	 
	 protected SimpleAdapter getAppCtg(){
		 app_catg_adpt.setViewBinder(new SimpeLstBinder());
		 return app_catg_adpt;
	 }
	 
	 protected SimpleAdapter getGamesCtg(){
		 game_catg_adpt.setViewBinder(new SimpeLstBinder());
		 return game_catg_adpt;
	 }
	 
	 protected SimpleAdapter getAvailable(String show_now, int main_show_now){
		 if(sPref.getBoolean("mode", false)){
			 if(!(show_now == null) || main_show_now == 2){
				 return getGivenCatg(show_now, main_show_now);
			 }
        	 main_catg_adpt.setViewBinder(new SimpeLstBinder());
        	 return main_catg_adpt;
         }
		 return availAdpt;
	 }
	 
	 protected SimpleAdapter getGivenCatg(String ctg, int ord){
		 List<Map<String, Object>> availMap = new ArrayList<Map<String, Object>>();
		 Map<String, Object> apk_line;
		 Vector<ApkNode> tmp_lst = null;
		 SimpleAdapter rtnadp = null;

		/* if(tmp_lst != null)
			 tmp_lst.clear();*/
		 tmp_lst = db.getAll(order_lst, ctg, ord);


		 for(ApkNode node: tmp_lst){
			 apk_line = new HashMap<String, Object>();
			 apk_line.put("pkg", node.apkid);
			 String iconpath = new String(getString(R.string.icons_path)+node.apkid);
			 apk_line.put("icon", iconpath);
			 apk_line.put("rat", node.rat);
			 apk_line.put("status", "Version: " + node.ver);
			 apk_line.put("name", node.name);
			 if(node.down >= 0)
				 apk_line.put("down", node.down + " Down.");
			 availMap.add(apk_line);
		 }


		 rtnadp = new SimpleAdapter(mctx, availMap, R.layout.listicons, 
				 new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat", "down"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating, R.id.dwn});

		 rtnadp.setViewBinder(new LstBinder());

		 return rtnadp;
	 }
	 
	 
	 
	 protected Handler stop_pd = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			while(pd.isShowing()){
				Log.d("Aguamarina","======================= I KILL");
				pd.dismiss();
			}
			prefEdit.putBoolean("changeavail", true);
			prefEdit.putBoolean("changeinst", true);
			prefEdit.putBoolean("changeupdt", true);
			prefEdit.putBoolean("redrawis", false);
			prefEdit.commit();
		}
		 
	 };
	 
	 


	@Override
	protected void onPause() {
		super.onPause();
		prefEdit.putString("order_lst", order_lst);
		prefEdit.commit();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}	
	

}
