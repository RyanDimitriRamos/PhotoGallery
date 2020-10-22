package com.dimitriusramos.android.photogallery.api

import com.dimitriusramos.android.photogallery.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}