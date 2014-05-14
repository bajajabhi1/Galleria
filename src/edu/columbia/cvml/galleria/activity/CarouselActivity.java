package edu.columbia.cvml.galleria.activity;
/*
 * 3D carousel View
 * http://www.pocketmagic.net 
 *
 * Copyright (c) 2013 by Radu Motisan , radu.motisan@gmail.com
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
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * For more information on the GPL, please go to:
 * http://www.gnu.org/copyleft/gpl.html
 *
 */ 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import edu.columbia.cvml.galleria.carousel.AppUtils;
import edu.columbia.cvml.galleria.carousel.CarouselDataItem;
import edu.columbia.cvml.galleria.carousel.CarouselView;
import edu.columbia.cvml.galleria.carousel.Singleton;
import edu.columbia.cvml.galleria.util.ClusterFeatureManager;

public class CarouselActivity extends Activity implements TextWatcher, OnItemClickListener {//,OnItemSelectedListener{
	
	Singleton 				m_Inst 					= Singleton.getInstance();
	CarouselViewAdapter 	m_carouselAdapter		= null;	 
	private final int		m_nFirstItem			= 1000;
	String LOG_TAG =  "CarouselActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //no keyboard unless requested by user
      	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
      		
        // compute screen size and scaling
     	Singleton.getInstance().InitGUIFrame(this);
     	
     	int padding = m_Inst.Scale(10);
		// create the interface : full screen container
		RelativeLayout panel  = new RelativeLayout(this);
	    panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		panel.setPadding(padding, padding, padding, padding);
	    panel.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, 
	    		new int[]{Color.WHITE, Color.GRAY}));
	    setContentView(panel); 
	    
	   //Create carousel view documents
	    ArrayList<CarouselDataItem> Docus = new ArrayList<CarouselDataItem>();
	    
	    Intent i = getIntent();		
	    ArrayList<String> filepath = i.getStringArrayListExtra("filepath");
	    Log.d(LOG_TAG, "Image List Size = " + filepath.size());
		int index = 0;
		CarouselDataItem docu;
		Iterator<String> itr = filepath.iterator();
		// load the image feature map
		//ClusterFeatureManager cfm = new ClusterFeatureManager(getApplicationContext());
		ClusterFeatureManager cfm = ClusterFeatureManager.getInstance(getApplicationContext());
		
		Map<String,String> imageFeatureMap = cfm.loadImageFeatureMap();
		while (itr.hasNext()) 
		{
			String path = itr.next();
			Log.d(LOG_TAG, "Image in Carousel = " + path);
			String imageName = path.substring(path.lastIndexOf("/")+1,path.length());
			Log.d(LOG_TAG, "Image name = " + imageName);
			if(imageFeatureMap.get(imageName) == null)
				docu = new CarouselDataItem(path, 0,"No annotations found for this image");
			else
				docu = new CarouselDataItem(path, 0, imageFeatureMap.get(imageName));
			Docus.add(docu);
			index +=1;
		}
		//cursor.close();	    
	    
	    // add the serach filter
	    EditText etSearch = new EditText(this);
	    etSearch.setHint("Search your image");
	    etSearch.setSingleLine();
	    etSearch.setTextColor(Color.BLACK);
	    etSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_search, 0, 0, 0);
	    AppUtils.AddView(panel, etSearch, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
	    		new int[][]{new int[]{RelativeLayout.CENTER_HORIZONTAL}, new int[]{RelativeLayout.ALIGN_PARENT_TOP}}, -1,-1);
	    etSearch.addTextChangedListener((TextWatcher) this); 
    
	    // create the carousel
	    CarouselView coverFlow = new CarouselView(this);
        
	    // create adapter and specify device independent items size (scaling)
	    m_carouselAdapter =  new CarouselViewAdapter(this,Docus, m_Inst.Scale(400),m_Inst.Scale(300));
        coverFlow.setAdapter(m_carouselAdapter);
        coverFlow.setSpacing(-1*m_Inst.Scale(150));
        coverFlow.setSelection(Integer.MAX_VALUE / 2, true);
        coverFlow.setAnimationDuration(1000);
        //coverFlow.setOnItemSelectedListener((OnItemSelectedListener) this);
        coverFlow.setOnItemClickListener((OnItemClickListener) this);

        AppUtils.AddView(panel, coverFlow, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT, 
        		new int[][]{new int[]{RelativeLayout.CENTER_IN_PARENT}},
        		-1, -1); 
    }

	public void afterTextChanged(Editable arg0) {}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		m_carouselAdapter.getFilter().filter(s.toString()); 
	}

	public void onNothingSelected(AdapterView<?> arg0) {}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		CarouselDataItem docu =  (CarouselDataItem) m_carouselAdapter.getItem((int) arg3);
		 if (docu!=null)
		 {
			
			 Intent i = new Intent(CarouselActivity.this, DisplayImageActivity.class);
			 i.putExtra("filepath", docu.getImgPath());
			 System.gc();
			 startActivity(i);
		 }
		
	}   
}
