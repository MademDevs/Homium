package de.madem.homium.utilities.pictures

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import java.io.File

object BitmapUtil {

    val imageCache : ImageCache = ImageCache(HomiumApplication.getAvailableApplicationMemory()/8)

    fun loadBitmapFromPath(path: String?, reqWidth: Int = 400, reqHeight: Int = 400): Bitmap {
        try {
            if (path.isNullOrEmpty()) {
                return BitmapFactory.decodeResource(HomiumApplication.appContext!!.resources,R.mipmap.empty_picture)
            }

            val imageFile = File(path)

            if(!(imageFile).exists()){
                return BitmapFactory.decodeResource(HomiumApplication.appContext!!.resources,R.mipmap.empty_picture)
            }

            return BitmapFactory.Options().run {

                inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, this)

                // Calculate inSampleSize
                inSampleSize =
                    calculateInSampleSize(
                        this,
                        reqWidth,
                        reqHeight
                    )

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                BitmapFactory.decodeFile(path, this)
            }
        }
        catch (ex : Exception){
            return BitmapFactory.decodeResource(HomiumApplication.appContext!!.resources,R.mipmap.empty_picture)
        }

    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {

        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }


}
