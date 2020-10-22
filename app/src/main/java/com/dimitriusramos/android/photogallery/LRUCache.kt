package com.dimitriusramos.android.photogallery

import android.graphics.Bitmap
import androidx.collection.LruCache

class LRUCache private constructor() {

    private object HOLDER {
        val INSTANCE = LRUCache()
    }

    companion object {
        val instance: LRUCache by lazy { HOLDER.INSTANCE }
    }
    val lru: LruCache<Any, Any> = LruCache(1024)

    fun saveBitmapToCache(key: String, bitmap: Bitmap) {
        try {
            LRUCache.instance.lru.put(key, bitmap)
        } catch (e: Exception) {
        }

    }

    fun retrieveBitmapFromCache(key: String): Bitmap? {

        try {
            return LRUCache.instance.lru.get(key) as Bitmap?
        } catch (e: Exception) {
        }

        return null
    }

}