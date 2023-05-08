package com.bignerdranch.android.popuptrip.ui.dashboard

class Itinerary(id:String,it_name:String,create_time:String,content:String){
    var itineraryName:String=""
    var createTime:String=""
    var itineraryId: String=""
    var itineraryContent:String = ""

    init {
        this.itineraryId=id
        this.itineraryName=it_name
        this.createTime=create_time
        this.itineraryContent=content
    }
}