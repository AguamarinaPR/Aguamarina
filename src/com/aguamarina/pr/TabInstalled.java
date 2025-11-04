/* Copyright (C) 2023-2025 TropicalBananas */

package com.aguamarina.pr;

import java.util.ArrayList;
import java.util.Vector;

import com.aguamarina.pr.multiversion.VersionApk;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class TabInstalled extends BaseManagement implements OnItemClickListener{
	
	private ListView lv = null;
	
    private InstallApkListener installApkListener = null;
//    private Boolean installApkListenerIsRegistered = false;

	
	private DbHandler db = null;
	//private Context mctx = null;
	
	private int pos = -1;
	

	protected class InstallApkListener extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Aguamarina-TabInstalled", "Tab Installed, broadcast received.");
			if (intent.getAction().equals("pr.fleurdelis.aguamarina.INSTALL_APK_ACTION")) {
				installApk(intent.getStringExtra("localPath"), intent.getStringExtra("version"));
			}
		}
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lv = new ListView(this);
		lv.setOnItemClickListener(this);
		db = new DbHandler(this);
		
		installApkListener = new InstallApkListener();
		registerReceiver(installApkListener, new IntentFilter("pr.fleurdelis.aguamarina.INSTALL_APK_ACTION"));
		//mctx = this;
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(Menu.NONE, EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(), EnumOptionsMenu.DISPLAY_OPTIONS.ordinal(), R.string.menu_order)
//			.setIcon(android.R.drawable.ic_menu_sort_by_size);
//		return super.onCreateOptionsMenu(menu);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		EnumOptionsMenu menuEntry = EnumOptionsMenu.reverseOrdinal(item.getItemId());
//		Log.d("Aguamarina-OptionsMenu", "menuOption: "+menuEntry+" itemid: "+item.getItemId());
//		switch (menuEntry) {
//		case DISPLAY_OPTIONS:
//			/*if(resumeMe()){
//				lv.setAdapter(instAdpt);
//				setContentView(lv);
//				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//				lv.setSelection(pos-1);
//			}*/
//			
//			final AlertDialog p = resumeMe();
//			p.show();
//			
//			new Thread(){
//				@Override
//				public void run() {
//					super.run();
//					while(p.isShowing()){
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {	}
//					}
//					displayRefresh.sendEmptyMessage(0);
//				}
//			}.start();
//				
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
//	@Override
//	protected void onPause() {
//		if (!installApkListenerIsRegistered) {
//            unregisterReceiver(installApkListener);
//            installApkListenerIsRegistered = false;
//        }
//		Log.d("Aguamarina-TabInstalled", "installApkListenerIsUnregistered");
//		
//		super.onPause();
//	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Aguamarina-TabInstalled", "onResume");
		
//		if (!installApkListenerIsRegistered) {
//            registerReceiver(installApkListener, new IntentFilter("pr.fleurdelis.aguamarina.INSTALL_APK_ACTION"));
//            installApkListenerIsRegistered = true;
//        }
//		Log.d("Aguamarina-TabInstalled", "installApkListenerIsRegistered");

		
		new Thread(){
			@Override
			public void run() {
				super.run();
				while(sPref.getBoolean("redrawis", false)){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) { }
				}
				displayRefresh2.sendEmptyMessage(0);
			}
			
		}.start();	
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final String pkg_id = ((LinearLayout)arg1).getTag().toString();

		pos = arg2;

		Intent apkinfo = new Intent(this,ApkInfo.class);
		apkinfo.putExtra("name", db.getName(pkg_id));
		apkinfo.putExtra("icon", this.getString(R.string.icons_path)+pkg_id);
		apkinfo.putExtra("apk_id", pkg_id);
		
		String tmpi = db.getDescript(pkg_id);
		if(!(tmpi == null)){
			apkinfo.putExtra("about", tmpi);
		}else{
			apkinfo.putExtra("about",getText(R.string.app_pop_up_no_info));
		}
		

		Vector<String> tmp_get = db.getApk(pkg_id);
		apkinfo.putExtra("server", tmp_get.firstElement());
//		apkinfo.putExtra("rat", tmp_get.get(5));
		apkinfo.putExtra("type", 1);
		
		ArrayList<VersionApk> versions = db.getOldApks(pkg_id);
		VersionApk versionApkPassed = new VersionApk(tmp_get.get(1).substring(1,tmp_get.get(1).length()-1),Integer.parseInt(tmp_get.get(5)),pkg_id, Integer.parseInt(tmp_get.get(4)), Integer.parseInt(tmp_get.get(6)));
		versions.add(versionApkPassed);
		
		
		
		try {
			PackageManager mPm = getApplicationContext().getPackageManager();
			PackageInfo pkginfo = mPm.getPackageInfo(pkg_id, 0);
			VersionApk versionInstApk = new VersionApk(pkginfo.versionName,pkginfo.versionCode,pkg_id,-1,-1);
			apkinfo.putExtra("instversion", versionInstApk);
//			Iterator<VersionApk> iteratorVersion = versions.iterator();
//			while(iteratorVersion.hasNext()){
//				if(iteratorVersion.next().compareTo(versionInstApk)>0){
//					iteratorVersion.remove();
//				}
//			}
		} catch (NameNotFoundException e) {
			//Not installed... do nothing, not going to happen hear
		}
		
		
		
		apkinfo.putParcelableArrayListExtra("versions", versions);
		startActivityForResult(apkinfo,30);
		
		/*
		Vector<String> tmp_get = db.getApk(pkg_id);
		String tmp_path = this.getString(R.string.icons_path)+pkg_id;
		File test_icon = new File(tmp_path);

		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.alertscroll, null);
		Builder alrt = new AlertDialog.Builder(this).setView(view);
		final AlertDialog p = alrt.create();
		if(test_icon.exists() && test_icon.length() > 0){
			p.setIcon(new BitmapDrawable(tmp_path));
		}else{
			p.setIcon(R.drawable.loadingicon);
		}
		p.setTitle(db.getName(pkg_id));
		TextView t1 = (TextView) view.findViewById(R.id.n11);
		t1.setText(tmp_get.firstElement());
		TextView t2 = (TextView) view.findViewById(R.id.n22);
		t2.setText(tmp_get.get(1));
		TextView t3 = (TextView) view.findViewById(R.id.n33);
		t3.setText(tmp_get.get(2));
		TextView t4 = (TextView) view.findViewById(R.id.n44);
		t4.setText(tmp_get.get(3));
		TextView t5 = (TextView) view.findViewById(R.id.n55);
		String tmpi = db.getDescript(pkg_id);
		if(!(tmpi == null)){
			t5.setText(tmpi);
		}else{
			t5.setText(getText(R.string.app_pop_up_no_info));
		}

		TextView t6 = (TextView) view.findViewById(R.id.down_n);
		t6.setText(tmp_get.get(4));

		p.setButton2(getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			} });

		p.setButton(getString(R.string.rem), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				removeApk(pkg_id);
			} });
		p.setButton3(getText(R.string.btn_search_market), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				p.dismiss();					
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id="+pkg_id));
				try{
					startActivity(intent);
				}catch (ActivityNotFoundException e){
					Toast.makeText(mctx, getText(R.string.error_no_market), Toast.LENGTH_LONG).show();
				}
			} });

		p.show();
		*/
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 30 && data != null && data.hasExtra("apkid")){
			if(data.getBooleanExtra("rm", false)){
				new Thread() {
					public void run() {
						String apk_id = data.getStringExtra("apkid");
						Log.d("Aguamarina", ".... removing: " + apk_id);
						removeApk(apk_id);
					}
				}.start();
			} else if(data.getBooleanExtra("install", false) && data.hasExtra("version")){
					String apk_id = data.getStringExtra("apkid");
					Log.d("Aguamarina", "....... getting: " + apk_id);
					queueDownload(apk_id, data.getStringExtra("version"), true);
			}
		}
	}



	protected Handler displayRefresh = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			redraw();
			lv.setAdapter(instAdpt);
			setContentView(lv);
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv.setSelection(pos-1);
		}
		 
	 };
	
	
	protected Handler displayRefresh2 = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(sPref.getBoolean("changeinst", false)){
				lv.setAdapter(instAdpt);
				setContentView(lv);
				lv.setSelection(pos-1);
				prefEdit.remove("changeinst");
				prefEdit.commit();
			}
		}
		
	};
	 
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}	
}
