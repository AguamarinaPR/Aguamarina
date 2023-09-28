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
import java.io.FileOutputStream;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class RssHandler extends DefaultHandler{
	
	private ApkNodeFull tmp_apk = new ApkNodeFull();
	
	private String icon_path;
	
	SharedPreferences sPref = null;
	
	Context mctx;
	String mserver;
	
	private int napk = 0;
	
	private int readed = 0;
	
	private boolean new_apk = false;
	private boolean apk_name = false;
	private boolean apk_path = false;
	private boolean apk_ver = false;
	private boolean apk_vercode = false;
	private boolean apk_id = false;
	private boolean apk_icon = false;
	private boolean apk_date = false;
	private boolean apk_rat = false;
	private boolean apk_md5hash = false;
	private boolean apk_down = false;
	private boolean apk_ctg = false;
	private boolean apk_ctg2 = false;
	private boolean apk_size = false;
	
	private DbHandler db = null;
	
	private DefaultHttpClient mHttpClient = null;
	//private DefaultHttpClient mHttpClient2 = null;

	private Vector<ApkNode> listapks= null;
	
	private Vector<IconNode> iconFetchList = new Vector<IconNode>();
	private Vector<IconNode> iconFinalFetchList = new Vector<IconNode>();
	
	private boolean iconsInPool = true;
	
	//private boolean requireLogin = false;
	private String usern = null;
	private String passwd = null;
	
	private Handler pd_set = null;
	private Handler pd_tick = null;
	private Handler extras_hd = null;
	
	private boolean isDelta = false;
	private boolean onDelta = false;
	private String thisDelta = "";
	private boolean isRemove = false;
	private boolean hasIcon = false;
	private boolean apkcount = false;
	private int apks_n = -1;
	
	private boolean is_last = false;
		
	public RssHandler(Context ctx, String srv, Handler pd_set, Handler pd_tick, Handler extras_hd, boolean is_last){
		mctx = ctx;
		mserver = srv;
		db = new DbHandler(mctx);
		//listapks = db.getForUpdate();
		tmp_apk.apkid = "";
		tmp_apk.name = "unknown";
		tmp_apk.ver = "0.0";
		tmp_apk.vercode = 0;
		tmp_apk.rat = 0.0f;
		tmp_apk.down = -1;
		tmp_apk.date = "2000-01-01";
		tmp_apk.md5hash = "";
		tmp_apk.catg="";
		tmp_apk.catg_type = 2;
		tmp_apk.path="";
		icon_path = "";
		tmp_apk.size = 0;
		
		
		this.pd_set = pd_set;
		this.pd_tick = pd_tick;
		
		this.extras_hd = extras_hd;
		
		this.is_last = is_last;
		
		sPref = mctx.getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
		
	}
	
	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		super.characters(ch, start, length);
		if(apk_name){
			tmp_apk.name = new String(ch).substring(start, start + length);
		}else if(apk_id){
			tmp_apk.apkid = tmp_apk.apkid.concat(new String(ch).substring(start, start + length));
		}else if(apk_path){
			tmp_apk.path = tmp_apk.path.concat(new String(ch).substring(start, start + length));
		}else if(apk_ver){
			tmp_apk.ver = new String(ch).substring(start, start + length);
		}else if (apk_vercode){
			try{
				tmp_apk.vercode = new Integer(new String(ch).substring(start, start + length));
			}catch(Exception e){
				tmp_apk.vercode = 0;
			}
		}else if(apk_icon){
			/*IconNode a = new IconNode(new String(ch).substring(start, start + length), tmp_apk.apkid);
			synchronized(iconFetchList) {
				iconFetchList.add(a);
			}*/
			icon_path = icon_path.concat(new String(ch).substring(start, start + length));
			hasIcon = true;
		}else if(apk_date){
			tmp_apk.date = new String(ch).substring(start, start + length);
		}else if(apk_rat){
			try{
				tmp_apk.rat = new Float(new String(ch).substring(start, start + length));
			}catch(Exception e){
				tmp_apk.rat = 0.0f;
			}
		}else if(apk_md5hash){
			tmp_apk.md5hash = tmp_apk.md5hash.concat(new String(ch).substring(start, start + length));
		}else if(apk_down){
			tmp_apk.down = new Integer(new String(ch).substring(start, start + length));
		}else if(apk_ctg){
			String tmp = new String(ch).substring(start, start + length);
			if(tmp.equals("Applications")){
				tmp_apk.catg_type = 1;
			}else if(tmp.equals("Games")){
				tmp_apk.catg_type = 0;
			}else
				tmp_apk.catg_type = 2;
		}else if(apk_ctg2){
			tmp_apk.catg = tmp_apk.catg.concat(new String(ch).substring(start, start + length));
		}else if(onDelta){
			thisDelta = thisDelta.concat(new String(ch).substring(start, start + length));
		}else if(apkcount){
			apks_n = new Integer(new String(ch).substring(start, start + length));
		}else if(apk_size){
			tmp_apk.size = new Integer(new String(ch).substring(start, start + length));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.trim().equals("package")){
			if(listapks == null){
				listapks = db.getForUpdate();
			}
			if(hasIcon){
				IconNode a = new IconNode(icon_path, tmp_apk.apkid);
				synchronized(iconFetchList) {
					iconFetchList.add(a);
				}
				hasIcon = false;
			}
			
			
			napk++;
			new_apk = false;

			readed++;
			if(readed >= 10){
				readed = 0;
				cleanTransHeap();
			}

			
			if(isRemove){
				isRemove = false;
				db.delApk(tmp_apk.apkid);
			}else{
				if(tmp_apk.name.equalsIgnoreCase("Unknown"))
					tmp_apk.name = tmp_apk.apkid;

				ApkNode node = new ApkNode(tmp_apk.apkid, tmp_apk.vercode);
				if(!listapks.contains(node)){
					db.insertApk(false,tmp_apk.name, tmp_apk.path, tmp_apk.ver, tmp_apk.vercode,tmp_apk.apkid, tmp_apk.date, tmp_apk.rat, mserver, tmp_apk.md5hash, tmp_apk.down, tmp_apk.catg, tmp_apk.catg_type, tmp_apk.size);
					//tmp_apk.isnew = false;
					//updateTable.add(tmp_apk);
					listapks.add(node);
				}else{
					int pos = listapks.indexOf(node);
					ApkNode list = listapks.get(pos);
					if(list.vercode < node.vercode){
						db.insertApk(true,tmp_apk.name, tmp_apk.path, tmp_apk.ver, tmp_apk.vercode,tmp_apk.apkid, tmp_apk.date, tmp_apk.rat, mserver, tmp_apk.md5hash, tmp_apk.down, tmp_apk.catg, tmp_apk.catg_type, tmp_apk.size);
						//tmp_apk.isnew = true;
						//updateTable.remove(new ApkNodeFull(list.apkid));
						//updateTable.add(tmp_apk);
						listapks.remove(pos);
						listapks.add(node);
					}
				}
			}
			
			/*readed++;
			if(readed >= 10){
				readed = 0;
				cleanTransHeap();
			}*/
			tmp_apk.apkid = "";
			tmp_apk.name = "Unknown";
			tmp_apk.ver = "0.0";
			tmp_apk.vercode = 0;
			tmp_apk.rat = 0.0f;
			tmp_apk.date = "2000-01-01";
			tmp_apk.down = -1;
			tmp_apk.md5hash = "";
			tmp_apk.catg="";
			tmp_apk.catg_type = 2;
			tmp_apk.path="";
			icon_path = "";
			tmp_apk.size = 0;
			pd_tick.sendEmptyMessage(0);
		}else if(localName.trim().equals("name")){
			apk_name = false;
		}else if(localName.trim().equals("path")){
			apk_path = false;
		}else if(localName.trim().equals("ver")){
			apk_ver = false;
		}else if(localName.trim().equals("vercode")){
			apk_vercode = false;
		}else if(localName.trim().equals("apkid")){
			apk_id = false;
		}else if(localName.trim().equals("icon")){
			apk_icon = false;
		}else if(localName.trim().equals("date")){
			apk_date = false;
		}else if(localName.trim().equals("rat")){
			apk_rat = false;
		}else if(localName.trim().equals("md5h")){
			apk_md5hash = false;
		}else if(localName.trim().equals("dwn")){
			apk_down = false;
		}else if(localName.trim().equals("catg")){
			apk_ctg = false;
		}else if(localName.trim().equals("catg2")){
			apk_ctg2 = false;
		}else if(localName.trim().equals("repository")){
			if(!isDelta){
				db.cleanRepoApps(mserver);
				cleanTransHeap();
			}
			listapks = db.getForUpdate();
		}else if(localName.trim().equals("delta")){
			onDelta = false;
			if(thisDelta.equalsIgnoreCase("")){
				extras_hd.sendEmptyMessage(0);
			}
		}else if(localName.trim().equals("appscount")){
			apkcount = false;
			int what = 2*apks_n;
			pd_set.sendEmptyMessage(what);
		}else if(localName.trim().equals("sz")){
			apk_size = false;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.trim().equals("package")){
			new_apk = true;
		}else if(localName.trim().equals("name")){
			apk_name = true;
		}else if(localName.trim().equals("path")){
			apk_path = true;
		}else if(localName.trim().equals("ver")){
			apk_ver = true;
		}else if(localName.trim().equals("vercode")){
			apk_vercode = true;
		}else if(localName.trim().equals("apkid")){
			apk_id = true;
		}else if(localName.trim().equals("icon")){
			apk_icon = true;
		}else if(localName.trim().equals("date")){
			apk_date = true;
		}else if(localName.trim().equals("rat")){
			apk_rat = true;
		}else if(localName.trim().equals("md5h")){
			apk_md5hash = true;
		}else if(localName.trim().equals("dwn")){
			apk_down = true;
		}else if(localName.trim().equals("catg")){
			apk_ctg = true;
		}else if(localName.trim().equals("catg2")){
			apk_ctg2 = true;
		}else if(localName.trim().equals("delta")){
			Log.d("Aptoide","Is a delta...");
			isDelta = true;
			onDelta = true;
		}else if(localName.trim().equals("del")){
			Log.d("Aptoide","Is a remove...");
			isRemove = true;
			apks_n--;
		}else if(localName.trim().equals("appscount")){
			apkcount = true;
		}else if(localName.trim().equals("sz")){
			apk_size = true;
		}
	}
	
	
	
	
	@Override
	public void startDocument() throws SAXException {
		String[] logins = null; 
		logins = db.getLogin(mserver);
		if(logins != null){
			//requireLogin = true;
			usern = logins[0];
			passwd = logins[1];
		}
		
		mHttpClient = NetworkApis.createItOpen(mserver, usern, passwd);
		//mHttpClient2 = NetworkApis.createItOpen(mserver, usern, passwd);
		
		new Thread() {
			public void run() {
				try{
					while(iconsInPool){
						while(!iconFetchList.isEmpty()){
							IconNode node = null;
							synchronized(iconFetchList){
								node = iconFetchList.remove(0);
							}
							String test_file = mctx.getString(R.string.icons_path) + node.name;
							
							File exists = new File(test_file);
							if(exists.exists()){
								pd_tick.sendEmptyMessage(0);
							}else {
								synchronized(iconFinalFetchList){
									iconFinalFetchList.add(node);
								}

							}
						}
						Thread.sleep(1000);
					}
				} catch (Exception e) { 
				}
			}
		}.start();
		
		new Thread() {
			public void run() {
				IconNode node = null;
				try{
					while(true){
						Log.d("Aptoide","A1 - " + mserver);
						Thread.sleep(2000);
						if(iconFinalFetchList.isEmpty() || (!iconsInPool)){
							Log.d("Aptoide","break A1 - " + mserver);
							break;
						}else{
							synchronized(iconFinalFetchList){
								Log.d("Aptoide","Removing onde - " + mserver);
								node = iconFinalFetchList.remove(0);
							}
							getIcon(node.url, node.name);
						}
					}
				}catch (Exception e){
				}
			}
		}.start();
		
		db.startTrans();
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d("Aptoide","Done parsing XML from " + mserver + " ...");
		int oldNapks = db.getServerNApk(mserver);
		if(isDelta){
			apks_n += oldNapks;
		}
		if(apks_n != -1)
			db.updateServerNApk(mserver, apks_n);
		else
			db.updateServerNApk(mserver, napk);
		db.endTrans();
		
		if(isDelta){
			if(!thisDelta.equalsIgnoreCase("")){
				db.setServerDelta(mserver, thisDelta);
			}/*else{
				boolean iso = extras_hd.sendEmptyMessage(0);
				Log.d("Aptoide","Delta is empty... disable extras! - " + iso);
			}*/
		}else{
			File thisXML = new File(mctx.getString(R.string.info_path));
			Md5Handler hash = new Md5Handler();
			String deltahash = hash.md5Calc(thisXML);
			Log.d("Aptoide","A adicionar novo hash delta: " + mserver + ":" + deltahash);
			db.setServerDelta(mserver, deltahash);
		}
		

		if(is_last){
			new Thread() {
				public void run() {
					try{

						while(true) {
							if(iconFetchList.size() == 0){
								iconsInPool = false;
								break;
							}
							Thread.sleep(2000);
						}

						Thread main_icon_thread = new Thread(new FetchIcons(), "T1");

						main_icon_thread.start();					

					} catch (Exception e) {  }
				}
			}.start();
		}else{
			iconsInPool = false;
			synchronized (iconFetchList) {
				iconFinalFetchList.clear();
			}
		}
		
		/*IconNode node = null;
		try{
			while(true){
				if(iconFinalFetchList.isEmpty()){
					break;
				}else{
					synchronized(iconFinalFetchList){
						node = iconFinalFetchList.remove(0);
					}
					Log.d("Aptoide","A2");
					getIcon(node.url, node.name);
				}
			}
		}catch (Exception e){
		}*/
		super.endDocument();
	}
	
	private void cleanTransHeap(){
		db.endTrans();
		db.startTrans();
	}

	private void getIcon(String uri, String name){
		String url = mserver + "/" + uri;
		String file = mctx.getString(R.string.icons_path) + name;
		
		Log.d("Aptoide","getIcon: " + uri + " - " + mserver);
		pd_tick.sendEmptyMessage(0);
		
		try {
			FileOutputStream saveit = new FileOutputStream(file);
			
			HttpResponse mHttpResponse = NetworkApis.getHttpResponse(url, usern, passwd, mctx); //NetworkApis.fetch(url, mHttpClient); //
			
			if(mHttpResponse.getStatusLine().getStatusCode() == 401){
				return;
			}else if(mHttpResponse.getStatusLine().getStatusCode() == 403){
				return;
			}else{
				/*InputStream getit = mHttpResponse.getEntity().getContent();
				byte data[] = new byte[8096];
				int readed;
				while((readed = getit.read(data, 0, 8096)) != -1) {
					saveit.write(data,0,readed);
				}*/
				
				byte[] buffer = EntityUtils.toByteArray(mHttpResponse.getEntity());
				saveit.write(buffer);
			}
			
			Log.d("Aptoide","getIcon done: " + uri + "/" + name + " - " + mserver);

			
		}catch (Exception e){
			/*System.out.println("========================222===========================");
			e.printStackTrace();
			System.out.println("=========================222==========================");*/
			Log.d("Aptoide","Error fetching icon.");
		}
	}
	
	private class FetchIcons implements Runnable {
		public FetchIcons() {	}
		
		public void run() {
			IconNode node = null;
			try{
				while(true){
					if(sPref.getBoolean("kill_thread", false))
						break;
					if(iconFinalFetchList.isEmpty()){
						Log.d("Aptoide","List of icons is empty - " + mserver);
						break;
					}else{
						synchronized(iconFinalFetchList){
							node = iconFinalFetchList.remove(0);
						}
						if(node != null){
							Log.d("Aptoide","A2 - " + mserver);
							getIcon(node.url, node.name);
						}
					}
				}

			}catch (Exception e){ 
				Log.d("Aptoide", "Wash exception? " + e.toString());
			}
		}
	}

}
