package com.tribeappsoft.leedo.salesPerson.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/*public class ImageAdapterFlatAllotment extends BaseAdapter {
    private Context mContext;

    ArrayList myList = new ArrayList();


public ImageAdapterFlatAllotment(Context c)
{
    mContext = c;
}
    @Override
    public int getCount() {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;

        if (view == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        }
        else
        {
            imageView = (ImageView) view;
        }
        imageView.setImageResource(mThumbIds[i]);
        return imageView;
    }

    // Keep all Images in array
    public Integer[] mThumbIds = {

    };
}*/



public class ImageAdapterFlatAllotment extends BaseAdapter
{
    private Context context;
    private ArrayList<String> itemArraylist;

    public ImageAdapterFlatAllotment(Context c, ArrayList<String> thePic)
    {
        context = c;
        itemArraylist = thePic;
    }
    public int getCount() {
        return  (null != itemArraylist ? itemArraylist.size() : 0);
    }

    //—returns the ID of an item—
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    //—returns an ImageView view—
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView;
        BitmapFactory.Options bfOptions=new BitmapFactory.Options();
        bfOptions.inDither=false;                     //Disable Dithering mode
        bfOptions.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
        bfOptions.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
        bfOptions.inTempStorage=new byte[32 * 1024];
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }
        FileInputStream fs = null;
        Bitmap bm;
        try {
            fs = new FileInputStream(new File(itemArraylist.get(position)));

            if(fs!=null) {
                bm=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
                imageView.setImageBitmap(bm);
                imageView.setId(position);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 160));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(fs!=null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imageView;
    }
}


