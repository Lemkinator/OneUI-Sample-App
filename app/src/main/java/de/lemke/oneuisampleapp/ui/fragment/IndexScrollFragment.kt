package de.lemke.oneuisampleapp.ui.fragment

import android.content.Context
import android.content.res.Configuration
import android.database.MatrixCursor
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.util.SeslRoundedCorner
import androidx.appcompat.util.SeslSubheaderRoundedCorner
import androidx.appcompat.view.menu.SeslMenuItem
import androidx.indexscroll.widget.SeslCursorIndexer
import androidx.indexscroll.widget.SeslIndexScrollView
import androidx.indexscroll.widget.SeslIndexScrollView.OnIndexBarEventListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.domain.GetUserSettingsUseCase
import de.lemke.oneuisampleapp.domain.UpdateUserSettingsUseCase
import de.lemke.oneuisampleapp.ui.BaseFragment
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.widget.Separator
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IndexScrollFragment : BaseFragment() {
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var toolbarLayout: ToolbarLayout
    private lateinit var adapter: IndexAdapter
    private lateinit var listView: RecyclerView
    private var selected = HashMap<Int, Boolean>()
    private var selecting = false
    private var checkAllListening = true
    private var currentSectionIndex = 0
    private lateinit var indexScrollView: SeslIndexScrollView
    private var isTextModeEnabled = false

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        indexScrollView = view.findViewById(R.id.indexscroll_view)
        toolbarLayout = requireActivity().findViewById(R.id.drawerLayout)
        listView = view.findViewById(R.id.indexscroll_list)
        initListView(view)
        initIndexScroll()
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (selecting) setSelecting(false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val textModeItem = menu.findItem(R.id.menu_indexscroll_text)
        textModeItem.isVisible = true
        (textModeItem as SeslMenuItem).badgeText = getString(dev.oneuiproject.oneui.design.R.string.oui_new_badge_text)
        lifecycleScope.launch {
            isTextModeEnabled = getUserSettings().showLetters
            textModeItem.title = if (isTextModeEnabled) getString(R.string.hide_letters) else getString(R.string.show_letters)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_indexscroll_text) {
            (item as SeslMenuItem).badgeText = null
            isTextModeEnabled = !isTextModeEnabled
            lifecycleScope.launch { updateUserSettings { it.copy(showLetters = isTextModeEnabled) } }
            item.title = if (isTextModeEnabled) getString(R.string.hide_letters) else getString(R.string.show_letters)
            indexScrollView.setIndexBarTextMode(isTextModeEnabled)
            indexScrollView.invalidate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isRtl = newConfig.layoutDirection == View.LAYOUT_DIRECTION_RTL
        indexScrollView.setIndexBarGravity(if (isRtl) SeslIndexScrollView.GRAVITY_INDEX_BAR_LEFT else SeslIndexScrollView.GRAVITY_INDEX_BAR_RIGHT)
    }

    override val layoutResId: Int = R.layout.fragment_indexscroll
    override val iconResId: Int = dev.oneuiproject.oneui.R.drawable.ic_oui_edge_panels
    override val title: CharSequence = "IndexScroll"

    @Suppress("unused_parameter")
    private fun initListView(view: View) {
        selected = HashMap()
        listItems.indices.forEach { i -> selected[i] = false }
        listView.layoutManager = LinearLayoutManager(context)
        adapter = IndexAdapter()
        listView.adapter = adapter
        listView.addItemDecoration(ItemDecoration(requireContext()))
        listView.itemAnimator = null
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetLastRoundedCorner(true)
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetSmoothScrollEnabled(true)
        listView.seslSetLongPressMultiSelectionListener(object : RecyclerView.SeslLongPressMultiSelectionListener {
            override fun onItemSelected(view: RecyclerView, child: View, position: Int, id: Long) {
                if (adapter.getItemViewType(position) == 0) toggleItemSelected(position)
            }

            override fun onLongPressMultiSelectionStarted(x: Int, y: Int) {}
            override fun onLongPressMultiSelectionEnded(x: Int, y: Int) {}
        })
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            selecting = true
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
            toolbarLayout.actionModeBottomMenu.clear()
            toolbarLayout.setActionModeBottomMenu(R.menu.menu_select)
            toolbarLayout.setActionModeBottomMenuListener { item: MenuItem ->
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                setSelecting(false)
                true
            }
            toolbarLayout.showActionMode()
            toolbarLayout.setActionModeCheckboxListener { _, isChecked ->
                if (checkAllListening) {
                    selected.replaceAll { _, _ -> isChecked }
                    listItems.forEachIndexed { index, itemName -> if (itemName.length == 1) selected[index] = false }
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
                toolbarLayout.setActionModeCount(selected.values.count { it }, listItems.size - 28)
            }
            onBackPressedCallback.isEnabled = true
        } else {
            selecting = false
            for (i in 0 until adapter.itemCount) selected[i] = false
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
            toolbarLayout.setActionModeCount(0, listItems.size - 28)
            toolbarLayout.dismissActionMode()
            onBackPressedCallback.isEnabled = false
        }
    }

    fun toggleItemSelected(position: Int) {
        selected[position] = !selected[position]!!
        adapter.notifyItemChanged(position)
        checkAllListening = false
        toolbarLayout.setActionModeCount(selected.values.count { it }, listItems.size - 28)
        checkAllListening = true
    }

    private fun initIndexScroll() {
        val isRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        indexScrollView.setIndexBarGravity(if (isRtl) SeslIndexScrollView.GRAVITY_INDEX_BAR_LEFT else SeslIndexScrollView.GRAVITY_INDEX_BAR_RIGHT)
        val cursor = MatrixCursor(arrayOf("item"))
        for (item in listItems) cursor.addRow(arrayOf(item))
        cursor.moveToFirst()
        val indexer = SeslCursorIndexer(
            cursor, 0,
            "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,Б".split(",").toTypedArray(), 0
        )
        indexer.setGroupItemsCount(1)
        indexer.setMiscItemsCount(3)
        indexScrollView.setIndexer(indexer)
        indexScrollView.setOnIndexBarEventListener(object : OnIndexBarEventListener {
            override fun onIndexChanged(sectionIndex: Int) {
                if (currentSectionIndex != sectionIndex) {
                    currentSectionIndex = sectionIndex
                    if (listView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                        listView.stopScroll()
                    }
                    (listView.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(sectionIndex, 0)
                }
            }

            override fun onPressed(v: Float) {}
            override fun onReleased(v: Float) {}
        })
        indexScrollView.attachToRecyclerView(listView)
        lifecycleScope.launch {
            isTextModeEnabled = getUserSettings().showLetters
            indexScrollView.setIndexBarTextMode(isTextModeEnabled)
        }

    }

    inner class IndexAdapter internal constructor() : RecyclerView.Adapter<IndexAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = mutableListOf()
        private var positionForSection: MutableList<Int> = mutableListOf()
        private var sectionForPosition: MutableList<Int> = mutableListOf()

        init {
            sections.add("")
            positionForSection.add(0)
            sectionForPosition.add(0)
            for (i in 1 until listItems.size) {
                val letter = listItems[i]
                if (letter.length == 1) {
                    sections.add(letter)
                    positionForSection.add(i)
                }
                sectionForPosition.add(sections.size - 1)
            }
        }

        override fun getItemCount(): Int = listItems.size
        override fun getItemViewType(position: Int): Int = if (listItems[position].length == 1) 1 else 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = if (viewType == 0) {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.indexscroll_listview_item, parent, false)
            ViewHolder(view, false)
        } else ViewHolder(Separator(context!!), true)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = listItems[position]
            if (holder.isSeparator) {
                holder.textView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } else {
                holder.imageView.setImageResource(
                    when {
                        selected[position]!! -> R.drawable.indexscroll_selected_icon
                        position == 0 -> R.drawable.indexscroll_group_icon
                        else -> R.drawable.indexscroll_item_icon
                    }
                )
                holder.parentView.setOnClickListener {
                    if (selecting) toggleItemSelected(position)
                    else {
                        Toast.makeText(context, holder.textView.text, Toast.LENGTH_SHORT).show()
                    }
                }
                holder.parentView.setOnLongClickListener {
                    if (!selecting) setSelecting(true)
                    toggleItemSelected(position)
                    listView.seslStartLongPressMultiSelection()
                    true
                }
            }
        }

        override fun getSections(): Array<Any> = sections.toTypedArray()
        override fun getPositionForSection(sectionIndex: Int): Int = positionForSection[sectionIndex]
        override fun getSectionForPosition(position: Int): Int = sectionForPosition[position]

        inner class ViewHolder internal constructor(itemView: View, var isSeparator: Boolean) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView
            lateinit var parentView: LinearLayout
            lateinit var imageView: ImageView

            init {
                if (isSeparator) textView = itemView as TextView
                else {
                    parentView = itemView as LinearLayout
                    imageView = itemView.findViewById(R.id.item_icon)
                    textView = itemView.findViewById(R.id.item_text)
                }
            }
        }
    }

    private inner class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable?
        private val roundedCorner: SeslSubheaderRoundedCorner

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = context.getDrawable(
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
            roundedCorner = SeslSubheaderRoundedCorner(getContext())
            roundedCorner.roundedCorners = SeslRoundedCorner.ROUNDED_CORNER_ALL
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val holder = listView.getChildViewHolder(child) as IndexAdapter.ViewHolder
                if (!holder.isSeparator) {
                    val top = (child.bottom + (child.layoutParams as MarginLayoutParams).bottomMargin)
                    val bottom = divider!!.intrinsicHeight + top
                    divider.setBounds(parent.left, top, parent.right, bottom)
                    divider.draw(c)
                }
            }
        }

        override fun seslOnDispatchDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val holder = listView.getChildViewHolder(child) as IndexAdapter.ViewHolder
                if (holder.isSeparator) roundedCorner.drawRoundedCorner(child, c)
            }
        }
    }

    var listItems = listOf(
        "Groups",
        "A",
        "Aaron",
        "Abe",
        "Abigail",
        "Abraham",
        "Ace",
        "Adelaide",
        "Adele",
        "Aiden",
        "Alice",
        "Allison",
        "Amelia",
        "Amity",
        "Anise",
        "Ann",
        "Annabel",
        "Anneliese",
        "Annora",
        "Anthony",
        "Apollo",
        "Arden",
        "Arthur",
        "Aryn",
        "Ashten",
        "Avery",
        "B",
        "Bailee",
        "Bailey",
        "Beck",
        "Benjamin",
        "Berlynn",
        "Bernice",
        "Bianca",
        "Blair",
        "Blaise",
        "Blake",
        "Blanche",
        "Blayne",
        "Bram",
        "Brandt",
        "Bree",
        "Breean",
        "Brendon",
        "Brett",
        "Brighton",
        "Brock",
        "Brooke",
        "Byron",
        "C",
        "Caleb",
        "Cameron",
        "Candice",
        "Caprice",
        "Carelyn",
        "Caren",
        "Carleen",
        "Carlen",
        "Carmden",
        "Cash",
        "Caylen",
        "Cerise",
        "Charles",
        "Chase",
        "Clark",
        "Claude",
        "Claudia",
        "Clelia",
        "Clementine",
        "Cody",
        "Conrad",
        "Coralie",
        "Coreen",
        "Coy",
        "D",
        "Damien",
        "Damon",
        "Daniel",
        "Dante",
        "Dash",
        "David",
        "Dawn",
        "Dean",
        "Debree",
        "Denise",
        "Denver",
        "Devon",
        "Dex",
        "Dezi",
        "Dominick",
        "Doran",
        "Drake",
        "Drew",
        "Dustin",
        "E",
        "Edward",
        "Elein",
        "Eli",
        "Elias",
        "Elijah",
        "Ellen",
        "Ellice",
        "Ellison",
        "Ellory",
        "Elodie",
        "Eloise",
        "Emeline",
        "Emerson",
        "Eminem",
        "Erin",
        "Evelyn",
        "Everett",
        "Evony",
        "F",
        "Fawn",
        "Felix",
        "Fern",
        "Fernando",
        "Finn",
        "Francis",
        "G",
        "Gabriel",
        "Garrison",
        "Gavin",
        "George",
        "Georgina",
        "Gillian",
        "Glenn",
        "Grant",
        "Gregory",
        "Grey",
        "Gwendolen",
        "H",
        "Haiden",
        "Harriet",
        "Harrison",
        "Heath",
        "Henry",
        "Hollyn",
        "Homer",
        "Hope",
        "Hugh",
        "Hyrum",
        "I",
        "Imogen",
        "Irene",
        "Isaac",
        "Isaiah",
        "J",
        "Jack",
        "Jacklyn",
        "Jackson",
        "Jae",
        "Jaidyn",
        "James",
        "Jane",
        "Janetta",
        "Jared",
        "Jasper",
        "Javan",
        "Jax",
        "Jeremy",
        "Joan",
        "Joanna",
        "Jolee",
        "Jordon",
        "Joseph",
        "Josiah",
        "Juan",
        "Judd",
        "Jude",
        "Julian",
        "Juliet",
        "Julina",
        "June",
        "Justice",
        "Justin",
        "K",
        "Kae",
        "Kai",
        "Kaitlin",
        "Kalan",
        "Karilyn",
        "Kate",
        "Kathryn",
        "Kent",
        "Kingston",
        "Korin",
        "Krystan",
        "Kylie",
        "L",
        "Lane",
        "Lashon",
        "Lawrence",
        "Lee",
        "Leo",
        "Leonie",
        "Levi",
        "Lilibeth",
        "Lillian",
        "Linnea",
        "Louis",
        "Louisa",
        "Love",
        "Lucinda",
        "Luke",
        "Lydon",
        "Lynn",
        "M",
        "Madeleine",
        "Madisen",
        "Mae",
        "Malachi",
        "Marcella",
        "Marcellus",
        "Marguerite",
        "Matilda",
        "Matteo",
        "Meaghan",
        "Merle",
        "Michael",
        "Menime",
        "Mirabel",
        "Miranda",
        "Miriam",
        "Monteen",
        "Murphy",
        "Myron",
        "N",
        "Nadeen",
        "Naomi",
        "Natalie",
        "Naveen",
        "Neil",
        "Nevin",
        "Nicolas",
        "Noah",
        "Noel",
        "O",
        "Ocean",
        "Olive",
        "Oliver",
        "Oren",
        "Orlando",
        "Oscar",
        "P",
        "Paul",
        "Payten",
        "Porter",
        "Preston",
        "Q",
        "Quintin",
        "R",
        "Raine",
        "Randall",
        "Raven",
        "Ray",
        "Rayleen",
        "Reagan",
        "Rebecca",
        "Reese",
        "Reeve",
        "Rene",
        "Rhett",
        "Ricardo",
        "Riley",
        "Robert",
        "Robin",
        "Rory",
        "Rosalind",
        "Rose",
        "Ryder",
        "Rylie",
        "S",
        "Salvo :)",
        "Sean",
        "Selene",
        "Seth",
        "Shane",
        "Sharon",
        "Sheridan",
        "Sherleen",
        "Silvia",
        "Sophia",
        "Sue",
        "Sullivan",
        "Susannah",
        "Sutton",
        "Suzan",
        "Syllable",
        "T",
        "Tanner",
        "Tavian",
        "Taye",
        "Taylore",
        "Thomas",
        "Timothy",
        "Tobias",
        "Trevor",
        "Trey",
        "Tristan",
        "Troy",
        "Tyson",
        "U",
        "Ulvi",
        "Uwu",
        "V",
        "Vanessa",
        "Varian",
        "Verena",
        "Vernon",
        "Vincent",
        "Viola",
        "Vivian",
        "W",
        "Wade",
        "Warren",
        "Will",
        "William",
        "X",
        "Xavier",
        "Y",
        "Yann :)",
        "Z",
        "Zachary",
        "Zane",
        "Zion",
        "Zoe",
        "Б",
        "Блять lol",
        "#",
        "040404",
        "121002"
    )
}