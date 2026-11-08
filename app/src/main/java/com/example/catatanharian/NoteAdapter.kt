package com.example.catatanharian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catatanharian.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private var fontSize: String = PrefsManager.FONT_MEDIUM
    private var selectedIds: Set<Int> = emptySet()

    fun setFontSize(size: String) {
        if (size != fontSize) {
            fontSize = size
            notifyDataSetChanged()
        }
    }

    fun setSelection(ids: Set<Int>) {
        selectedIds = ids.toSet()
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        val context = holder.binding.root.context

        val titleSize: Float
        val contentSize: Float
        when (fontSize) {
            PrefsManager.FONT_SMALL -> { titleSize = 16f; contentSize = 12f }
            PrefsManager.FONT_LARGE -> { titleSize = 22f; contentSize = 17f }
            else -> { titleSize = 18f; contentSize = 14f }
        }

        holder.binding.tvTitle.text = note.title
        holder.binding.tvTitle.textSize = titleSize
        holder.binding.tvContent.text = note.content
        holder.binding.tvContent.textSize = contentSize
        holder.binding.tvDate.text = formatDate(note.date)

        // Catatan yang dipilih diberi garis tepi
        val selected = selectedIds.contains(note.id)
        holder.binding.cardNote.strokeColor = ContextCompat.getColor(context, R.color.primary)
        holder.binding.cardNote.strokeWidth =
            if (selected) (2 * context.resources.displayMetrics.density).toInt() else 0

        holder.binding.root.setOnClickListener { onNoteClick(note) }
        holder.binding.root.setOnLongClickListener {
            onNoteLongClick(note)
            true
        }
    }

    private fun formatDate(raw: String): String {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(raw) ?: return raw
            val cal = Calendar.getInstance()
            val thisYear = cal.get(Calendar.YEAR)
            cal.time = parsed
            val pattern = if (cal.get(Calendar.YEAR) == thisYear) "d MMMM" else "d MMMM yyyy"
            return SimpleDateFormat(pattern, Locale("id", "ID")).format(parsed)
        } catch (e: Exception) {
            return raw
        }
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem == newItem
}
