package com.example.catatanharian

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.catatanharian.databinding.ActivityAddEditNoteBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var prefs: PrefsManager

    private var noteId: Int = -1
    private var currentNote: Note? = null

    companion object {
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        prefs = PrefsManager(this)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        noteId = intent.getIntExtra(EXTRA_ID, -1)

        if (noteId != -1) {
            binding.toolbar.title = getString(R.string.edit_note)
            loadNote(noteId)
        } else {
            binding.toolbar.title = getString(R.string.add_note)
            // Catatan baru masuk ke folder yang sedang dipilih (jika ada)
            selectFolderChip(prefs.getFolder())
        }

        binding.btnSave.setOnClickListener { saveNote() }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    // Ukuran font isi catatan mengikuti pengaturan yang tersimpan
    override fun onResume() {
        super.onResume()
        val size = when (prefs.getFontSize()) {
            PrefsManager.FONT_SMALL -> 14f
            PrefsManager.FONT_LARGE -> 20f
            else -> 16f
        }
        binding.etContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    private fun selectFolderChip(folder: String) {
        val chipId = when (folder) {
            "Kuliah" -> R.id.chipKuliah
            "Pekerjaan" -> R.id.chipPekerjaan
            else -> R.id.chipPribadi
        }
        binding.chipGroupFolder.check(chipId)
    }

    private fun selectedFolder(): String {
        return when (binding.chipGroupFolder.checkedChipId) {
            R.id.chipKuliah -> "Kuliah"
            R.id.chipPekerjaan -> "Pekerjaan"
            else -> "Pribadi"
        }
    }

    private fun loadNote(id: Int) {
        lifecycleScope.launch {
            val note = viewModel.getNoteById(id)
            if (note != null) {
                currentNote = note
                binding.etTitle.setText(note.title)
                binding.etContent.setText(note.content)
                selectFolderChip(note.folder)
            }
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = getString(R.string.title_required)
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val folder = selectedFolder()

        val existing = currentNote
        if (noteId != -1 && existing != null) {
            viewModel.update(
                existing.copy(title = title, content = content, date = today, folder = folder)
            )
            Snackbar.make(binding.root, getString(R.string.note_updated), Snackbar.LENGTH_SHORT).show()
        } else {
            viewModel.insert(
                Note(title = title, content = content, date = today, folder = folder)
            )
            Snackbar.make(binding.root, getString(R.string.note_added), Snackbar.LENGTH_SHORT).show()
        }

        binding.root.postDelayed({ finish() }, 400)
    }
}
