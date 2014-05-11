package edu.columbia.cvml.galleria.activity;

import java.io.File;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

/**
 * 
 * This will not work so great since the heights of the imageViews 
 * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get 
 * the (intrinsic) image width and height and set the views height manually. I will
 * look into a fix once I find extra time.
 * 
 * @author Maurycy Wojtowicz
 *
 */
@SuppressLint("NewApi")
public class MainActivity extends Activity {
	String LOG_TAG =  "MainActivity";

	/**
	 * This will not work so great since the heights of the imageViews 
	 * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get 
	 * the (intrinsic) image width and height and set the views height manually. I will
	 * look into a fix once I find extra time.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionbar = getActionBar();
		actionbar.setTitle("Galleria");
		actionbar.setSubtitle("When you love clicking!!!");
		actionbar.setHomeButtonEnabled(true);
		StaggeredGridView gridView = (StaggeredGridView) this.findViewById(R.id.staggeredGridView1);

		int margin = getResources().getDimensionPixelSize(R.dimen.margin);


		//StaggeredAdapter adapter = new StaggeredAdapter(MainActivity.this, R.id.imageView1, urls);

		gridView.setItemMargin(margin); // set the GridView margin
		gridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well 


		//String ExternalStorageDirectoryPath = Environment.getDataDirectory().getAbsolutePath();
	
		Uri extUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		
		String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.Images.Media.DATA,
				MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns._ID };
		Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), extUri, projection, null, MediaStore.MediaColumns.DATE_ADDED + " asc");
		String[] imagePath = new String[cursor.getCount()];
		int index = 0;
		while (cursor.moveToNext()) 
		{
			imagePath[index] = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			Log.d(LOG_TAG, "Image = " + imagePath[index]);
			index +=1;
		}
		cursor.close();

		/*String targetPath = ExternalStorageDirectoryPath + "/DCIM/Camera/";

		Toast.makeText(getApplicationContext(), targetPath, Toast.LENGTH_LONG).show();
		File targetDirector = new File(targetPath);
		File[] files = targetDirector.listFiles();
		shuffleArray(files);
		String[] uri = new String[files.length];
		int index =0;

		for (File file: files)
		{		
			uri[index] = file.getAbsolutePath();
			index +=1;
		}*/

		CustomStaggeredAdapter adapter = new CustomStaggeredAdapter(MainActivity.this, R.id.imageView1, imagePath);
		gridView.setAdapter(adapter);

		//Intent i = new Intent(this, DisplayImageActivity.class);
		gridView.setOnItemClickListener(new OnItemClickListener() {            	  
			@Override
			public void onItemClick(StaggeredGridView parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(), "You clicked it, yay", Toast.LENGTH_LONG).show();

				Intent i = new Intent(MainActivity.this, DisplayImageActivity.class);
				String filepath = (String) parent.getAdapter().getItem(position);
				i.putExtra("filepath", filepath);
				startActivity(i);
			}

		}); 

		adapter.notifyDataSetChanged();




		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{         
		switch (item.getItemId())
		{
		case R.id.DebugScreen:
			Toast.makeText(MainActivity.this, "DebugScreen is selected", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, DebugActivity.class);
            startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}    

	private void shuffleArray(Object[] array)
	{
		int index; 
		Object temp;
		Random random = new Random();
		for (int i = array.length - 1; i > 0; i--)
		{
			index = random.nextInt(i + 1);
			temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
	}
}
