package de.madem.homium.utilities.pictures

import android.graphics.Bitmap
import androidx.collection.LruCache

class ImageCache(maxSize : Int) : LruCache<String, Bitmap>(maxSize) {
    override fun sizeOf(key: String, value: Bitmap): Int {
        return value.byteCount
    }
}

