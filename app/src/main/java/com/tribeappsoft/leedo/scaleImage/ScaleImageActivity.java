package com.tribeappsoft.leedo.scaleImage;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tribeappsoft.leedo.R;
import com.tribeappsoft.leedo.util.TouchImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tribeappsoft.leedo.util.Helper.StausBarTransp;
import static com.tribeappsoft.leedo.util.Helper.isValidContextForGlide;


public class ScaleImageActivity extends AppCompatActivity {

    @BindView(R.id.iv_scaleImage_close)
    AppCompatImageView iv_close;
    @BindView(R.id.tv_scaleImage_eventTitle)
    AppCompatTextView tv_eventTitle;
    @BindView(R.id.iv_scaleImage_touchThumbnail)
    TouchImageView iv_scaleImage_touchThumbnail;

    private String banner_path  = "", event_title="";
    private int from_adapter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_image);
        ButterKnife.bind(this);
        AppCompatActivity context = ScaleImageActivity.this;

        if (getIntent()!=null)
        {
            banner_path = getIntent().getStringExtra("banner_path");
            event_title = getIntent().getStringExtra("event_title");
            from_adapter = getIntent().getIntExtra("from_adapter", 0);
        }

        //set status bar color black
        StausBarTransp(context);

        //close activity
        iv_close.setOnClickListener(view -> onBackPressed());

        //set title
        tv_eventTitle.setText(event_title!=null ? event_title : "");

        //set Image to imageView

        if (isValidContextForGlide(context))
        {
            Glide.with(context)//getActivity().this
                    .load(banner_path)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .apply(new RequestOptions().fitCenter())
                    .apply(new RequestOptions().placeholder(from_adapter==1 ? R.drawable.ic_user : R.color.main_black))
                    .apply(new RequestOptions().error(from_adapter==1 ? R.drawable.ic_user : R.color.main_black))
                    .into(iv_scaleImage_touchThumbnail);
        }

       /* try {
            Bitmap bitmap =   new getBitmapAsync().execute(banner_path).get();
            //iv_scaleImage_thumbnail.setImage(banner_path!=null ?  ImageSource.uri(banner_path) : ImageSource.resource(R.drawable.gif_animated_gallery));
            iv_scaleImage_thumbnail.setImage(banner_path!=null ?  ImageSource.bitmap(bitmap) : ImageSource.resource(R.drawable.gif_animated_gallery));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
