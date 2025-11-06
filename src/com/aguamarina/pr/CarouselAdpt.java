/*
 * Copyright (C) 2023-2025 TropicalBananas
 * aguamarina@altervista.org
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
package com.aguamarina.pr;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class CarouselAdpt extends BaseAdapter {

	int CarouselBackground;
	private Context ctx;
	private List<Map<String, Object>> AppList = null;
	
	public CarouselAdpt(Context c, List<Map<String, Object>> AppList) {
    	ctx = c;
    	this.AppList = AppList;
        TypedArray a = c.obtainStyledAttributes(R.styleable.Carousel);
        CarouselBackground = a.getResourceId(
                R.styleable.Carousel_android_galleryItemBackground, 0);
        a.recycle();
    }
	
	public int getCount() {
		return AppList.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imgview = new ImageView(ctx);
		String icnpath = AppList.get(position).get("icon").toString();
		File icn = new File(icnpath);
		
		if(icn.exists() && icn.length() > 0){
			new Uri.Builder().build();
			imgview.setImageURI(Uri.parse(icnpath));
     	}else{
     		imgview.setImageResource(R.drawable.loadingicon);
     	}
		imgview.setLayoutParams(new Gallery.LayoutParams(95, 95));
		imgview.setScaleType(ImageView.ScaleType.FIT_XY);
		imgview.setBackgroundResource(CarouselBackground);
		
		return imgview;
	}

}
