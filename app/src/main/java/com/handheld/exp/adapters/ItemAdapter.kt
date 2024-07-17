import android.annotation.SuppressLint
import android.opengl.Visibility
import android.util.SparseArray
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.traceEventEnd
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.models.OptionItem

class ItemAdapter(
    private var items: List<Item>,
    private val navigationHandler: NavigationHandler
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OPTION = 0
        private const val VIEW_TYPE_BUTTON = 1
        private const val VIEW_TYPE_NAVIGATION = 2
    }

    init {
        setItems(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_OPTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.option_item_layout, parent, false)
                OptionViewHolder(view)
            }

            VIEW_TYPE_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.option_item_layout, parent, false)
                ButtonViewHolder(view)
            }

            VIEW_TYPE_NAVIGATION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.option_item_layout, parent, false)
                NavigationViewHolder(view)
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

            VIEW_TYPE_NAVIGATION -> {
                val navigationHolder = holder as NavigationViewHolder
                navigationHolder.bind(item as NavigationItem)
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
            is NavigationItem -> VIEW_TYPE_NAVIGATION
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setItems(items: List<Item>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.label)
        private val valueTextView: TextView = itemView.findViewById(R.id.value)
        private val arrowView: View = itemView.findViewById(R.id.arrow)

        fun bind(optionItem: OptionItem, position: Int) {
            labelTextView.text = optionItem.label
            arrowView.visibility = View.GONE
            valueTextView.text = optionItem.getOption().label

            setDefaultListeners(itemView,
                onClick = {
                    optionItem.nextOption()
                    optionItem.notifyOnOptionChange()
                },
                onKeyDown = {
                    when (it) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            optionItem.prevOption()
                            optionItem.notifyOnOptionChange()
                            return@setDefaultListeners true
                        }

                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            optionItem.nextOption()
                            optionItem.notifyOnOptionChange()
                            return@setDefaultListeners true
                        }

                        KeyEvent.KEYCODE_BACK -> {
                            navigationHandler.onNavigateBack()
                            return@setDefaultListeners true
                        }

                        else -> false
                    }
                }
            )
        }
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.label)
        private val selectorView: View = itemView.findViewById(R.id.selector)
        private val arrowView: View = itemView.findViewById(R.id.arrow)

        fun bind(optionItem: ButtonItem) {
            labelTextView.text = optionItem.label
            selectorView.visibility = View.GONE
            arrowView.visibility = View.GONE

            setDefaultListeners(itemView,
                onClick = { optionItem.onClick() },
                onKeyDown = { false }
            )
        }
    }

    inner class NavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.label)
        private val selectorView: View = itemView.findViewById(R.id.selector)

        fun bind(navigationItem: NavigationItem) {
            labelTextView.text = navigationItem.label
            selectorView.visibility = View.GONE

            setDefaultListeners(itemView,
                onClick = { navigationHandler.onNavigateTo(navigationItem) },
                onKeyDown = { false }
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setDefaultListeners(
        view: View,
        onClick: () -> Unit,
        onKeyDown: (keyCode: Int) -> Boolean
    ) {
        view.setOnClickListener {
            onClick()
        }

        view.setOnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        navigationHandler.onNavigateBack()
                        return@setOnKeyListener true
                    }
                }
                return@setOnKeyListener onKeyDown(keyCode)
            }
            false
        }
    }

    interface NavigationHandler {
        fun onNavigateTo(navigationItem: NavigationItem)
        fun onNavigateBack()
    }
}
