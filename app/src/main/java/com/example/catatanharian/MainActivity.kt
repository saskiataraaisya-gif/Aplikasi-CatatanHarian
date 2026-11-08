package com.example.catatanharian

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catatanharian.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var prefs: PrefsManager

    private var allNotes: List<Note> = emptyList()
    private var searchQuery: String = ""
    private var selectionMode = false
    private val selectedIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        adapter = NoteAdapter(
            onNoteClick = { note ->
                if (selectionMode) toggleSelection(note) else openEditor(note)
            },
            onNoteLongClick = { note ->
                if (!selectionMode) enterSelectionMode()
                toggleSelection(note)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        folderAdapter = FolderAdapter { folder ->
            prefs.setFolder(folder)
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
        binding.rvFolders.layoutManager = LinearLayoutManager(this)
        binding.rvFolders.adapter = folderAdapter

        viewModel.allNotes.observe(this) { notes ->
            allNotes = notes ?: emptyList()
            render()
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditNoteActivity::class.java))
        }
        binding.btnSelect.setOnClickListener { enterSelectionMode() }
        binding.btnCancelSelect.setOnClickListener { exitSelectionMode() }
        binding.btnDeleteSelected.setOnClickListener { confirmDeleteSelected() }
        binding.btnSearch.setOnClickListener { toggleSearch() }
        binding.btnMore.setOnClickListener { showSettingsMenu() }

        binding.etSearch.doAfterTextChanged { text ->
            searchQuery = text?.toString().orEmpty().trim()
            render()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showHome()
                R.id.nav_folder -> showFolders()
            }
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectionMode) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // Pengaturan dibaca ulang setiap Activity tampil kembali
    override fun onResume() {
        super.onResume()
        adapter.setFontSize(prefs.getFontSize())
        render()
    }

    private fun openEditor(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.id)
        startActivity(intent)
    }

    // ---------- Tampilan daftar ----------

    private fun render() {
        val folder = prefs.getFolder()
        val byFolder = if (folder == PrefsManager.FOLDER_ALL) allNotes
        else allNotes.filter { it.folder == folder }
        val shown = if (searchQuery.isEmpty()) byFolder
        else byFolder.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
        }

        adapter.submitList(shown)
        binding.tvPageTitle.text =
            if (folder == PrefsManager.FOLDER_ALL) getString(R.string.page_title) else folder

        binding.emptyStateLayout.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE

        val items = listOf(FolderItem(PrefsManager.FOLDER_ALL, allNotes.size)) +
            PrefsManager.FOLDERS.map { name ->
                FolderItem(name, allNotes.count { it.folder == name })
            }
        folderAdapter.submitList(items)
        folderAdapter.setSelected(folder)

        if (selectionMode) {
            selectedIds.retainAll(allNotes.map { it.id }.toSet())
            updateSelectionUi()
        }
    }

    private fun showHome() {
        binding.homeContainer.visibility = View.VISIBLE
        binding.folderContainer.visibility = View.GONE
        render()
    }

    private fun showFolders() {
        if (selectionMode) exitSelectionMode()
        binding.homeContainer.visibility = View.GONE
        binding.folderContainer.visibility = View.VISIBLE
        render()
    }

    // ---------- Mode pilih (hapus banyak catatan) ----------

    private fun enterSelectionMode() {
        selectionMode = true
        selectedIds.clear()
        binding.normalBar.visibility = View.GONE
        binding.selectionBar.visibility = View.VISIBLE
        binding.fabAdd.hide()
        updateSelectionUi()
    }

    private fun exitSelectionMode() {
        selectionMode = false
        selectedIds.clear()
        binding.selectionBar.visibility = View.GONE
        binding.normalBar.visibility = View.VISIBLE
        binding.fabAdd.show()
        adapter.setSelection(emptySet())
    }

    private fun toggleSelection(note: Note) {
        if (!selectedIds.add(note.id)) selectedIds.remove(note.id)
        updateSelectionUi()
    }

    private fun updateSelectionUi() {
        binding.tvSelectedCount.text = getString(R.string.selected_count, selectedIds.size)
        adapter.setSelection(selectedIds)
    }

    private fun confirmDeleteSelected() {
        if (selectedIds.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_selected_title))
            .setMessage(getString(R.string.delete_selected_message, selectedIds.size))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteNotes(allNotes.filter { selectedIds.contains(it.id) })
                exitSelectionMode()
            }
            .show()
    }

    // ---------- Pencarian ----------

    private fun toggleSearch() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (binding.etSearch.visibility == View.VISIBLE) {
            binding.etSearch.setText("")
            binding.etSearch.visibility = View.GONE
            imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        } else {
            binding.etSearch.visibility = View.VISIBLE
            binding.etSearch.requestFocus()
            imm.showSoftInput(binding.etSearch, 0)
        }
    }

    // ---------- Menu pengaturan (disimpan di SharedPreferences) ----------

    private fun showSettingsMenu() {
        val popup = PopupMenu(
            ContextThemeWrapper(this, R.style.Theme_CatatanHarian_Popup),
            binding.btnMore
        )
        popup.menuInflater.inflate(R.menu.main_menu, popup.menu)

        popup.menu.findItem(R.id.action_dark_mode).isChecked = prefs.isDarkMode()
        val checkedId = when (prefs.getFontSize()) {
            PrefsManager.FONT_SMALL -> R.id.font_small
            PrefsManager.FONT_LARGE -> R.id.font_large
            else -> R.id.font_medium
        }
        popup.menu.findItem(checkedId).isChecked = true

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_dark_mode -> {
                    val enabled = !prefs.isDarkMode()
                    prefs.setDarkMode(enabled)
                    AppCompatDelegate.setDefaultNightMode(
                        if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    true
                }
                R.id.font_small, R.id.font_medium, R.id.font_large -> {
                    val size = when (item.itemId) {
                        R.id.font_small -> PrefsManager.FONT_SMALL
                        R.id.font_large -> PrefsManager.FONT_LARGE
                        else -> PrefsManager.FONT_MEDIUM
                    }
                    prefs.setFontSize(size)
                    adapter.setFontSize(size)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
