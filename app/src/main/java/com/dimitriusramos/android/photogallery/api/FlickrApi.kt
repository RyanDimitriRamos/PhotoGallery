package com.dimitriusramos.android.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {
    @GET("services/rest/? method=flickr.interestingness.getList"
            + "&api_key=098d670c6e49ce53e311b39a42e3f11b"
            + "&format=json"
            + "&nojsoncallback=1"
            + "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>
}