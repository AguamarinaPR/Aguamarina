package cm.aptoide.pt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
	
	static private Vector<ApkNode> apk_lst = null;
	
	static private boolean pop_change = false;
	
	static private String order_lst = "abc";
	
	static protected SimpleAdapter availAdpt = null;
	static protected SimpleAdapter instAdpt = null;
	static protected SimpleAdapter updateAdpt = null;
	
	protected static final int INSTALL = 127;
	protected static final int REMOVE = 128;
	protected static final int UPDATE = 129;
	private static final String APK_PATH = Environment.getExternalStorageDirectory().getPath()+"/.aptoide/";
	
	
	private static SimpleAdapter main_catg_adpt = null;
	private static SimpleAdapter app_catg_adpt = null;
	private static SimpleAdapter game_catg_adpt = null;
	
	private static final String[] main_ctg = {"Games", "Applications", "Others"};
	private static final String[] app_ctg = {"Comics", "Communication", "Entertainment", "Finance", "Health", "Lifestyle", "Multimedia", 
   		 "News & Weather", "Productivity", "Reference", "Shopping", "Social", "Sports", "Themes", "Tools", 
		 "Travel", "Demo", "Software Libraries", "Other"};
	private static final String[] game_ctg = {"Arcade & Action", "Brain & Puzzle", "Cards & Casino", "Casual", "Other"};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mPm = getPackageManager();
		db = new DbHandler(this);
		mctx = this;
		sPref = getSharedPreferences("aptoide_prefs", MODE_PRIVATE);
		prefEdit = sPref.edit();
		
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
		
		p.setButton("Ok", new DialogInterface.OnClickListener() {
			
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
			if(sPref.contains("order_lst")){
				order_lst = sPref.getString("order_lst", "abc");
				prefEdit.remove("order_lst");
				prefEdit.commit();
			}
			redraw();
			redrawCatgList();
			
		}else if(sPref.contains("order_lst")){
			order_lst = sPref.getString("order_lst", "abc");
			prefEdit.remove("order_lst");
			prefEdit.commit();
			redraw();
		}
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
	
	protected void installApk(String apk_path){
		pkginfo = mPm.getPackageArchiveInfo(apk_path, 0);
		if(pkginfo == null){
			// Ficheiro está corrupto, temos de verificar!
		}else{
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + apk_path), "application/vnd.android.package-archive");
			prefEdit.putString("pkg", pkginfo.packageName);
			prefEdit.commit();
			startActivityForResult(intent,INSTALL);
		}
	}
	
	protected void updateApk(String apk_path, String apk_id){	
		pkginfo = mPm.getPackageArchiveInfo(apk_path, 0);
		Intent intent = new Intent();
    	intent.setAction(android.content.Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.parse("file://" + apk_path), "application/vnd.android.package-archive");
    	prefEdit.putString("pkg", pkginfo.packageName);
    	prefEdit.commit();
    	startActivityForResult(intent,UPDATE);
	}
	
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == INSTALL){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
				db.insertInstalled(apkid);
				prefEdit.remove("pkg");
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
			if(db.wasUpdate(apkid, vercode)){
				db.removeInstalled(apkid);
				db.insertInstalled(apkid);
				prefEdit.remove("pkg");
				prefEdit.commit();
				redrawCatgList();
				redraw();
			}
		}
	}

	protected void redraw(){
		
		prefEdit.putBoolean("redrawis", true);
    	prefEdit.commit();
		
		pd = ProgressDialog.show(mctx, getText(R.string.top_please_wait), getText(R.string.updating_msg), true);
		pd.setIcon(android.R.drawable.ic_dialog_info);
		
		
		new Thread() {

			public void run(){

				List<Map<String, Object>> availMap = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> instMap = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> updtMap = new ArrayList<Map<String, Object>>();
				Map<String, Object> apk_line;

				if(apk_lst != null)
					apk_lst.clear();
				apk_lst = db.getAll(order_lst);


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
						instMap.add(apk_line);
					}else if(node.status == 2){
						apk_line.put("status2", getString(R.string.installed_update) + " " + node.ver);
						apk_line.put("name2", node.name);
						updtMap.add(apk_line);
						instMap.add(apk_line);
					}else{
						apk_line.put("status", "Version: " + node.ver);
						apk_line.put("name", node.name);
						availMap.add(apk_line);
					}
				}

				availAdpt = new SimpleAdapter(mctx, availMap, R.layout.listicons, 
						new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat", "down"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating, R.id.dwn});

				availAdpt.setViewBinder(new LstBinder());

				instAdpt = new SimpleAdapter(mctx, instMap, R.layout.listicons, 
						new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating});

				instAdpt.setViewBinder(new LstBinder());

				updateAdpt = new SimpleAdapter(mctx, updtMap, R.layout.listicons, 
						new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating});

				updateAdpt.setViewBinder(new LstBinder());

				stop_pd.sendEmptyMessage(0);

				
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
	 
	 protected String downloadFile(String apkid){
		 Vector<DownloadNode> tmp_serv = new Vector<DownloadNode>();
		 String getserv = new String();
		 String md5hash = null;
		 String repo = null;
		 String name = null;
		 int size = 0;

		 try{

			 tmp_serv = db.getPathHash(apkid);

			 if(tmp_serv.size() > 0){
				 DownloadNode node = new DownloadNode();
				 node = tmp_serv.firstElement();
				 getserv = node.repo + "/" + node.path;
				 md5hash = node.md5h;
				 repo = node.repo;
				 size = node.size;
			 }

			 
			 if(getserv.length() == 0)
				 throw new TimeoutException();
			 
			 Message msg = new Message();
			 msg.arg1 = 0;
			 msg.arg2 = size;
			 msg.obj = new String(getserv);
			 download_handler.sendMessage(msg);
			 
			 String path = new String(APK_PATH+apkid+".apk");
			 
			 // If file exists, removes it...
			 File f_chk = new File(path);
			 if(f_chk.exists()){
				 f_chk.delete();
			 }
			 f_chk = null;
			 
			 FileOutputStream saveit = new FileOutputStream(path);
			 
			 HttpResponse mHttpResponse = NetworkApis.getHttpResponse(getserv, repo, mctx);
			 
			 if(mHttpResponse == null){
				 Log.d("Aptoide","Problem in network... retry...");	
				 mHttpResponse = NetworkApis.getHttpResponse(getserv, repo, mctx);
				 if(mHttpResponse == null){
					 Log.d("Aptoide","Major network exception... Exiting!");
					 return null;
				 }
			 }
			 
			 if(mHttpResponse.getStatusLine().getStatusCode() == 401){
				 return null;
			 }else{
				 InputStream getit = mHttpResponse.getEntity().getContent();
                 byte data[] = new byte[8096];
				 int readed;
				 readed = getit.read(data, 0, 8096);
				 while(readed != -1) {
					 download_tick.sendEmptyMessage(readed);
					 saveit.write(data,0,readed);
					 readed = getit.read(data, 0, 8096);
				 }
				 Log.d("Aptoide","Download done!");
				 saveit.flush();
				 saveit.close();
				 getit.close();
			 }
			 
			 Log.d("Aptoide","Download MD5...");
			 File f = new File(path);
			 Md5Handler hash = new Md5Handler();
			 if(md5hash == null || md5hash.equalsIgnoreCase(hash.md5Calc(f))){
				 return path;
			 }else{
				 Log.d("Aptoide",md5hash + " VS " + hash.md5Calc(f));
				 return "*md5*";
			 }
		 } catch(Exception e){
			 return null;
		 }
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

		 if(tmp_lst != null)
			 tmp_lst.clear();
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

	 protected Handler download_tick = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//Log.d("Aptoide","Progress: " + pd.getProgress() + " Other: " +  (pd.getMax()*0.96) + " Adding: " + msg.what);
			pd.incrementProgressBy(msg.what);
		}
	 };
	 
	 protected Handler download_handler = new Handler() {
		 @Override
		 public void handleMessage(Message msg) {
			 if(msg.arg1 == 0){
				 //pd = ProgressDialog.show(mctx, "Download", getString(R.string.download_alrt) + msg.obj.toString(), true);
				 pd = new ProgressDialog(mctx);
				 pd.setTitle("Download");
				 pd.setMessage(getString(R.string.download_alrt) + msg.obj.toString());
				 pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				 pd.setCancelable(false);
				 pd.setCanceledOnTouchOutside(false);
				 /*int max = (((msg.arg2*106)/100)*1000);
				 Log.d("Aptoide","Max is: " + max);*/
				 pd.setMax(msg.arg2*1024);
				 pd.setProgress(0);
				 pd.show();
			 }else{
				 pd.dismiss();
			 }
		 }
	 };

	 protected Handler download_error_handler = new Handler() {
		 @Override
		 public void handleMessage(Message msg) {
			 while(pd.isShowing())
				 pd.dismiss();
			
			 if(msg.arg1 == 1){
				 Toast.makeText(mctx, getString(R.string.network_error), Toast.LENGTH_LONG).show();
			 }else{
	        	Toast.makeText(mctx, getString(R.string.md5_error), Toast.LENGTH_LONG).show();
			 }
		 }
	 };
	 
	 protected Handler stop_pd = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			while(pd.isShowing()){
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
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
		}	
}
