/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.instructure.candroid.R;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.util.PandaDrawables;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;


public class PandaAvatarActivity extends ParentActivity {

    private enum BODY_PART { HEAD, BODY, LEGS}
    private ImageView head;
    private ImageView body;
    private ImageView legs;

    private Button changeHead;
    private Button changeBody;
    private Button changeLegs;

    private RelativeLayout partsView;
    private LinearLayout partsLayout;
    private Button backButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.pandaAvatar);
        setupViews();
        setupListeners();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.panda_avatar_create, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public int contentResId() {
        return R.layout.panda_image;
    }

    @Override
    public boolean showHomeAsUp() {
        return true;
    }

    @Override
    public boolean showTitleEnabled() {
        return true;
    }

    @Override
    public void onUpPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item != null) {
            switch (item.getItemId()) {
                case R.id.menu_item_save_image:
                    if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    saveImageAsPNG(true, Color.TRANSPARENT);
                    break;
                case R.id.menu_item_set_avatar:
                    if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    setAsAvatar();
                    break;
                case R.id.menu_item_share:
                    if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    File file = saveImageAsPNG(false, Color.TRANSPARENT);
                    Intent shareIntent = getShareIntent(file);
                    if(shareIntent != null) {
                        startActivity(shareIntent);
                    }
                    break;
            }

        }
        return super.onOptionsItemSelected(item);
    }


    // Call to update the share intent
    private Intent getShareIntent(File file) {
        if(file == null) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        return intent;
    }

    private void setupViews() {
        head = (ImageView) findViewById(R.id.imageHead);
        body = (ImageView) findViewById(R.id.imageBody);
        legs = (ImageView) findViewById(R.id.imageLegs);

        changeHead = (Button) findViewById(R.id.changeHead);
        changeBody = (Button) findViewById(R.id.changeBody);
        changeLegs = (Button) findViewById(R.id.changeLegs);

        //make the head and body all black
        changeHead.setBackgroundDrawable(CanvasContextColor.getColoredDrawable(PandaAvatarActivity.this, R.drawable.pandify_head_02, Color.BLACK));
        changeBody.setBackgroundDrawable(CanvasContextColor.getColoredDrawable(PandaAvatarActivity.this, R.drawable.pandify_body_11, Color.BLACK));

        partsView = (RelativeLayout) findViewById(R.id.partsOptions);
        partsLayout = (LinearLayout) findViewById(R.id.partsContainer);

        backButton = (Button) findViewById(R.id.backButton);

        loadBodyParts();
    }


    //if the user has changed the body parts we should remember and load those parts
    private void loadBodyParts() {
        if(loadPart(BODY_PART.HEAD) != -1) {
            head.setImageDrawable(getResources().getDrawable(loadPart(BODY_PART.HEAD)));
        } else {
            head.setImageDrawable(getResources().getDrawable(R.drawable.pandify_head_02));
        }

        if(loadPart(BODY_PART.BODY) != -1) {
            body.setImageDrawable(getResources().getDrawable(loadPart(BODY_PART.BODY)));
        } else {
            body.setImageDrawable(getResources().getDrawable(R.drawable.pandify_body_2));
        }

        if(loadPart(BODY_PART.LEGS) != -1) {
            legs.setImageDrawable(getResources().getDrawable(loadPart(BODY_PART.LEGS)));
        } else {
            legs.setImageDrawable(getResources().getDrawable(R.drawable.pandify_feet_2));
        }
    }

    private void setupListeners() {

        changeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPartsMenu();
                addHeads();
            }
        });

        changeBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPartsMenu();
                addBodies();
            }
        });

        changeLegs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPartsMenu();
                addLegs();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideDown();
            }
        });
    }

    private void setAsAvatar() {
        File file = saveImageAsPNG(false, getResources().getColor(R.color.canvasBackgroundMedium));
        if(file == null) {
            //something went wrong
            Toast.makeText(getContext(), R.string.errorSavingAvatar, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(Const.PATH, file.getPath());
        data.putExtra(Const.SIZE, file.length());

        setResult(RESULT_OK, data);
        finish();
    }

    private File saveImageAsPNG(boolean showSuccessMsg, int color) {

        int padding = 16;

        //we can set the density value to be bigger to make the images smaller if necessary
        float density = 1;

        //make the bitmap as wide as the widest body part + padding
        int width = Math.max(Math.max((int)(head.getDrawable().getIntrinsicWidth()/density), (int)(body.getDrawable().getIntrinsicWidth()/density)), (int)(legs.getDrawable().getIntrinsicWidth()/density)) + padding;
        //make the bitmap as high as all the body parts together
        int height = (int)(head.getDrawable().getIntrinsicHeight()/density) + (int)(body.getDrawable().getIntrinsicHeight()/density) + (int)(legs.getDrawable().getIntrinsicHeight()/density) + padding;

        //create a bitmap to contain all of our images
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        Paint transPainter = new Paint();
        if(color == Color.TRANSPARENT) {
            transPainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRect(0, 0, background.getWidth(), background.getHeight(), transPainter);
        } else {
            transPainter.setColor(color);
            canvas.drawRect(0, 0, background.getWidth(), background.getHeight(), transPainter);
        }

        int y = 8;

        Bitmap headBmp = Bitmap.createScaledBitmap(((BitmapDrawable) head.getDrawable()).getBitmap(), ((int)(head.getDrawable().getIntrinsicWidth() / density)), (int)(head.getDrawable().getIntrinsicHeight() / density), false);
        //try to center the images on the screen horizontally
        int x = (width - ((int)(head.getDrawable().getIntrinsicWidth() / density)))/2;
        canvas.drawBitmap(headBmp, x, y, null);
        //increment the y so we know where to draw the next body part
        y += head.getDrawable().getIntrinsicHeight()/density;

        Bitmap bodyBmp = Bitmap.createScaledBitmap(((BitmapDrawable) body.getDrawable()).getBitmap(), ((int)(body.getDrawable().getIntrinsicWidth() / density)), (int)(body.getDrawable().getIntrinsicHeight() / density), false);
        x = (width - ((int)(body.getDrawable().getIntrinsicWidth() / density)))/2;
        canvas.drawBitmap(bodyBmp, x, y, null);
        y += body.getDrawable().getIntrinsicHeight()/density;

        Bitmap legsBmp = Bitmap.createScaledBitmap(((BitmapDrawable) legs.getDrawable()).getBitmap(), ((int)(legs.getDrawable().getIntrinsicWidth() / density)), (int)(legs.getDrawable().getIntrinsicHeight() / density), false);
        x = (width - ((int)(legs.getDrawable().getIntrinsicWidth() / density)))/2;
        canvas.drawBitmap(legsBmp, x, y, null);


        //save the bitmap as a png
        OutputStream os = null;
        File picFile = null;

        try {
            File root = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), getString(R.string.pandaAvatarsFolderName));
            if (!root.mkdirs()) {
                LoggingUtility.LogCrashlytics("File create failed for Panda Avatar");
            }
            if (root.canWrite()) {
                picFile = new File(root, generateFileName());
                os = new FileOutputStream(picFile);
                background.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if(showSuccessMsg) {
            Toast.makeText(getContext(), R.string.avatarSuccessfullySaved, Toast.LENGTH_SHORT).show();
        }
        if(picFile != null) {
            return picFile;
        }
        else {
            return null;
        }
    }

    public String generateFileName() {
        Date date = new Date();

        return "Pandafy_" + date.getTime() + ".png";
    }

    private void showPartsMenu() {
        partsView.setVisibility(View.VISIBLE);
        slideUp();
        partsLayout.removeAllViewsInLayout();
    }



    public void slideUp() {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                5.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        slide.setDuration(400);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        partsView.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                partsView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                partsView.clearAnimation();

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        partsView.getWidth(), partsView.getHeight());
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                partsView.setLayoutParams(lp);

            }

        });

    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //  Animations
    //////////////////////////////////////////////////////////////////////////////////////////

    public void slideDown() {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

        slide.setDuration(400);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        partsView.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                partsView.clearAnimation();

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        partsView.getWidth(), partsView.getHeight());
                lp.setMargins(0, partsView.getWidth(), 0, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                partsView.setLayoutParams(lp);
                partsView.setVisibility(View.INVISIBLE);
            }

        });

    }
    public static void ImageViewAnimatedChange(Context c, final ImageView v, final int new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.shrink_to_middle);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.expand_from_middle);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageResource(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    private void addHeads() {
        List<Integer> heads = PandaDrawables.getHeads();
        for(final Integer res : heads) {
            ImageView ii= new ImageView(this);
            ii.setBackgroundResource(res);
            ii.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageViewAnimatedChange(PandaAvatarActivity.this, head, res);
                    savePart(BODY_PART.HEAD, res);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.scrollview_image_size), (int)getResources().getDimension(R.dimen.scrollview_image_size));
            layoutParams.setMargins((int)getResources().getDimension(R.dimen.scrollview_image_margin),0,(int)getResources().getDimension(R.dimen.scrollview_image_margin),0);
            ii.setLayoutParams(layoutParams);

            partsLayout.addView(ii);
        }
    }

    private void addBodies() {
        List<Integer> bodies = PandaDrawables.getBodies();
        for(final Integer res : bodies) {
            ImageView ii= new ImageView(this);
            ii.setBackgroundResource(res);
            ii.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageViewAnimatedChange(PandaAvatarActivity.this, body, res);
                    savePart(BODY_PART.BODY, res);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.panda_body_width), (int)getResources().getDimension(R.dimen.scrollview_image_size));
            layoutParams.setMargins((int)getResources().getDimension(R.dimen.scrollview_image_margin),0,(int)getResources().getDimension(R.dimen.scrollview_image_margin),0);
            ii.setLayoutParams(layoutParams);
            partsLayout.addView(ii);
        }
    }

    private void addLegs() {
        List<Integer> legsList = PandaDrawables.getLegs();
        for(final Integer res : legsList) {
            ImageView ii= new ImageView(this);
            ii.setBackgroundResource(res);
            ii.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageViewAnimatedChange(PandaAvatarActivity.this, legs, res);
                    savePart(BODY_PART.LEGS, res);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.panda_feet_width), (int)getResources().getDimension(R.dimen.panda_feet_height));
            layoutParams.setMargins((int)getResources().getDimension(R.dimen.scrollview_image_margin),20,(int)getResources().getDimension(R.dimen.scrollview_image_margin),0);
            ii.setLayoutParams(layoutParams);
            partsLayout.addView(ii);
        }
    }

    private void savePart(BODY_PART part, int id) {
        SharedPreferences preferences = getSharedPreferences(Const.NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(part.toString(), id).apply();
    }

    private int loadPart(BODY_PART part) {
        SharedPreferences preferences = getSharedPreferences(Const.NAME, Context.MODE_PRIVATE);
        return preferences.getInt(part.toString(), -1);
    }


}
