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

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class NewServerRssHandler extends DefaultHandler{
	
	private String[] node = null;
	
	private Context mctx;
	
	private Vector<String> servers = new Vector<String>();
	private Vector<String[]> apks = new Vector<String[]>();
	
	private boolean new_serv_lst = false;
	private boolean new_serv = false;
	
	private boolean app_list = false;
	private boolean app_apk = false;
	private boolean app_name = false;
	
	public NewServerRssHandler(Context ctx){
		mctx = ctx;
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if(new_serv){
			servers.add(new String(ch).substring(start, start + length));
		}else if(app_apk){
			//apks.add(new String(ch).substring(start, start + length));
			node[0] = new String(ch).substring(start, start + length);
		}else if(app_name){
			node[1] = new String(ch).substring(start, start + length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.trim().equals("newserver")){
			new_serv_lst = false;
		}else if(localName.trim().equals("server")){
			new_serv = false;
		}else if(localName.trim().equals("getapp")){
			app_list = false;
			apks.add(node);
			node = null;
		}else if(localName.trim().equals("get")){
			app_apk = false;
		}else if(localName.trim().equals("name")){
			app_name = false;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.trim().equals("newserver")){
			new_serv_lst = true;
		}else if(localName.trim().equals("server")){
			new_serv = true;
		}else if(localName.trim().equals("getapp")){
			app_list = true;
			node = new String[2];
		}else if(localName.trim().equals("get")){
			app_apk = true;
		}else if(localName.trim().equals("name")){
			app_name = true;
		}
	}
	
	public Vector<String> getNewSrvs(){
		return servers;
	}
	
	public Vector<String[]> getNewApks(){
		return apks;
	}

}
