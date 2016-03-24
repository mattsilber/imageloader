/*
   Copyright 2013 Paul LeBeau, Cave Rock Software Ltd.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.guardanis.imageloader.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;
import com.guardanis.imageloader.ImageFileRequest;
import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.guardanis.imageloader.R;

/**
 * Edited / Dumbed-down version of com.caverock.androidsvg.SVGImageView
 */
public class SVGImageView extends ImageView {

	private interface LayoutEventListener {
		public void onLaidOut(int width);
	}

	public SVGImageView(Context context) {
		super(context);
		disableHardwareAcceleration(this);
	}

	public SVGImageView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		disableHardwareAcceleration(this);

		init(attrs, 0);
	}

	public SVGImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		disableHardwareAcceleration(this);

		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SVGImageView, defStyle, 0);
		try {
			int resourceId = a.getResourceId(R.styleable.SVGImageView_svg, -1);
			if(resourceId != -1) {
				setImageResource(resourceId);
				return;
			}

			String url = a.getString(R.styleable.SVGImageView_svg);

			if(url != null)
				setImageAsset(url);
		}
		finally { a.recycle(); }
	}

	/**
	 * Load an SVG image from the given filename in the assets directory.
	 */
	public void setImageAsset(String filename){
		try	{
			if(filename.length() < 1)
				setImageDrawable(null);
			else setImageSvg(SVG.getFromAsset(getContext().getAssets(), filename));
		}
		catch (Throwable e) {
			ImageUtils.log(getContext(), "Unable to find asset file: " + filename);

			setImageDrawable(null);
		}
	}

    /**
     * Load an SVG image from an external URL.
     */
    public void setImageUrl(String url){
        new ImageRequest<SVGImageView>(getContext(), url)
                .setTargetView(this)
                .setFadeTransition()
                .execute();
    }

    /**
     * Load an SVG image from a file on the device.
     */
    public void setImageFile(String imageFile){
        new ImageFileRequest<SVGImageView>(getContext(), imageFile)
                .setTargetView(this)
                .setFadeTransition()
                .execute();
    }

	private void setImageSvg(final SVG svg) throws Throwable {
		getBestWidth(new LayoutEventListener() {
			public void onLaidOut(int width) {
				setImageBitmap(ImageUtils.decodeBitmap(svg, width));
			}
		});
	}

	private void getBestWidth(final LayoutEventListener eventListener){
		if(getLayoutParams() == null) {
			this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				public void onGlobalLayout() {
					eventListener.onLaidOut(getLayoutParams().width);
					removeOnGlobalLayoutListener(SVGImageView.this, this);
				}
			});
			requestLayout();
		}
		else eventListener.onLaidOut(getLayoutParams().width);
	}

	@SuppressLint("NewApi")
	public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
		if (Build.VERSION.SDK_INT < 16)
			v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		else v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
	}

	@SuppressLint("NewApi")
	public static void disableHardwareAcceleration(View v){
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

}
