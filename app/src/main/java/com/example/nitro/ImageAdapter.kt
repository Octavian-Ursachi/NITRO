package com.example.nitro

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import java.util.Objects

class ImageAdapter(context : Context) : PagerAdapter() {

    private var mContext : Context = context
    private val mImageIds: IntArray = intArrayOf(R.drawable.cedeaza_trecerea, R.drawable.drum_cu_prioritate, R.drawable.oprire, R.drawable.sens_unic, R.drawable.trecere_de_pietoni)

    override fun getCount(): Int {
        return mImageIds.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var imageView : ImageView = ImageView(mContext)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(mImageIds[position])
        container.addView(imageView,0)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }
}