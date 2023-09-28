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

public class ApkNode extends Object{
	public String name;
	public String apkid;
	public String ver;
	public int status;
	/*
	 * status
	 * 0 - not installed
	 * 1 - installed
	 * 2 - installed need update
	 */
	public float rat;
	public int down;
	public String catg;
	public int catg_ord;
	
	//Only used for update 
	public int vercode;
	
	public ApkNode(){
		
	}
	
	public ApkNode(String apkid, int vercode){
		this.apkid = apkid;
		this.vercode = vercode;
	}

	@Override
	public boolean equals(Object o) {
		if(o != null){
			ApkNode node = (ApkNode)o;
			if(this.apkid.equals(node.apkid))
				return true;
		}
		return false;
	}
}
