package com.bignerdranch.android.popuptrip.ui.dashboard

class Itinerary(id:String,it_name:String,content:String){
    var itineraryName:String=""
    var createTime:String=""
    var itineraryId: String=""
    var itineraryContent:String = ""

    init {
        this.itineraryId=id
        this.itineraryName=it_name
        this.itineraryContent=content
    }
}