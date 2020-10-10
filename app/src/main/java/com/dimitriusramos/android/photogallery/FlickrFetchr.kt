package com.dimitriusramos.android.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dimitriusramos.android.photogallery.api.FlickrApi
import com.dimitriusramos.android.photogallery.api.FlickrResponse
import com.dimitriusramos.android.photogallery.api.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {
    private val flickrApi: FlickrApi
    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }
    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()
        flickrRequest.enqueue(object : Callback<FlickrResponse>
        {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.d(TAG, "Response received")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                val galleryItems1: List<GalleryItem>? = photoResponse?.galleryItems
                //Check if the gallery list item from the photo response is a valid response. If it isn't then set it to an
                // an empty mutable list
                var galleryItems: List<GalleryItem> = if (galleryItems1 != null) galleryItems1 else mutableListOf()
                    galleryItems = galleryItems.filterNot {
                        it.url.isBlank()
                    }
                responseLiveData.value = galleryItems
            }
        })
        return responseLiveData
    }

    /* WorkerThread indicates that this function should only be called on a background process.
     * This doesn't place the fetchPhoto function on a background process or create one, but
     * it does create a lint error.
     * Here this fetches the url and creates a bitmap image.
     */
    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}