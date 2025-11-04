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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.aguamarina.pr.multiversion.VersionApk;





import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbHandler {
	
	private Context mctx = null;

	private static final String[] CATGS = {"Comics", "Communication", "Entertainment", "Finance", "Health", "Lifestyle", "Multimedia", 
		 "News & Weather", "Productivity", "Reference", "Shopping", "Social", "Sports", "Themes", "Tools", 
		 "Travel, Demo", "Software Libraries", "Arcade & Action", "Brain & Puzzle", "Cards & Casino", "Casual", "Emulators"};
	
	
	private static final String DATABASE_NAME = "aguamarina_db";
	private static final String TABLE_NAME_LOCAL = "local";
	private static final String TABLE_NAME = "aguamarina";
	private static final String TABLE_NAME_URI = "servers";
	private static final String TABLE_NAME_EXTRA = "extra";
	private static final String TABLE_NAME_OLD_VERSIONS = "old_versions";
	
	
	
	
	private static SQLiteDatabase db = null;
	
	private static final String CREATE_TABLE_AGUAMARINA = "create table if not exists " + TABLE_NAME + " (apkid text, "
	            + "name text not null, path text not null, lastver text not null, lastvercode number not null, "
	            + "server text, md5hash text, size number default 0 not null, sdkver number default 0 not null, primary key(apkid, server));";
	
	private static final String CREATE_TABLE_LOCAL = "create table if not exists " + TABLE_NAME_LOCAL + " (apkid text, "
				+ "instver text not null, instvercode number not null, primary key(apkid));";
	
	private static final String CREATE_TABLE_URI = "create table if not exists " + TABLE_NAME_URI 
				+ " (uri text primary key, inuse integer not null, napk integer default -1 not null,"
				+ " secure integer default 0 not null, updatetime text default 0 not null, delta text default 0 not null);";
	
	private static final String CREATE_TABLE_EXTRA = "create table if not exists " + TABLE_NAME_EXTRA
				+ " (apkid text, dt date, desc text, catg text default 'Other' not null,"
				+ " catg_ord integer default 2 not null, primary key(apkid));";
	
	private static final String CREATE_TABLE_OLD_VERSIONS = "create table if not exists " + TABLE_NAME_OLD_VERSIONS
				+ " (apkid text, name text not null, path text not null, ver text not null, vercode number not null, " 
				+ " md5hash text, size number default 0 not null, sdkver number default 0 not null, server text, primary key(apkid,ver,server));";
	
	
	Map<String, Object> getCountSecCatg(int ord){
		final String basic_query = "select a.catg, count(a.apkid) from " + TABLE_NAME_EXTRA + " as a where a.catg_ord = " + ord + " and not exists" +
								   " (select * from " + TABLE_NAME_LOCAL + " as b where b.apkid = a.apkid) group by catg;"; 
		
		//final String basic_query2 = "select catg, count(*) from " + TABLE_NAME_EXTRA + " where catg_ord = " + ord + " group by catg;";
		Map<String, Object> count_lst = new HashMap<String, Object>();
		Cursor q = null;
		
		q = db.rawQuery(basic_query, null);
		if(q.moveToFirst()){
			count_lst.put(q.getString(0), q.getInt(1));
			while(q.moveToNext()){
				count_lst.put(q.getString(0), q.getInt(1));
			}
			q.close();
			return count_lst;
		}else{
			q.close();
			return null;
		}
	}
	
	int[] getCountMainCtg(){
		final String basic_query = "select a.catg_ord, count(a.apkid) from " + TABLE_NAME_EXTRA + " as a where not exists " +
				                   "(select * from " + TABLE_NAME_LOCAL + " as b where b.apkid = a.apkid) group by catg_ord;";
		//final String basic_query2 = "select catg_ord, count(*) from " + TABLE_NAME_EXTRA + " group by catg_ord;";		
		int[] rtn = new int[3];
		Cursor q = null;

		q = db.rawQuery(basic_query, null);
		if(q.moveToFirst()){
			rtn[q.getInt(0)] = q.getInt(1);
			while(q.moveToNext()){
				rtn[q.getInt(0)] = q.getInt(1);
			}
			q.close();
			return rtn;
		}else{
			q.close();
			return null;
		}
	}
	
	/*
	 * catg_ord: game (0) / application (1) / others(2) 
	 * 
	 * catg: category for the application:
	 *  - Comics, Communication, Entertainment, Finance, Health, Lifestyle, Multimedia, 
	 *  - News & Weather, Productivity, Reference, Shopping, Social, Sports, Themes, Tools, 
	 *  - Travel, Demo, Software Libraries, Arcade & Action, Brain & Puzzle, Cards & Casino, Casual, Emulators,
	 *  - Other
	 */

	public DbHandler(Context ctx) {
		mctx = ctx;
		if(db == null){
			db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);
			db.execSQL(CREATE_TABLE_URI);
			db.execSQL(CREATE_TABLE_EXTRA);
			db.execSQL(CREATE_TABLE_AGUAMARINA);
			db.execSQL(CREATE_TABLE_LOCAL);
			db.execSQL(CREATE_TABLE_OLD_VERSIONS);
		}else if(!db.isOpen()){
			db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);
		}
	}
	
	/*public void updateBasePathRepo(String repo, String basepath){
		ContentValues tmp = new ContentValues();
		tmp.put("base_path", basepath);
		db.update(TABLE_NAME_URI, tmp, "uri='" + repo + "'", null);
	}*/
	
	public void startTrans(){
		db.beginTransaction();
	}
	
	public void endTrans(){
		try{
			db.setTransactionSuccessful();
		}catch (Exception e){
		}finally{
			db.endTransaction();
		}
	}
	
	/*
	 * Code for DB update on new version of Aguamarina
	 */
	public void UpdateTables(){
		String[] repos = null;
		int[] inuser = null;
//		boolean[] secure = null;
//		String[] login_user = null;
//		String[] login_pwd = null;
		try{
			Cursor c;
			c = db.query(TABLE_NAME_URI, new String[] {"uri","inuse"}, null, null, null, null, null, null);
			if(c.moveToFirst()){
				int i = 0;
				repos = new String[c.getCount()+1];
				inuser = new int[c.getCount()+1];
//				secure = new boolean[c.getCount()+1];
//				login_pwd = new String[c.getCount()+1];
//				login_user = new String[c.getCount()+1];
				repos[i] = "http://aguamarina.altervista.org/repo";
				inuser[i] = 1;
//				secure[i] = false;


				i++;
				repos[i] = c.getString(0);
				inuser[i] = c.getInt(1);
				/*if(c.getInt(4) == 1){
					secure[i] = true;
				}else{
					secure[i] = false;
				}*/

				while(c.moveToNext()){
					i++;
					repos[i] = c.getString(0);
					inuser[i] = c.getInt(1);
					/*if(c.getInt(4) == 1){
						secure[i] = true;
					}else{
						secure[i] = false;
					}*/
				}
			}
			c.close();
			db.execSQL("drop table if exists " + TABLE_NAME);
			db.execSQL("drop table if exists " + TABLE_NAME_EXTRA);
			db.execSQL("drop table if exists " + TABLE_NAME_LOCAL);
			db.execSQL("drop table if exists " + TABLE_NAME_URI);
			db.execSQL("drop table if exists " + TABLE_NAME_OLD_VERSIONS);
			db.execSQL(CREATE_TABLE_URI);
			db.execSQL(CREATE_TABLE_EXTRA);
			db.execSQL(CREATE_TABLE_AGUAMARINA);
			db.execSQL(CREATE_TABLE_LOCAL);
			db.execSQL(CREATE_TABLE_OLD_VERSIONS);
			/*for(String uri: repos){
				ContentValues tmp = new ContentValues();
				tmp.put("uri", uri);
				tmp.put("inuse", 0);
				db.insert(TABLE_NAME_URI, null, tmp);
			}*/
			for(int z = 0; z < repos.length; z++){
				if(!repos[z].equalsIgnoreCase("http://aguamarina.altervista.org/repo")){
					ContentValues tmp = new ContentValues();
					tmp.put("uri", repos[z]);
					tmp.put("inuse", inuser[z]);
					db.insert(TABLE_NAME_URI, null, tmp);
				}
			}
			
		}catch(Exception e){ }
	}
	
	public void delApk(String apkid, String ver){
		if(db.delete(TABLE_NAME, "apkid='"+apkid+"' and lastver='"+ver+"'", null)==1)
			db.delete(TABLE_NAME_EXTRA, "apkid='"+apkid+"'", null);
		db.delete(TABLE_NAME_OLD_VERSIONS, "apkid='"+apkid+"' and ver='"+ver+"'", null);
	}
	 
	public void insertApk(boolean delfirst, String name, String path, String ver, int vercode ,String apkid, String date, String serv, String md5hash, String catg, int catg_type, int size, int sdkver){

		if(delfirst){
			db.delete(TABLE_NAME, "apkid='"+apkid+"'", null);
			db.delete(TABLE_NAME_EXTRA, "apkid='"+apkid+"'", null);
		}
		
		ContentValues tmp = new ContentValues();
		tmp.put("apkid", apkid);
		tmp.put("name", name);
		tmp.put("path", path);
		tmp.put("lastver", ver);
		tmp.put("lastvercode", vercode);
		tmp.put("server", serv);
		tmp.put("md5hash", md5hash);
		tmp.put("size", size);
		tmp.put("sdkver", sdkver);
		db.insert(TABLE_NAME, null, tmp);
		tmp.clear();
		tmp.put("apkid", apkid);
//		tmp.put("rat", rat);
		tmp.put("dt", date);
//		tmp.put("dwn", down);
		tmp.put("catg_ord", catg_type);
		for (String node : CATGS) {
			if(node.equals(catg)){
				tmp.put("catg", node);
				break;
			}
		}
		db.insert(TABLE_NAME_EXTRA, null, tmp);
		
   		PackageManager mPm = mctx.getPackageManager();
		try {
			PackageInfo pkginfo = mPm.getPackageInfo(apkid, 0);
			String vers = pkginfo.versionName;
		    int verscode = pkginfo.versionCode;
		    insertInstalled(apkid, vers, verscode);
		} catch (NameNotFoundException e) {
			//Not installed... do nothing
		}
		
	}
	
	/**
	 * @author rafael
	 * 
	 * @param name
	 * @param path
	 * @param ver
	 * @param vercode
	 * @param apkid
	 * @param serv
	 * @param md5hash
	 * @param size
	 */
	public void insertOldApk(String name, String path, String ver, int vercode ,String apkid, String serv, String md5hash, int size, int sdkver){
		
		ContentValues tmp = new ContentValues();
		tmp.put("apkid", apkid);
		tmp.put("name", name);
		tmp.put("path", path);
		tmp.put("ver", ver);
		tmp.put("vercode", vercode);
		tmp.put("md5hash", md5hash);
		tmp.put("size", size);
		tmp.put("sdkver", sdkver);
		tmp.put("server", serv);
//		tmp.put("dwn", dwn);
		db.insert(TABLE_NAME_OLD_VERSIONS, null, tmp);
		
   		PackageManager mPm = mctx.getPackageManager();
		try {
			PackageInfo pkginfo = mPm.getPackageInfo(apkid, 0);
			String vers = pkginfo.versionName;
		    int verscode = pkginfo.versionCode;
			insertInstalled(apkid, vers, verscode);
		} catch (NameNotFoundException e) {
			//Not installed... do nothing
		}
		
	}
	
	/**
	 * @author rafael
	 * 
	 * @param tmp
	 * @param mserver
	 */
	public void insertOldApk(ApkNodeFull tmp, String mserver){
		insertOldApk(tmp.name, tmp.path, tmp.ver, tmp.vercode,tmp.apkid, mserver, tmp.md5hash, tmp.size, tmp.sdkver);
	}
	
	/**
	 * @author rafael
	 * 
	 * 
	 * @param apkid
	 * @param ver
	 * @return
	 */
	public boolean insertInstalled(String apkid, String ver){
		ContentValues tmp = new ContentValues();
		tmp.put("apkid", apkid);
		Cursor c = db.query(TABLE_NAME, new String[] {"lastvercode"}, " apkid=\""+apkid+"\" and lastver=\""+ver+"\" ", null, null, null, null);
		
		if(!c.moveToFirst()){
			c = db.query(TABLE_NAME_OLD_VERSIONS, new String[] {"vercode"}, " apkid=\""+apkid+"\" and ver=\""+ver+"\" ", null, null, null, null);
			if(!c.moveToFirst()){
				c.close();
				return false;
			}
		}
		
		tmp.put("instver", ver);
		tmp.put("instvercode", c.getInt(0));
		c.close();
		
		return (db.insert(TABLE_NAME_LOCAL, null, tmp) > 0); 
	}
	
	public boolean wasUpdateOrDowngrade(String apkid, int versioncode){
		Cursor c = db.query(TABLE_NAME_LOCAL, new String[] {"instvercode"}, "apkid=\""+apkid+"\"", null, null, null, null);
		
		if(!c.moveToFirst())
			return true;
		
		int bd_code = c.getInt(0);
		c.close();
		return (versioncode != bd_code);
	}
	
	/*
	 * With explicit version
	 */
	public boolean insertInstalled(String apkid, String ver, int vercode){
		ContentValues tmp = new ContentValues();
		tmp.put("apkid", apkid);
		tmp.put("instver", ver);
		tmp.put("instvercode", vercode);
		long i = db.insert(TABLE_NAME_LOCAL, null, tmp);
		return i > 0; 
	}
	
	public boolean UpdateInstalled(String apkid, String ver, int vercode){
		ContentValues tmp = new ContentValues();
		tmp.put("instver", ver);
		tmp.put("instvercode", vercode);
		return (db.update(TABLE_NAME_LOCAL, tmp, "apkid='" + apkid + "'", null) > 0); 
	}
	
	public boolean removeInstalled(String apkid){
		return (db.delete(TABLE_NAME_LOCAL, "apkid='"+apkid+"'", null) > 0);
	}
	
	/*public boolean removeAlli(){
		db.delete(TABLE_NAME_EXTRA, null, null);
		return (db.delete(TABLE_NAME, null, null) > 0);
	}*/
	
	public Vector<ApkNode> getForUpdate(){
		Vector<ApkNode> tmp = new Vector<ApkNode>();
		Cursor c = null;
		
		try{
			c = db.query(TABLE_NAME, new String[]{"apkid", "lastvercode"}, null, null, null, null, null);
			if(c.moveToFirst()){
				ApkNode node = new ApkNode(c.getString(0), c.getInt(1));
				tmp.add(node);
				while(c.moveToNext()){
					node = new ApkNode(c.getString(0), c.getInt(1));
					tmp.add(node);
				}
			}
		c.close();	
		}catch (Exception e) {c.close(); return null;}
		return tmp;
	}
	
	public Vector<ApkNode> getAll(String type){
		Vector<ApkNode> tmp = new Vector<ApkNode>();
		Cursor c = null;
		try{
			
			final String basic_query = "select distinct c.apkid, c.name, c.instver, c.lastver, c.instvercode, c.lastvercode, c.sdkver ,b.dt, b.catg, b.catg_ord from "
				+ "(select distinct a.apkid as apkid, a.name as name, l.instver as instver, l.instvercode as instvercode, a.lastver as lastver, a.lastvercode as lastvercode, a.sdkver as sdkver from "
				+ TABLE_NAME + " as a left join " + TABLE_NAME_LOCAL + " as l on a.apkid = l.apkid) as c left join "
				+ TABLE_NAME_EXTRA + " as b on c.apkid = b.apkid";
			
//			final String rat = " order by rat desc";
			final String mr = " order by dt desc";
			final String alfb = " order by name collate nocase";
//			final String down = " order by dwn desc";
						
			String search;
			if(type.equalsIgnoreCase("abc")){
				search = basic_query+alfb;
//			}else if(type.equalsIgnoreCase("dwn")){
//				search = basic_query+down;
			}else if(type.equalsIgnoreCase("rct")){
				search = basic_query+mr;
//			}else if(type.equalsIgnoreCase("rat")){
//				search = basic_query+rat;
			}else{
				search = basic_query;
			}
			c = db.rawQuery(search, null);
			c.moveToFirst();
			
			
			for(int i = 0; i< c.getCount(); i++){
				ApkNode node = new ApkNode();
				node.apkid = c.getString(0);
				node.name = c.getString(1);
				if(c.getString(2) == null){
					node.status = 0;
					node.ver = c.getString(3);
				}else{
					
					
					
					
					
					int instvercode = c.getInt(4);
					int lastvercode = c.getInt(5);
					
					if(instvercode >= lastvercode){
						
						if(getOldApks(node.apkid).size()==0 && instvercode == lastvercode){
							node.status = 1;
						} else {
							node.status = 3;
						}
						
						node.ver = c.getString(2);
					}else if(instvercode < lastvercode){
						node.status = 2;
						node.verUpdate = c.getString(3);
						node.ver = c.getString(2) + "/ new: " + c.getString(3);
					}
					
				}
//				node.rat = c.getFloat(7);
//				node.down = c.getInt(8);
				node.catg = c.getString(7);
				node.catg_ord = c.getInt(8);
				node.sdkver = c.getInt(9);
				tmp.add(node);
				c.moveToNext();
			}
		}catch (Exception e){ 
			e.printStackTrace();
		}
		finally{
			c.close();
		}
		return tmp;
	}
	
	public Vector<ApkNode> getAll(String type, String ctg, int ord){
		Vector<ApkNode> tmp = new Vector<ApkNode>();
		Cursor c = null;
		try{
			String catgi = "d.catg = '" + ctg + "' and ";
			if(ctg == null)
				catgi = "";
			
			final String basic_query = "select * from (select distinct c.apkid, c.name as name, c.instver as instver, c.lastver, c.instvercode, c.lastvercode, c.sdkver ,b.dt as dt, b.catg as catg, b.catg_ord as catg_ord from "
				+ "(select distinct a.apkid as apkid, a.name as name, l.instver as instver, l.instvercode as instvercode, a.lastver as lastver, a.lastvercode as lastvercode, a.sdkver as sdkver from "
				+ TABLE_NAME + " as a left join " + TABLE_NAME_LOCAL + " as l on a.apkid = l.apkid) as c left join "
				+ TABLE_NAME_EXTRA + " as b on c.apkid = b.apkid) as d where " + catgi + "d.catg_ord = " + ord + " and d.instver is null";
			
			
//			final String rat = " order by rat desc";
			final String mr = " order by dt desc";
			final String alfb = " order by name collate nocase";
//			final String down = " order by dwn desc";
						
			String search;
			if(type.equalsIgnoreCase("abc")){
				search = basic_query+alfb;
//			}else if(type.equalsIgnoreCase("dwn")){
//				search = basic_query+down;
			}else if(type.equalsIgnoreCase("rct")){
				search = basic_query+mr;
//			}else if(type.equalsIgnoreCase("rat")){
//				search = basic_query+rat;
			}else{
				search = basic_query;
			}
			c = db.rawQuery(search, null);
			c.moveToFirst();
			
			
			for(int i = 0; i< c.getCount(); i++){
				ApkNode node = new ApkNode();
				node.apkid = c.getString(0);
				node.name = c.getString(1);
				node.status = 0;
				node.ver = c.getString(3);
//				node.rat = c.getFloat(7);
//				node.down = c.getInt(8);
				node.sdkver = c.getInt(9);
				tmp.add(node);
				c.moveToNext();
			}
		}catch (Exception e){ 
			e.printStackTrace();
		}
		finally{
			if(c != null)
				c.close();
		}
		return tmp;
	}
	
	/*
	 * Same get type function, used in search
	 */
	public Vector<ApkNode> getSearch(String exp, String type){
		Vector<ApkNode> tmp = new Vector<ApkNode>();
		Cursor c = null;
		try{
			
			final String basic_query = "select distinct c.apkid, c.name, c.instver, c.lastver, c.instvercode, c.lastvercode, c.sdkver, b.dt from "
				+ "(select distinct a.apkid as apkid, a.name as name, l.instver as instver, a.lastver as lastver, l.instvercode as instvercode, a.lastvercode as lastvercode, a.sdkver as sdkver from "
				+ TABLE_NAME + " as a left join " + TABLE_NAME_LOCAL + " as l on a.apkid = l.apkid) as c left join "
				+ TABLE_NAME_EXTRA + " as b on c.apkid = b.apkid where name like '%" + exp + "%'";
			
			final String iu = " order by instver desc";
//			final String rat = " order by rat desc";
			final String mr = " order by dt desc";
			final String alfb = " order by name collate nocase";
			//final String down = " order by dwn desc";

			
			String search;
			if(type.equalsIgnoreCase("abc")){
				search = basic_query+alfb;
			}else if(type.equalsIgnoreCase("iu")){
				search = basic_query+iu;
			}else if(type.equalsIgnoreCase("recent")){
				search = basic_query+mr;
//			}else if(type.equalsIgnoreCase("rating")){
//				search = basic_query+rat;
			}else{
				search = basic_query;
			}
			
			c = db.rawQuery(search, null);
			c.moveToFirst();
			for(int i = 0; i< c.getCount(); i++){
				ApkNode node = new ApkNode();
				node.apkid = c.getString(0);
				node.name = c.getString(1);
				if(c.getString(2) == null){
					node.status = 0;
				}else{
					//if(c.getString(2).equalsIgnoreCase(c.getString(3))){
					node.ver = c.getString(2);
					if(c.getInt(4) == c.getInt(5)){
						node.status = 1;
					}else{
						int instvercode = c.getInt(4);
						int lastvercode = c.getInt(5);
						if(instvercode < lastvercode){
							node.status = 2;
							node.ver += "/ new: " + c.getString(3);
						}else{
							node.status = 1;
						}
					}
					
				}
//				node.rat = c.getFloat(7);
				tmp.add(node);
				c.moveToNext();
			}
			//c.close();
		}catch (Exception e){ }
		finally{
			if(c != null)
				c.close();
		}
		return tmp;
	}
	
	/*
	 * Same function as above, used in list of updates
	 */
	public Vector<ApkNode> getUpdates(String type){
		Vector<ApkNode> tmp = new Vector<ApkNode>();
		Cursor c = null;
		try{
			
			final String basic_query = "select distinct c.apkid, c.name, c.instver, c.lastver, c.instvercode, c.lastvercode, c.sdkver ,b.dt from "
				+ "(select distinct a.apkid as apkid, a.name as name, l.instver as instver, l.instvercode as instvercode, a.lastver as lastver, a.lastvercode as lastvercode, a.sdkver as sdkver from "
				+ TABLE_NAME + " as a left join " + TABLE_NAME_LOCAL + " as l on a.apkid = l.apkid) as c left join "
				+ TABLE_NAME_EXTRA + " as b on c.apkid = b.apkid where c.instvercode < c.lastvercode";
			
//			final String rat = " order by rat desc";
			final String mr = " order by dt desc";
			final String alfb = " order by name collate nocase";
//			final String down = " order by dwn desc";
			
			
			String search;
			if(type.equalsIgnoreCase("abc")){
				search = basic_query+alfb;
//			}else if(type.equalsIgnoreCase("dwn")){
//				search = basic_query+down;
			}else if(type.equalsIgnoreCase("rct")){
				search = basic_query+mr;
//			}else if(type.equalsIgnoreCase("rat")){
//				search = basic_query+rat;
			}else{
				search = basic_query;
			}
			c = db.rawQuery(search, null);
			c.moveToFirst();
			
			
			for(int i = 0; i< c.getCount(); i++){
				ApkNode node = new ApkNode();
				node.apkid = c.getString(0);
				node.name = c.getString(1);
				if(c.getString(2) == null){
					node.status = 0;
				}else{
					//if(c.getString(2).equalsIgnoreCase(c.getString(3))){
					if(c.getInt(4) == c.getInt(5)){
						node.status = 1;
						node.ver = c.getString(2);
					}else{
						int instvercode = c.getInt(4);
						int lastvercode = c.getInt(5);
						if(instvercode < lastvercode){
							node.status = 2;
							node.ver = c.getString(2) + "/ new: " + c.getString(3);
						}else{
							node.status = 1;
							node.ver = c.getString(2);
						}
					}
					
				}
//				node.rat = c.getFloat(7);
				tmp.add(node);
				c.moveToNext();
			}
		}catch (Exception e){ 	}
		finally{
			c.close();
		}
		return tmp;
	}
	
	public Vector<String> getApk(String id){
		Vector<String> tmp = new Vector<String>();
		Cursor c = null;
		int size = 0;
		String lastvercode = "";
		int minsdk = 1;
		try{
			c = db.query(TABLE_NAME, new String[] {"server", "lastver", "size", "lastvercode", "sdkver"}, "apkid=\""+id.toString()+"\"", null, null, null, null);
			c.moveToFirst();
			/*String tmp_serv = new String();
			for(int i=0; i<c.getCount(); i++){
				tmp_serv = tmp_serv.concat(c.getString(0)+"\n");
				c.moveToNext();
			}
			tmp.add(tmp_serv);
			c.moveToPrevious();*/
			tmp.add(c.getString(0));
			tmp.add("\t" + c.getString(1)+"\n");
			size = c.getInt(2);
			
			lastvercode = c.getInt(3)+"";
			minsdk = c.getInt(4);
			
			c = db.query(TABLE_NAME_LOCAL, new String[] {"instver"}, "apkid=\""+id.toString()+"\"", null, null, null, null);
			if(c.getCount() == 0){
				tmp.add("\tno\n");
				tmp.add("\t--- \n");
			}else{
				tmp.add("\tyes\n");
				c.moveToFirst();
				tmp.add("\t"+c.getString(0)+"\n");
			}
			
//			c = db.query(TABLE_NAME_EXTRA, new String[] {"dwn", "rat"}, "apkid=\""+id.toString()+"\"", null, null, null, null);
//			c.moveToFirst();
//			int downloads = c.getInt(0);
//			float rat = c.getFloat(1);
			
			
//			tmp.add(Integer.toString(downloads));
			
//			tmp.add(Float.toString(rat));
			
			tmp.add(new Integer(size).toString());

			tmp.add(lastvercode);
			
			tmp.add(new Integer(minsdk).toString());
			//c.close();
		}catch (Exception e){
			//System.out.println(e.toString());
		}finally{
			c.close();
		}
		return tmp;
	}
	
	/**
	 * @author rafael
	 * 
	 * @param apk_id
	 * @return
	 */
	public ArrayList<VersionApk> getOldApks(String apk_id){
		ArrayList<VersionApk> tmp = new ArrayList<VersionApk>();
		Cursor c = null;
		try{
			
			c = db.query(TABLE_NAME_OLD_VERSIONS, new String[] {"ver", "size", "vercode", "sdkver"}, "apkid=\""+apk_id+"\"", null, null, null, null, null);
			c.moveToFirst();
			
			do{
				tmp.add( new VersionApk(c.getString(0), c.getInt(2), apk_id, c.getInt(1),c.getInt(3)) );
			}while(c.moveToNext());
			
		}catch (Exception e){
			Log.e("Aguamarina", e.getMessage());
		}finally{
			c.close();
		}
		return tmp;
	}
	
	
	/**
	 * @author rafael
	 * 
	 * @param apk_id
	 * @param server
	 * @return
	 */
	public Vector<String> copyFromRecentApkToOldApk(String apk_id, String server){
		Vector<String> tmp = new Vector<String>();
		Cursor c = null;
		ApkNodeFull tmp_apk = new ApkNodeFull();
		
		tmp_apk.apkid = apk_id;
		tmp_apk.name = "";
		tmp_apk.ver = "0.0";
		tmp_apk.vercode = -1;
		tmp_apk.md5hash = "";
		tmp_apk.path="";
		tmp_apk.size = 0;
		tmp_apk.sdkver = 0;
//		tmp_apk.down = -1;
		
//		private static final String CREATE_TABLE_AGUAMARINA = "create table if not exists " + TABLE_NAME + " (apkid text, "
//        + "name text not null, path text not null, lastver text not null, lastvercode number not null, "
//        + "server text, md5hash text, size number default 0 not null, primary key(apkid, server));";
//		private static final String CREATE_TABLE_EXTRA = "create table if not exists " + TABLE_NAME_EXTRA
//		+ " (apkid text, rat number, dt date, desc text, dwn number, catg text default 'Other' not null,"
//		+ " catg_ord integer default 2 not null, dt date, md5hash text, primary key(apkid));";
		
		try{
			
			c = db.query(TABLE_NAME, 
					new String[] {"name", "path", "lastver", "lastvercode", "server", "md5hash", "size", "sdkver"}
					, "server=\""+server+"\" and apkid=\""+apk_id+"\"", null, null, null, null);
			c.moveToFirst();
			tmp_apk.name = c.getString(0);
			tmp_apk.path = c.getString(1);
			tmp_apk.ver = c.getString(2);
			tmp_apk.vercode = c.getInt(3);
			server = c.getString(4);
			if(!c.isNull(5)) tmp_apk.md5hash = c.getString(5);
			tmp_apk.size = c.getInt(6);
			tmp_apk.sdkver = c.getInt(7);
			
//			c = db.query(TABLE_NAME_EXTRA, new String[] {"dwn"}, "apkid=\""+apk_id+"\"", null, null, null, null);
//			c.moveToFirst();
//			tmp_apk.down = c.getInt(0);
			
			this.insertOldApk(tmp_apk, server);
			
		}catch (Exception e){
			//System.out.println(e.toString());
		}finally{
			c.close();
		}
		return tmp;
	}
	
	
	public Vector<DownloadNode> getPathHash(String id_apk, String ver){
		Vector<DownloadNode> out = new Vector<DownloadNode>();
		Cursor c = null;
		Cursor e = null;
		try{
			c = db.query(TABLE_NAME, new String[] {"server", "path", "md5hash", "size", "lastver"}, "apkid='"+id_apk+"' and lastver ='"+ver+"'", null, null, null, null);
			c.moveToFirst();
			for(int i =0; i<c.getCount(); i++){
				String repo = c.getString(0);
				
				String remotePath = repo+"/"+c.getString(1);
				String md5sum = null;
				if(!c.isNull(2)){
					md5sum = c.getString(2);
				}
				int size = c.getInt(3);
				DownloadNode node = new DownloadNode(repo, remotePath, md5sum, size);
				node.version = c.getString(4);
				out.add(node);
			}
			//c.close();
		}catch(Exception exception){
			Log.e("Aguamarina", exception.getMessage());
		}finally{
			c.close();
			if(e!=null){
				e.close();
			}
		}
		
		if(out.size()==0){
			out = getPathHashOld(id_apk, ver);
		}
		return out;
	}
	
	/**
	 * @author rafael
	 * 
	 * @param id_apk
	 * @param ver
	 * @return
	 */
	private Vector<DownloadNode> getPathHashOld(String id_apk, String ver){
		
		Vector<DownloadNode> out = new Vector<DownloadNode>();
		Cursor c = null;
		Cursor e = null;
		try{
			c = db.query(TABLE_NAME_OLD_VERSIONS, new String[] {"server", "path", "md5hash", "size", "sdkver"}, "apkid='"+id_apk+"' and ver ='"+ver+"'", null, null, null, null);
			c.moveToFirst();
			for(int i =0; i<c.getCount(); i++){
				
				String repo = c.getString(0);
				
				String remotePath = repo+"/"+c.getString(1);
				
				String md5h = null;
				if(!c.isNull(2)){
					md5h = c.getString(2);
				}
				DownloadNode node = new DownloadNode(c.getString(0), remotePath, md5h, c.getInt(3));
				node.version = ver;
				out.add(node);
			}
			//c.close();
		}catch(Exception exception){
		}finally{
			c.close();
			if(e!=null){
				e.close();
			}
				
		}
		return out;
		
	}	
	
	/*
	 * 
	 * (0): server
	 * (1): path
	 * (2): md5hash (may not exist)
	 */
	public Vector<String> getPathHash2(String id){
		Vector<String> out = new Vector<String>();
		Cursor c = null;
		try{
			c = db.query(TABLE_NAME, new String[] {"server", "path", "md5hash"}, "apkid=\""+id.toString()+"\"", null, null, null, null);
			c.moveToFirst();
			for(int i =0; i<c.getCount(); i++){
				if(c.isNull(2)){
					out.add(c.getString(0)+"/"+c.getString(1)+"*"+null);
				}else{
					out.add(c.getString(0)+"/"+c.getString(1)+"*"+c.getString(2));
				}
			}
			//c.close();
		}catch(Exception e){
		}finally{
			c.close();
		}
		return out;
	}
	
	public String getName(String id){
		String out = new String();
		Cursor c = null;
		try{
			c = db.query(TABLE_NAME, new String[] {"name"}, "apkid=\""+id.toString()+"\"", null, null, null, null);
			c.moveToFirst();
			out = c.getString(0);
			c.close();
		}catch (Exception e){}
		finally{ c.close(); }
		return out;
	}
	
	/*
	 * CHECKED
	 */
	public Vector<ServerNode> getServers(){
		Vector<ServerNode> out = new Vector<ServerNode>();
		Cursor c = null;
		try {
			c = db.rawQuery("select uri, inuse, napk, delta from " + TABLE_NAME_URI + " order by uri collate nocase", null);
			c.moveToFirst();
			for(int i=0; i<c.getCount(); i++){
				ServerNode node = new ServerNode();
				node.uri = c.getString(0);
				if(c.getInt(1) == 1){
					node.inuse = true;
				}else{
					node.inuse = false;
				}
				node.napk = c.getInt(2);
				node.hash = c.getString(3);
				out.add(node);
				c.moveToNext();
			}
		}catch (Exception e){ }
		finally{
			c.close();
		}
		return out;
	}
	
	public Vector<String> getServersName(){
		Vector<String> out = new Vector<String>();
		Cursor c = null;
		try {
			//c = db.rawQuery("select uri from " + TABLE_NAME_URI + " order by uri collate nocase", null);
			c = db.query(TABLE_NAME_URI, new String[]{"uri"}, null, null, null, null, null);
			c.moveToFirst();
			for(int i=0; i<c.getCount(); i++){
				out.add(c.getString(0));
				c.moveToNext();
			}
		}catch (Exception e){ }
		finally{
			c.close();
		}
		return out;
	}
	
	public boolean areServers(){
		boolean rt = false;
		
		Cursor c = null;
		try {
			c = db.rawQuery("select uri from " + TABLE_NAME_URI + " where inuse==1", null);
			c.moveToFirst();
			if(c.getCount() > 0)
				rt = true;
		}catch (Exception e){ }
		finally{
			c.close();
		}
		return rt;
	}
	
	public void resetServerCacheUse(String repo){
		ContentValues tmp = new ContentValues();
		tmp.put("napk", -1);
		tmp.put("updatetime", "0");
		tmp.put("delta", 0);
		db.update(TABLE_NAME_URI, tmp, "uri='" + repo + "'", null);
	}
	
	public void changeServerStatus(String uri){
		Cursor c = db.query(TABLE_NAME_URI, new String[] {"inuse"}, "uri=\""+uri+"\"", null, null, null, null);
		c.moveToFirst();
		int state = c.getInt(0);
		c.close();
		state = (state+1)%2;
		db.execSQL("update " + TABLE_NAME_URI + " set inuse=" + state + " where uri='" + uri + "'");
	}
	
	/*
	 * @inuse
	 *  1 - yes
	 *  0 - no
	 */
	public void addServer(String srv){
		ContentValues tmp = new ContentValues();
		tmp.put("uri", srv);
		tmp.put("inuse", 1);
		db.insert(TABLE_NAME_URI, null, tmp);
	}
	
	public void removeServer(Vector<String> serv){
		for(String node: serv){
			cleanRepoApps(node);
			db.delete(TABLE_NAME_URI, "uri='"+node+"'", null);
		}
	}
	
	public void removeServer(String serv){
		cleanRepoApps(serv);
		db.delete(TABLE_NAME_URI, "uri='"+serv+"'", null);
	}
	
	public void updateServer(String old, String repo){
		db.execSQL("update " + TABLE_NAME_URI + " set uri='" + repo + "' where uri='" + old + "'");
	}
	
	public void updateServerNApk(String repo, int napk){
		Log.d("Aguamarina","Update napks count to: " + napk);		
		db.execSQL("update " + TABLE_NAME_URI + " set napk=" + napk + " where uri='" + repo + "'");
	}
	
	public int getServerNApk(String repo){
		int napk = 0;
		Cursor c = null;
		try{
			c = db.query(TABLE_NAME_URI, new String[] {"napk"}, "uri='" + repo + "'", null, null, null, null);
			c.moveToFirst();
			napk = c.getInt(0);
			return napk;
		}catch (Exception e) {return 0;	}
		finally{
			c.close();
		}
	}
	
	public String getServerDelta(String srv){
		Cursor c = null;
		String rtn = null;
		try{
			c = db.query(TABLE_NAME_URI, new String[] {"delta"}, "uri='"+srv+"'", null, null, null, null);
			c.moveToFirst();
			rtn = c.getString(0);
			return rtn;
		}catch (Exception e) {return rtn;}
		finally{
			c.close();
		}
	}
	
	public void setServerDelta(String srv, String hashdelta){
		try{
		ContentValues tmp = new ContentValues();
		tmp.put("delta", hashdelta);
		db.update(TABLE_NAME_URI, tmp, "uri='"+srv+"'", null);
		}catch (Exception e) {	}
	}
	
	public void addExtraXML(String apkid, String cmt, String srv){
		Cursor c = null;
		try{
			c = db.query(TABLE_NAME, new String[] {"lastvercode"}, "server=\""+srv+"\" and apkid=\""+apkid+"\"", null, null, null, null);
			if(c.getCount() > 0){
				ContentValues extra = new ContentValues();
				if(cmt.length() > 1){
					extra.put("desc", cmt);
				}else{
					extra.putNull("desc");
				}
				db.update(TABLE_NAME_EXTRA, extra, "apkid=\""+apkid+"\"", null);
			}
		}catch(Exception e) { }
		finally{
			c.close();
		}
	}
	
	public String getDescript(String apkid){
		Cursor c = null;
		String ret = null;
		try{
			c = db.query(TABLE_NAME_EXTRA, new String[] {"desc"}, "apkid=\""+apkid+"\"", null, null, null, null);
			if(c.getCount() > 0){
				c.moveToFirst();
				ret = c.getString(0);
			}
		}
		catch(Exception e) { }
		finally{
			c.close();
		}
		return ret;
	}
	
	/*
	public String[] getLogin(String uri){
		String[] login = new String[2];
		Cursor c = null;
		try{
			c = db.query(TABLE_NAME_URI, new String[] {"secure", "user", "psd"}, "uri='" + uri + "'", null, null, null, null);
			if(c.moveToFirst()){
				if(c.getInt(0) == 0)
					return null;
				else{
					login[0] = c.getString(1);
					login[1] = c.getString(2);
				}
			}
		}catch(Exception e) { }
		finally{
			c.close();
		}
		return login;
	}
	
	public void addLogin(String user, String pwd, String repo){
		ContentValues tmp = new ContentValues();
		tmp.put("user", user);
		tmp.put("psd", pwd);
		tmp.put("secure", 1);
		db.update(TABLE_NAME_URI, tmp, "uri='" + repo + "'", null);
	}
	
	public void disableLogin(String repo){
		ContentValues tmp = new ContentValues();
		tmp.put("secure", 0);
		db.update(TABLE_NAME_URI, tmp, "uri='" + repo + "'", null);
	}
	*/
	void clodeDb(){
		db.close();
	}
	
	/*
	 * Tag is the md5 hash of server last-modified
	 */
	String getUpdateTime(String repo){
		String updt = null;
		Cursor c = null;
		try{
			c = db.query(TABLE_NAME_URI, new String[] {"updatetime"}, "uri='" + repo + "'", null, null, null, null);
			if(c.moveToFirst()){
				updt = c.getString(0);
			}
		}
		catch(Exception e) { return null;}
		finally{
			c.close();
		}
		return updt;
	}
	
	void setUpdateTime(String updt, String repo){
		ContentValues tmp = new ContentValues();
		tmp.put("updatetime", updt);
		db.update(TABLE_NAME_URI, tmp, "uri='" + repo + "'", null);
	}
	
	void cleanRepoApps(String repo){
		/*String query = "delete from " + TABLE_NAME_EXTRA + " where exists (select * from " + TABLE_NAME + " where " + 
						TABLE_NAME + ".apkid = " + TABLE_NAME_EXTRA + ".apkid and server='"+repo+"')";*/
		//db.rawQuery(query, null).close();
		int del = db.delete(TABLE_NAME_EXTRA, "exists (select * from "+TABLE_NAME+" where "+TABLE_NAME+".apkid = "+TABLE_NAME_EXTRA+".apkid and server='"+repo+"')", null);
		Log.d("Aguamarina","remved: " + del);
		int a = db.delete(TABLE_NAME, "server='"+repo+"'", null);
		Log.d("Aguamarina","Removed: " + a);
		int o = db.delete(TABLE_NAME_OLD_VERSIONS, "server='"+repo+"'", null);
		Log.d("Aguamarina","RemovedOld: " + o);
		
	}
}
