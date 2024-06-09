import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item
import com.handheld.exp.models.OptionItem

class ItemAdapter(private var items: List<Item>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OPTION = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_OPTION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.option_item_layout, parent, false)
                OptionViewHolder(view)
            }
            VIEW_TYPE_BUTTON -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.option_item_layout, parent, false)
                ButtonViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder.itemViewType) {
            VIEW_TYPE_OPTION -> {
                val optionHolder = holder as OptionViewHolder
                optionHolder.bind(item as OptionItem, position)
            }
            VIEW_TYPE_BUTTON -> {
                val buttonHolder = holder as ButtonViewHolder
                buttonHolder.bind(item as ButtonItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is OptionItem -> VIEW_TYPE_OPTION
            is ButtonItem -> VIEW_TYPE_BUTTON
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setItems(items: List<Item>){
        this.items = items
        notifyDataSetChanged()
    }

    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.label)
        private val valueTextView: TextView = itemView.findViewById(R.id.value)


        fun bind(optionItem: OptionItem, position: Int) {
            labelTextView.text = optionItem.label
            valueTextView.text = optionItem.getOption().label
            itemView.setOnClickListener {
                optionItem.nextOption()
                optionItem.notifyOnOptionChange()
            }
            itemView.setOnKeyListener { _, keyCode, event ->
                if (event?.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            optionItem.prevOption()
                            optionItem.notifyOnOptionChange()
                            return@setOnKeyListener true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            optionItem.nextOption()
                            optionItem.notifyOnOptionChange()
                            return@setOnKeyListener true
                        }
                    }
                }
                false
            }
        }
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.label)
        private val selectorView: View = itemView.findViewById(R.id.selector)

        fun bind(optionItem: ButtonItem) {
            labelTextView.text = optionItem.label
            selectorView.visibility = View.GONE
            itemView.setOnClickListener {
                optionItem.onClick()
            }
        }
    }
}
