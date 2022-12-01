package com.beva.bornmeme.keyboard

import timber.log.Timber
import java.io.File
import java.util.LinkedList

/**
 * Basically this behaved like an ordered set with some maximum capacity. When this capacity is
 * exceeded an element is removed from the start
 */
class Cache(private val size: Int = 30) {

    private var data: LinkedList<String> = LinkedList()

    /**
     * Logic to add an element
     *
     * @param elem
     *
     * @return
     */
    fun add(elem: String): String? {
        Timber.w("beva \n Cache add running")
        if (!this.data.contains(elem)) {
            this.data.add(elem)
            Timber.w("beva \n Cache this.data ${this.data}")
            Timber.w("beva \n Cache elem $elem")
        }
        if (this.data.size > size) {
            return this.data.removeAt(0)
        }
        return null
    }

    /**
     * Get an element
     *
     * @param idx
     */
    fun get(idx: Int) {
        Timber.w("beva \n Cache get running")
        this.data[idx]
        Timber.w("beva \n Cache this.data[idx] ${this.data[idx]}")
    }

    /**
     * convert this to a string to write to shared-pref
     *
     * @return
     */
    fun toSharedPref(): String {
        Timber.w("beva \n Cache toSharedPref running")
        return this.data.joinToString("\n") { it }
    }

    /**
     * convert this to a array of files
     *
     * @return
     */
    fun toFiles(): Array<File> {
        Timber.w("beva \n Cache toFiles running")
        return this.data.map { File(it) }.toTypedArray()
    }

    /** convert from a string (shared-pref) to this */
    fun fromSharedPref(raw: String) {
        Timber.w("beva \n Cache fromSharedPref running")
        this.data = LinkedList()
        this.data.addAll(raw.split("\n").filter { it.isNotEmpty() })
    }
}