import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.models.OptionItem
import com.handheld.exp.models.TextItem

class ItemAdapter(
    private var items: List<Item>,
    private val navigationHandler: NavigationHandler
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OPTION = 0
        private const val VIEW_TYPE_BUTTON = 1
        private const val VIEW_TYPE_NAVIGATION = 2
        private const val VIEW_TYPE_TEXT = 3
    }

    init {
        setItems(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.common_item_layout, parent, false)

        return when (viewType) {
            VIEW_TYPE_OPTION -> OptionViewHolder(view)
            VIEW_TYPE_BUTTON -> ButtonViewHolder(view)
            VIEW_TYPE_NAVIGATION -> NavigationViewHolder(view)
            VIEW_TYPE_TEXT -> TextViewHolder(view)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder.itemViewType) {
            VIEW_TYPE_OPTION -> {
                val optionHolder = holder as OptionViewHolder
                optionHolder.bind(item as OptionItem)
            }

            VIEW_TYPE_BUTTON -> {
                val buttonHolder = holder as ButtonViewHolder
                buttonHolder.bind(item as ButtonItem)
            }

            VIEW_TYPE_NAVIGATION -> {
                val navigationHolder = holder as NavigationViewHolder
                navigationHolder.bind(item as NavigationItem)
            }

            VIEW_TYPE_TEXT -> {
                val textHolder = holder as TextViewHolder
                textHolder.bind(item as TextItem)
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
            is TextItem -> VIEW_TYPE_TEXT
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

    abstract inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val labelTextView: TextView = itemView.findViewById(R.id.label)
        protected val selectorView: View = itemView.findViewById(R.id.selector)
        protected val selectorTextView: TextView = itemView.findViewById(R.id.value)
        protected val arrowView: View = itemView.findViewById(R.id.arrow)
        protected val iconView: ImageView = itemView.findViewById(R.id.icon)

        init {
            selectorView.visibility = View.GONE
            arrowView.visibility = View.GONE
        }

        protected fun bind(item: Item){
            if(item.icon > -1){
                iconView.setImageResource(item.icon);
                iconView.visibility = View.VISIBLE
                iconView.visibility = View.GONE
            }else{
                iconView.setImageDrawable(null)
                iconView.visibility = View.GONE
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        protected fun setDefaultListeners(
            onClick: () -> Unit,
            onKeyDown: (keyCode: Int) -> Boolean
        ) {
            itemView.setOnClickListener {
                onClick()
            }

            itemView.setOnKeyListener { _, keyCode, event ->
                if (event?.action != KeyEvent.ACTION_DOWN) {
                    return@setOnKeyListener false
                }

                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        navigationHandler.onNavigateBack()
                        return@setOnKeyListener true
                    }
                }
                return@setOnKeyListener onKeyDown(keyCode)
            }
        }
    }

    inner class OptionViewHolder(itemView: View) : ItemViewHolder(itemView) {

        fun bind(optionItem: OptionItem) {
            super.bind(optionItem)
            selectorView.visibility = View.VISIBLE
            labelTextView.text = optionItem.label
            selectorTextView.text = optionItem.getOption().label

            setDefaultListeners(
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

    inner class ButtonViewHolder(itemView: View) : ItemViewHolder(itemView) {
        fun bind(item: ButtonItem) {
            super.bind(item)
            labelTextView.text = item.label
            setDefaultListeners(
                onClick = { item.onClick() },
                onKeyDown = { false }
            )
        }
    }

    inner class NavigationViewHolder(itemView: View) : ItemViewHolder(itemView) {

        fun bind(navigationItem: NavigationItem) {
            super.bind(navigationItem)
            labelTextView.text = navigationItem.label
            arrowView.visibility = View.VISIBLE

            setDefaultListeners(
                onClick = { navigationHandler.onNavigateTo(navigationItem) },
                onKeyDown = { false }
            )
        }
    }

    inner class TextViewHolder(itemView: View) : ItemViewHolder(itemView) {
        fun bind(textItem: TextItem) {
            super.bind(textItem)
            labelTextView.text = textItem.label
        }
    }

    interface NavigationHandler {
        fun onNavigateTo(navigationItem: NavigationItem)
        fun onNavigateBack()
    }
}
