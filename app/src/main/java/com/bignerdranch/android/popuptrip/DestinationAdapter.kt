import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.Destination
import com.bignerdranch.android.popuptrip.R
import kotlinx.android.synthetic.main.item_destination.view.*

class DestinationAdapter(private val destinations: List<Destination>) :
    RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(destination: Destination) {
            itemView.destination_name.text = destination.name
            itemView.destination_description.text = destination.description
            itemView.imageView.setImageResource(destination.imageResource)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount() = destinations.size
}
