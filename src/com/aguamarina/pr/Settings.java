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

package cm.aptoide.pt;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Settings extends Activity implements OnCheckedChangeListener, OnClickListener{
	
	private SharedPreferences sPref;
	
	private Intent rtrn = new Intent();
	
	private ProgressDialog pd = null;
	private Context mctx = null;
	
	private Button clear_cache = null;
	
	private RadioGroup grp2 = null;
	private boolean catg = false;
	private boolean mix = false;
	private int ctg_id;
	private int mix_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		mctx = this;
		
		sPref = getSharedPreferences("aptoide_prefs", MODE_PRIVATE);
		
		
		grp2 = (RadioGroup) findViewById(R.id.groupshow);
		grp2.setOnCheckedChangeListener(this);
		
		RadioButton btn1 = (RadioButton) findViewById(R.id.shw_ct);
		RadioButton btn2 = (RadioButton) findViewById(R.id.shw_all);
		
		ctg_id = btn1.getId();
		mix_id = btn2.getId();
		
		if(sPref.getBoolean("mode", false))
			btn1.setChecked(true);
		else
			btn2.setChecked(true);
		
		
		Button btn_ok = (Button) findViewById(R.id.btn_save);
		btn_ok.setOnClickListener(this);
		
		clear_cache = (Button) findViewById(R.id.clear_cache);
		clear_cache.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pd = ProgressDialog.show(mctx, getText(R.string.top_please_wait), getText(R.string.settings_cache), true);
				pd.setIcon(android.R.drawable.ic_dialog_info);
				
				new Thread() {
					public void run() {
						File aptoide_dir = new File(getText(R.string.base_path).toString());
						for(File fl: aptoide_dir.listFiles()){
							if(fl.isFile() && fl.getName().toLowerCase().endsWith("apk")){
								fl.delete();
							}
						}
						File icons_dir = new File(getText(R.string.icons_path).toString());
						for(File fl: icons_dir.listFiles()){
							fl.delete();
						}
						done_handler.sendEmptyMessage(0);
					}
				}.start(); 
			}
		});
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		
		if(group.equals(grp2)){
			catg = false;
			mix = false;
			
			if(checkedId == ctg_id)
				catg = true;
			else if(checkedId == mix_id)
				mix = true;
		}
	}
	

	public void onClick(View v) {
		if(catg == true)
			rtrn.putExtra("mode", true);
		else if(mix == true)
			rtrn.putExtra("mode", false);
		finish();
	}

	@Override
	public void finish() {
		rtrn.putExtra("settings", 0);
		this.setResult(RESULT_OK, rtrn);
		super.finish();
	}

	private Handler done_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(pd.isShowing())
				pd.dismiss();
			clear_cache.setEnabled(false);
		}
	};
	
}
