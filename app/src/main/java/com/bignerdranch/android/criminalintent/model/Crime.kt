package com.bignerdranch.android.criminalintent.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(
  @PrimaryKey var id: UUID = UUID.randomUUID(),
  var title: String = "",
  var date: Date = Date(),
  var isSolved: Boolean = false,
  var suspect: String = "",
  @Ignore
  var requiresPolice: Boolean = false
)