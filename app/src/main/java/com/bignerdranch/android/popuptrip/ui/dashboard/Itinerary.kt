package com.bignerdranch.android.popuptrip.ui.dashboard

class Itinerary(id:String,it_name:String,create_time:String){
    var itineraryName:String=""
    var createTime:String=""
    var itineraryId: String=""

    init {
        this.itineraryId=id
        this.itineraryName=it_name
        this.createTime=create_time
    }
}