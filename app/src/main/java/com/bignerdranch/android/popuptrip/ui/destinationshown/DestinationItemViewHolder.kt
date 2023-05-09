import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.databinding.DestinationItemBinding
import com.bignerdranch.android.popuptrip.ui.destinationshown.DestinationItem

class DestinationItemViewHolder (val binding: DestinationItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(currentItem: DestinationItem, isLastItem: Boolean) {
        binding.tvDestinationName.text = currentItem.name

        if (!isLastItem) {
            binding.tvTimeToNext.text = currentItem.timeToNextLocation
            binding.tvArrow.visibility = View.VISIBLE
            binding.tvContent.text=currentItem.step
        } else {
            binding.tvTimeToNext.text = ""
            binding.tvArrow.visibility = View.GONE
            binding.tvContent.text=""
        }
    }
}