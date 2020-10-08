package com.dimitriusramos.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel: ViewModel() {
    val galleryItemLiveData: LiveData<List<GalleryItem>>
    init{
        // calling the flickrFetchr here so it can persist across rotation
        galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }
}