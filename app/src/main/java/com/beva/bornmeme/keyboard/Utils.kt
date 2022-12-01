package com.beva.bornmeme.keyboard


import android.webkit.MimeTypeMap
import timber.log.Timber
import java.io.File

/** Class to provide utils that are shared across ewesticker. */
object Utils {
    /**
     * Get the mimetype of a File
     *
     * @param file File file to get the mimetype of
     * @return String? Return the mimetype or none if it cannot be determined
     */
    fun getMimeType(file: File): String? {
        Timber.w("beva \n getMimeType file $file")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
    }

    /**
     * Return a MutableList of EweSticker Supported mimetypes
     *
     * @return MutableList of EweSticker Supported mimetypes
     */
    fun getSupportedMimes(): MutableList<String> {
        Timber.w("beva \n getSupportedMimes running")
        return mutableListOf(
            "image/gif",
            "image/png",
            "image/webp",
            "image/jpeg",
            "image/heif",
            "video/3gpp",
            "video/mp4",
            "video/x-matroska",
            "video/webm"
        )
    }
}
