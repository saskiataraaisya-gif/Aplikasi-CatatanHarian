package com.example.catatanharian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catatanharian.databinding.ItemFolderBinding

data class FolderItem(val name: String, val count: Int)

class FolderAdapter(
    private val onFolderClick: (String) -> Unit
) : ListAdapter<FolderItem, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    private var selectedFolder: String = PrefsManager.FOLDER_ALL

    fun setSelected(folder: String) {
        selectedFolder = folder
        notifyDataSetChanged()
    }

    inner class FolderViewHolder(val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.binding.root.context

        holder.binding.tvFolderName.text = item.name
        holder.binding.tvFolderCount.text = context.getString(R.string.notes_count, item.count)

        val selected = item.name == selectedFolder
        holder.binding.cardFolder.strokeColor = ContextCompat.getColor(context, R.color.primary)
        holder.binding.cardFolder.strokeWidth =
            if (selected) (2 * context.resources.displayMetrics.density).toInt() else 0

        holder.binding.root.setOnClickListener { onFolderClick(item.name) }
    }
}

class FolderDiffCallback : DiffUtil.ItemCallback<FolderItem>() {
    override fun areItemsTheSame(oldItem: FolderItem, newItem: FolderItem): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: FolderItem, newItem: FolderItem): Boolean =
        oldItem == newItem
}
