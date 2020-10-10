package com.dimitriusramos.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap
import android.util.LruCache


private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0 // used to identify messages as download requests
class ThumbnailDownloader<in T>(private val responseHandler: Handler, private val onThumbnailDownloaded: (T, Bitmap) -> Unit) : HandlerThread(TAG){
    val fragmentLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup() {
                Log.i(TAG, "Starting background thread")
                start()
                looper
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                quit()
            }
        }

    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
    }
    private var hasQuit = false
    private lateinit var requestHandler: Handler // store a reference to the Handler responsible for queueing download requests
    /* The identifying object T is being used as the key. You can store and retrieve the URL associated with a particular request.
     * In this particular case the object is a PhotoHolder so the request response can be used to easily route back to the UI element
     */
    private val requestMap = ConcurrentHashMap<T, String>() //using a thread safe version of hashmap
    private val flickrFetchr = FlickrFetchr()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean{
        hasQuit = true
        return super.quit()
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread")
        start()
        looper
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread")
        quit()
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        if(url == null){
            requestMap.remove(target)
        }else{
            requestMap[target] = url
            val bitmap: Bitmap? = LRUCache.instance.retrieveBitmapFromCache(url)
            if(bitmap != null){
                Log.i(TAG, "Got a URL: $url from cache")
                onThumbnailDownloaded(target, bitmap)
                return
            }
        }
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    fun clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD)
        requestMap.clear()
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
        LRUCache.instance.saveBitmapToCache(url, bitmap)
        responseHandler.post(Runnable {
            if (requestMap[target] != url || this.hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }



}