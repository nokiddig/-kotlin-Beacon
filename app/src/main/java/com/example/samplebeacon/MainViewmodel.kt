package com.example.samplebeacon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.altbeacon.beacon.Beacon

class MainViewmodel : ViewModel() {
    private var listBeacon: MutableList<Beacon> = mutableListOf(Beacon.Builder()
        .setId1("f72bf3fa-3087-11ee-be56-0242ac120002").build())

    fun getListBeacon(): MutableList<Beacon>{
        return listBeacon
    }

    fun addNewBeacon(beacon:Beacon){
        listBeacon.add(beacon)
    }
}