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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import com.aguamarina.pr.multiversion.VersionApk;
import com.aguamarina.pr.utils.EnumOptionsMenu;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

public class RemoteInSearch extends ListActivity{
	
	private ListView lv = null;
	private int pos = -1;
	
	private static final String LOCAL_APK_PATH = Environment.getExternalStorageDirectory().getPath()+"/.aguamarina/";
	
	static protected SharedPreferences sPref;
	static protected SharedPreferences.Editor prefEdit;
	
	private DbHandler db = null;
	private Vector<ApkNode> apk_lst = null;
	
	private PackageManager mPm;
	private PackageInfo pkginfo;

//	private static final int SETTINGS_FLAG = 0;
	
	private Context mctx = this; 

	private String query;
	
	private String order_lst = "abc";
	
	private DownloadQueueService downloadQueueService;
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        downloadQueueService = ((DownloadQueueService.DownloadQueueBinder)serviceBinder).getService();

	        Log.d("Aguamarina-RemoteInSearch", "DownloadQueueService bound to RemoteInSearch");
	    }
	    
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        downloadQueueService = null;
	        
	        Log.d("Aguamarina-RemoteInSearch","DownloadQueueService unbound from RemoteInSearch");
	    }

	};
	
	
	class LstBinder implements ViewBinder
	{

		public boolean setViewValue(View view, Object data, String textRepresentation)
		{
//			if(view.getClass().toString().equalsIgnoreCase("class android.widget.RatingBar")){
//				RatingBar tmpr = (RatingBar)view;
//				tmpr.setRating(new Float(textRepresentation));
			if(view.getClass().toString().equalsIgnoreCase("class android.widget.TextView")){
				TextView tmpr = (TextView)view;
				tmpr.setText(textRepresentation);
			}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.ImageView")){
				ImageView tmpr = (ImageView)view;	
				File icn = new File(textRepresentation);
             	if(icn.exists() && icn.length() > 0){
             		new Uri.Builder().build();
    				tmpr.setImageURI(Uri.parse(textRepresentation));
             	}else{
             		tmpr.setImageResource(R.drawable.loadingicon);
             	}
			}else{
				return false;
			}

			return true;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list);
				
		db = new DbHandler(this);
		
		mPm = getPackageManager();
		
		sPref = getSharedPreferences("aguamarina_prefs", MODE_PRIVATE);
		prefEdit = sPref.edit();
		
		lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		Intent i = getIntent();
		query = i.getStringExtra(SearchManager.QUERY);
		
//@dsilveira #529 search doens't handle hiphens well		
		query = query.replaceAll("[\\%27]|[\\']|[\\-]{2}|[\\%23]|[#]", " ");
		
		apk_lst = db.getSearch(query,order_lst);
		
		bindService(new Intent(getApplicationContext(), DownloadQueueService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
		
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		query = intent.getStringExtra(SearchManager.QUERY);

//@dsilveira #529 search doens't handle hiphens well	
		query = query.replaceAll("[\\%27]|[\\']|[\\-]{2}|[\\%23]|[#]", " ");
		
		apk_lst = db.getSearch(query,order_lst);
	}

	@Override
	protected void onResume() {
		super.onResume();
		redraw();
		lv.setSelection(pos-1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, EnumOptionsMenu.MANAGE_REPO.ordinal(), EnumOptionsMenu.MANAGE_REPO.ordinal(), R.string.menu_manage)
			.setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(Menu.NONE, EnumOptionsMenu.CHANGE_ORDER.ordinal(), EnumOptionsMenu.CHANGE_ORDER.ordinal(), R.string.menu_order)
		.setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(Menu.NONE, EnumOptionsMenu.SEARCH_MENU.ordinal(), EnumOptionsMenu.SEARCH_MENU.ordinal(), R.string.menu_search)
			.setIcon(android.R.drawable.ic_menu_search);
//		menu.add(Menu.NONE, EnumOptionsMenu.SETTINGS.ordinal(), EnumOptionsMenu.SETTINGS.ordinal(), R.string.menu_settings)
//			.setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, EnumOptionsMenu.ABOUT.ordinal(), EnumOptionsMenu.ABOUT.ordinal(), R.string.menu_about)
			.setIcon(android.R.drawable.ic_menu_help);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		EnumOptionsMenu menuEntry = EnumOptionsMenu.reverseOrdinal(item.getItemId());
		Log.d("Aguamarina-OptionsMenu", "menuOption: "+menuEntry+" itemid: "+item.getItemId());
		switch (menuEntry) {
		case MANAGE_REPO:
			Intent i = new Intent(this, ManageRepo.class);
			startActivity(i);
			return true;
		case SEARCH_MENU:
			onSearchRequested();
			return true;
		case ABOUT:
			LayoutInflater li = LayoutInflater.from(this);
			View view = li.inflate(R.layout.about, null);
			TextView info = (TextView)view.findViewById(R.id.about11);
			info.setText(mctx.getString(R.string.about_txt11, mctx.getString(R.string.ver_str)));
			Builder p = new AlertDialog.Builder(this).setView(view);
			final AlertDialog alrt = p.create();
			alrt.setIcon(R.drawable.icon);
			alrt.setTitle(getText(R.string.app_name));
			alrt.setButton(getText(R.string.btn_chlog), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int	whichButton) {
					Uri uri = Uri.parse(getString(R.string.change_log_url));
					startActivity(new Intent( Intent.ACTION_VIEW, uri));
				}
			});
			alrt.show();
			return true;
//		case SETTINGS:
//			Intent s = new Intent(mctx, Settings.class);
//			s.putExtra("order", order_lst);
//			startActivityForResult(s,SETTINGS_FLAG);
//			return true;
		case CHANGE_ORDER:
			if(order_lst.equalsIgnoreCase("abc"))
				order_lst = "iu";
			else if(order_lst.equalsIgnoreCase("iu"))
				order_lst = "recent";
			else
				order_lst = "abc";
			redraw();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		pos = position;
		
		String apkid = apk_lst.get(position).apkid;
		String name = apk_lst.get(position).name;
		Vector<String> tmp_get = db.getApk(apkid);

		String tmp_path = this.getString(R.string.icons_path)+apkid;
		
		Intent apkinfo = new Intent(mctx, ApkInfo.class);
		apkinfo.putExtra("position", position);
		apkinfo.putExtra("icon", tmp_path);
		apkinfo.putExtra("apk_id", apkid);
		apkinfo.putExtra("name", name);
		
		String tmpi = db.getDescript(apkid);
		if(!(tmpi == null)){
			apkinfo.putExtra("about",tmpi);
		}else{
			apkinfo.putExtra("about",getText(R.string.app_pop_up_no_info));
		}
		
		apkinfo.putExtra("server", tmp_get.firstElement());
//		apkinfo.putExtra("rat", tmp_get.get(5));
		apkinfo.putExtra("type", apk_lst.get(position).status == 0?0:1);
		// TODO: Problem is around here and others, fix here
		ArrayList<VersionApk> versions = db.getOldApks(apkid);
		VersionApk versionApkPassed = new VersionApk(tmp_get.get(1).substring(1,tmp_get.get(1).length()-1),Integer.parseInt(tmp_get.get(5)),apkid, Integer.parseInt(tmp_get.get(4)), Integer.parseInt(tmp_get.get(6)));
		versions.add(versionApkPassed);
		
		try {
			PackageManager mPm = getApplicationContext().getPackageManager();
			PackageInfo pkginfo = mPm.getPackageInfo(apkid, 0);
			apkinfo.putExtra("instversion", new VersionApk(pkginfo.versionName,pkginfo.versionCode,apkid,-1,-1));
		} catch (NameNotFoundException e) {
			//Not installed... do nothing
		}
		
		apkinfo.putParcelableArrayListExtra("versions", versions);
		
		startActivityForResult(apkinfo,30);
		
	}
	
	private void removeApk(String apk_pkg, int position){
		try {
			pkginfo = mPm.getPackageInfo(apk_pkg, 0);
		} catch (NameNotFoundException e) {	}
		Uri uri = Uri.fromParts("package", pkginfo.packageName, null);
	    Intent intent = new Intent(Intent.ACTION_DELETE, uri);
	    startActivityForResult(intent,position); 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("Aguamarina", "Position is: " + (pos-1));
		if(requestCode == 30){
			if(data != null){
				String apkid = data.getStringExtra("apkid");
				int pos = data.getIntExtra("position", -1);
				if(pos > -1){
					if(data.getBooleanExtra("in", false)){
						Log.d("Aguamarina","This: " + apkid + " - " + pos + " - Install");
						queueDownload(pos, data.getStringExtra("version"));
						
					}else if(data.getBooleanExtra("rm", false)){
						Log.d("Aguamarina","This: " + apkid + " - " + pos + " - Remove");
						removeApk(apkid, pos);
					}
				}
			}
		}else{

			mPm = getPackageManager();
			if(data != null && data.hasExtra("settings")){
				if(data.hasExtra("align"))
					order_lst = data.getStringExtra("align");
				redraw();
				lv.setSelection(pos-1);
			}else{
				List<PackageInfo> getapks = mPm.getInstalledPackages(0);
				for(PackageInfo node: getapks){
					if(node.packageName.equalsIgnoreCase(pkginfo.packageName)){
						db.insertInstalled(apk_lst.get(requestCode).apkid, apk_lst.get(requestCode).ver);
						prefEdit.putBoolean("search_updt", true);
						prefEdit.commit();
						redraw();
						lv.setSelection(pos-1);
						return;
					}
				}
				db.removeInstalled(apk_lst.get(requestCode).apkid);
				prefEdit.putBoolean("search_updt", true);
				prefEdit.commit();
				redraw();
				lv.setSelection(pos-1);
			}
		}
	}


	/*
	 * Retira a lista da base de dados e apresenta-a
	 */
	private void redraw(){

		apk_lst = db.getSearch(query,order_lst);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> apk_line;
        
        for(ApkNode node: apk_lst){
        	apk_line = new HashMap<String, Object>();
        	if(node.status == 0){
        		apk_line.put("status", getString(R.string.not_inst));
        		apk_line.put("name", node.name);
        	}else if(node.status == 1){
        		apk_line.put("status", getString(R.string.installed) + " " + node.ver);
        		apk_line.put("name", node.name);
        	}else{
        		apk_line.put("status2", getString(R.string.installed_update) + " " + node.ver);
        		apk_line.put("name2", node.name);
        	}
        	String iconpath = new String(this.getString(R.string.icons_path)+node.apkid);
        	apk_line.put("icon", iconpath);
//         	apk_line.put("rat", node.rat);
        	result.add(apk_line);
        }
        SimpleAdapter show_out = new SimpleAdapter(this, result, R.layout.listicons, 
        		new String[] {"name", "name2", "status", "status2", "icon"}, new int[] {R.id.name, R.id.nameup,  R.id.isinst, R.id.isupdt, R.id.appicon});
        show_out.setViewBinder(new RemoteInSearch.LstBinder());
        
        setListAdapter(show_out);

	}

	private void queueDownload(int position, String version){
		
		Vector<DownloadNode> tmp_serv = new Vector<DownloadNode>();	
		
		try{
		
			String packageName = apk_lst.get(position).apkid;
			String appName = apk_lst.get(position).name;
			
			tmp_serv = db.getPathHash(packageName, version);
			
			String localPath = new String(LOCAL_APK_PATH+apk_lst.get(position).apkid+".apk");
			
			//if(tmp_serv.size() > 0){
			DownloadNode downloadNode = tmp_serv.firstElement();
			downloadNode.setPackageName(packageName);
			downloadNode.setAppName(appName);
			downloadNode.setLocalPath(localPath);
			String remotePath = downloadNode.getRemotePath();
			
			//}
	
			if(remotePath.length() == 0)
				throw new TimeoutException();
			
//			String[] logins = null; 
//			logins = db.getLogin(downloadNode.getRepo());
					
			downloadQueueService.startDownload(downloadNode);
	
		} catch(Exception e){	}
	}

	@Override
	protected void onDestroy() {
		if(downloadQueueService != null){
			unbindService(serviceConnection);	
		}
		super.onDestroy();
	}
	
}
