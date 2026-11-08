package com.example.catatanharian

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note) = noteDao.insert(note)

    suspend fun update(note: Note) = noteDao.update(note)

    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun deleteAll(notes: List<Note>) = noteDao.deleteAll(notes)

    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)
}
