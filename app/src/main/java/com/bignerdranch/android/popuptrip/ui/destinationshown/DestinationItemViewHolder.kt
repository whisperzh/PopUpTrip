import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.popuptrip.databinding.DestinationItemBinding
import com.bignerdranch.android.popuptrip.ui.destinationshown.DestinationItem
import kotlin.math.exp


class DestinationItemViewHolder (val binding: DestinationItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var expand=true
    fun bind(currentItem: DestinationItem, isLastItem: Boolean) {
        binding.tvDestinationName.text = currentItem.name
        binding.tvArrow.setOnClickListener {

            val lp=binding.tvContent.layoutParams
            lp.height=0
            expand=!expand
            if(expand)
                lp.height=ViewGroup.LayoutParams.WRAP_CONTENT
            binding.tvContent.layoutParams=lp
        }
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