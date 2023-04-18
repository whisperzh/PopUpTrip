package com.bignerdranch.android.popuptrip.ui.home
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction

class PlacesAutoCompleteAdapter(context: Context, private val predictions: List<AutocompletePrediction>) :
    ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_expandable_list_item_2, predictions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_2, parent, false)

        val prediction = getItem(position)
        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        textView1.text = prediction?.getPrimaryText(null)
        textView2.text = prediction?.getSecondaryText(null)

        return view
    }
}