/* Copyright (C) 2023-2025 TropicalBananas */

package com.aguamarina.pr;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.aguamarina.pr.multiversion.MultiversionSpinnerAdapter;
import com.aguamarina.pr.multiversion.VersionApk;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ApkInfo extends Activity{

	//private final static String WS_img = "http://www.bazaarandroid.com/webservices/listApkScreens/";
	
	private Intent apkinfo = null;
	private Context mctx = null;
	
	private Intent rtrn_intent = null;
	
	private boolean jback = false;
	
	/*private Integer[] imageIDs = {
			R.drawable.no_screen
	};*/
	
	private Drawable[] imageDrwb = null;
	
	//private Gallery galry = null;
	
	private String apk_name_str = null;
	
	private TextView noscreens = null;

	
	List<ImageView> screens = null;
	
	private Spinner spinnerMulti;
	private String apk_repo_str;
	private String apk_id;
	
	//private String apk_repo_str_raw;
	private String apk_ver_str_raw;
	private VersionApk versionInstApk;
	ArrayList<VersionApk> versions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		apkinfo = getIntent();
		setContentView(R.layout.apkinfo);
		
		versions = apkinfo.getParcelableArrayListExtra("versions");
		Collections.sort(versions, Collections.reverseOrder());
		
		mctx = this;
		screens = new ArrayList<ImageView>();
		screens.add((ImageView) findViewById(R.id.shot1));
		screens.add((ImageView) findViewById(R.id.shot2));
		screens.add((ImageView) findViewById(R.id.shot3));
		screens.add((ImageView) findViewById(R.id.shot4));
		screens.add((ImageView) findViewById(R.id.shot5));
		
		for(int i = 0; i < screens.size(); i++){
			screens.get(i).setOnClickListener(new View.OnClickListener() {
		    	public void onClick(View v) {
		        	screenshotClick(v);
		    	}
			});
		}

		noscreens = (TextView)findViewById(R.id.noscreens);
		
		rtrn_intent = new Intent();
		
		
		
		apk_id = apkinfo.getStringExtra("apk_id");
		final int type = apkinfo.getIntExtra("type", 0);
		
		
		versionInstApk = (VersionApk) apkinfo.getParcelableExtra("instversion");
		String icon_path = apkinfo.getStringExtra("icon");
		apk_name_str = apkinfo.getStringExtra("name");
		String apk_descr = apkinfo.getStringExtra("about");
		apk_repo_str = apkinfo.getStringExtra("server");
		String apk_rat_str = apkinfo.getStringExtra("rat");
		
		// TODO: Add upcoming feature here and in res/layout/apkinfo.xml
		/*Button serch_mrkt = (Button)findViewById(R.id.btn_market);
		serch_mrkt.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id="+apk_id));
				try{
					startActivity(intent);
				}catch (ActivityNotFoundException e){
					Toast.makeText(mctx, getText(R.string.error_no_market), Toast.LENGTH_LONG).show();
				}
			}
		});*/
		final Button action = ((Button)findViewById(R.id.btn1));
//		switch (type) {
//		case 0:
//			action.setText(getString(R.string.install));
//			break;
//
//		case 1:
//			action.setText(getString(R.string.rem));
//			break;
//			
//		case 2:
//			action.setText(getString(R.string.update));
//			break;
//		}
		final Button uninstall = ((Button)findViewById(R.id.btnUninstall));
		
		spinnerMulti = ((Spinner)findViewById(R.id.spinnerMultiVersion));
		
		action.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				int pos = apkinfo.getIntExtra("position", -1);
				
				switch (type) {
				case 0:
					
					rtrn_intent.putExtra("apkid", apk_id);
					rtrn_intent.putExtra("in", true);
					rtrn_intent.putExtra("position", pos);
					jback = true;
					break;

				case 1:
					rtrn_intent.putExtra("apkid", apk_id);
					
					rtrn_intent.putExtra("rm", false);
					rtrn_intent.putExtra("install", true);
					rtrn_intent.putExtra("in", true);
					
					rtrn_intent.putExtra("position", pos);
					
					jback = true;
					break;

				case 2:
					rtrn_intent.putExtra("apkid", apk_id);
					jback = true;
					break;
				}
				
				
				rtrn_intent.putExtra("version", ((VersionApk)spinnerMulti.getSelectedItem()).getVersion());
				
				
				finish();
				
			}
		});
		
		ImageView icon = (ImageView) findViewById(R.id.appicon);
		File test_icon = new File(icon_path);
		
		
		if(test_icon.exists() && test_icon.length() > 0){
			icon.setImageDrawable(new BitmapDrawable(icon_path));
		}else{
			icon.setImageResource(R.drawable.loadingicon);
		}
		
		TextView apk_name = (TextView)findViewById(R.id.app_name);
		apk_name.setText(apk_name_str);
		
		TextView apk_about = (TextView)findViewById(R.id.descript);
		String desc_parsed = Html.fromHtml(apk_descr).toString();
		apk_about.setText(desc_parsed);
		
		final TextView apk_repo = (TextView)findViewById(R.id.app_repo);
		apk_repo.setText(apk_repo_str);
		
		TextView apk_version = (TextView)findViewById(R.id.app_ver);
		
		if(type == 1 || type == 2){
			apk_version.setText(this.getString(R.string.version_inst)+": " + ( versionInstApk!=null?versionInstApk.getVersion(): "Not available" ) );
		}else{
			apk_version.setVisibility(View.INVISIBLE);
		}
		
		
		RatingBar apk_rat_n = (RatingBar) findViewById(R.id.rating);
		apk_rat_n.setRating(new Float(apk_rat_str));
		
		if(versions.size()!=0){
			apk_ver_str_raw = versions.get(0).getVersion(); 
		}else{
			apk_ver_str_raw = null;
		}

		/*new Thread(){
			public void run(){
				try{
					String ws_repo = apk_repo_str.substring(7).split("[\\.]")[0];
					String fetch_imgs = WS_img+ws_repo+"/"+apk_id+"/"+apk_ver_str.trim()+"/json";

					Log.d("Aguamarina",apk_repo_str + " vs " + ws_repo);
					Log.d("Aguamarina","Get img from: " + fetch_imgs);
					
					HttpResponse response_ws = NetworkApis.imgWsGet(fetch_imgs);
					if(response_ws != null && response_ws.getStatusLine().getStatusCode() == 200){
						String json_str = null;
						json_str = EntityUtils.toString(response_ws.getEntity());
						response_ws.getEntity().consumeContent();
						Log.d("Aguamarina","Resp: " + json_str);
						JSONObject json_resp = new JSONObject(json_str);
						
						JSONArray img_url = json_resp.getJSONArray("listing");
						if(img_url.length()>0)
							imageDrwb = new Drawable[img_url.length()];
						for(int i = 0; i< img_url.length(); i++){
							String a = (String)img_url.get(i);
							Log.d("Aguamarina","* " + a);
							HttpResponse pic = NetworkApis.imgWsGet(a);
							InputStream pic_st = pic.getEntity().getContent();
							Drawable pic_drw = Drawable.createFromStream(pic_st, "src");
							imageDrwb[i] = pic_drw;
						}
					}
					
				}catch (Exception e ){ }
				finally{
					updateScreenshots.sendEmptyMessage(0);
				}
			}
		}.start();*/
		// TODO: Reimplement screenshots
		
		final MultiversionSpinnerAdapter<VersionApk> spinnerMultiAdapter = new MultiversionSpinnerAdapter<VersionApk>(this, R.layout.textviewfocused, versions);
		spinnerMultiAdapter.setDropDownViewResource(R.layout.multiversionspinneritem);
		spinnerMulti.setAdapter(spinnerMultiAdapter);
		if(type==2){
			//Tab updates
			spinnerMulti.setOnItemSelectedListener(new OnItemSelectedListener(){
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						VersionApk versionApk = ((VersionApk)spinnerMultiAdapter.getItem(position));
						apk_ver_str_raw = versionApk.getVersion();
						/*if(Configs.TASTE_ON){
							selectTaste(apk_repo_str_raw , apk_id, apk_ver_str_raw, likes, dislikes, like, dislike, userTaste);
						}
						if(Configs.COMMENTS_ON && loadOnScrollCommentList!=null){
							new Thread(newVersionFetchComments).start();
						}
						
						if(previousGetter!=null)
							previousGetter.cancel();
						resetScreenshots.sendEmptyMessage(0);*/
						
						((TextView)ApkInfo.this.findViewById(R.id.versionInfo)).setText(MultiversionSpinnerAdapter.formatInfo((VersionApk)spinnerMulti.getSelectedItem()));
						
						//previousGetter = new GetScreenShots(apk_ver_str_raw);
						//previousGetter.start();
						
				}
				public void onNothingSelected(AdapterView<?> parent) {}
			});
			
		} else if(type==1){
			
			uninstall.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, uninstall.getLayoutParams().height, 0.5f));
			uninstall.setOnClickListener(new OnClickListener(){

					public void onClick(View arg0) {
						
						rtrn_intent.putExtra("apkid", apk_id);
						
						rtrn_intent.putExtra("rm", true);
						rtrn_intent.putExtra("install", false);
						
						rtrn_intent.putExtra("position", apkinfo.getIntExtra("position", -1));
						
						jback = true;
						finish();
						
					}
					
				});
			
			if(versions.size()!=0){
				
				spinnerMulti.setOnItemSelectedListener(new OnItemSelectedListener(){
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						
							VersionApk versionApk = ((VersionApk)spinnerMultiAdapter.getItem(position));
							apk_ver_str_raw = versionApk.getVersion();
							int result = versionApk.compareTo(versionInstApk);
							
							/*if(Configs.TASTE_ON){
								selectTaste(apk_repo_str_raw , apk_id, apk_ver_str_raw, likes, dislikes, like, dislike, userTaste);
							}
							if(Configs.COMMENTS_ON && loadOnScrollCommentList!=null){
								new Thread(newVersionFetchComments).start();
							}*/
							
							if(result==0){
								action.setText("Reinstall");
							}else if(result<0) {
								action.setText(R.string.downgrade);
							}else if(result>0) {
								action.setText(R.string.update);
							}
							
							/*if(previousGetter!=null){
								previousGetter.cancel();
							}
							resetScreenshots.sendEmptyMessage(0);*/
							
							((TextView)ApkInfo.this.findViewById(R.id.versionInfo)).setText(MultiversionSpinnerAdapter.formatInfo((VersionApk)spinnerMulti.getSelectedItem()));
							
							//previousGetter = new GetScreenShots(apk_ver_str_raw);
							//previousGetter.start();
							
					}
					public void onNothingSelected(AdapterView<?> parent) {}
				});
				
			}else{
				//Otherwise
				spinnerMulti.setVisibility(View.GONE);
				action.setVisibility(View.GONE);
				//((Button)findViewById(R.id.btn1)).setText(type);
			}
			
		} else if(type==0){
			
			//If we are in tab available
			uninstall.setVisibility(View.GONE);
			spinnerMulti.setOnItemSelectedListener(new OnItemSelectedListener(){
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					apk_ver_str_raw = ((VersionApk)spinnerMultiAdapter.getItem(position)).getVersion();
					/*if(Configs.TASTE_ON){
						selectTaste(apk_repo_str_raw , apk_id, apk_ver_str_raw, likes, dislikes, like, dislike, userTaste);
					}
					if(Configs.COMMENTS_ON && loadOnScrollCommentList!=null){
						new Thread(newVersionFetchComments).start();
					}
					
					if(previousGetter!=null)
						previousGetter.cancel();
					resetScreenshots.sendEmptyMessage(0);*/
					
					((TextView)ApkInfo.this.findViewById(R.id.versionInfo)).setText(MultiversionSpinnerAdapter.formatInfo((VersionApk)spinnerMulti.getSelectedItem()));
					
					//previousGetter = new GetScreenShots(apk_ver_str_raw);
					//previousGetter.start();
				}
				public void onNothingSelected(AdapterView<?> parent) {}
			});
		}
	}

	/*Multiversion*/
	/*final Runnable newVersionFetchComments = new Runnable(){
		public void run() {
			loadOnScrollCommentList.fetchNewApp(apk_repo_str_raw, apk_id, apk_ver_str_raw);
		}
	};*/

	public void screenshotClick(View v){
		//Log.d("Aguamarina","This view.....");
		final Dialog dialog = new Dialog(mctx);

		dialog.setContentView(R.layout.screenshoot);
		dialog.setTitle(apk_name_str);

		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		ImageView fetch = (ImageView) v;
		image.setImageDrawable(fetch.getDrawable());
		image.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.setCanceledOnTouchOutside(true);
		
		dialog.show();
		
	}
	
	private Handler updateScreenshots = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			ProgressBar pd = (ProgressBar) findViewById(R.id.pscreens);
			pd.setVisibility(View.GONE);
			int i = 0;
			if(imageDrwb != null){
				noscreens.setVisibility(View.GONE);
				for (Drawable pic : imageDrwb) {
					screens.get(i).setImageDrawable(pic);
					i++;
					if(i>=5)
						break;
				}
			}else{
				noscreens.setVisibility(View.VISIBLE);
				noscreens.setText("No screenshots available.");
			}
			//galry.setAdapter(new GalAdpt(mctx));
		}		
	};
	
	@Override
	public void finish() {
		if(jback)
			this.setResult(RESULT_OK, rtrn_intent);
		super.finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
