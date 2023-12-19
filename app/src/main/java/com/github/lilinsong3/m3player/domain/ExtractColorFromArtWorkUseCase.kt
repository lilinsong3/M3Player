package com.github.lilinsong3.m3player.domain

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExtractColorFromArtworkUseCase @Inject constructor(
    private val contentResolver: ContentResolver
) {
    suspend operator fun invoke(artworkUri: Uri?): Int = withContext(Dispatchers.Default) {
        if (artworkUri == null) Color.BLACK
        else Palette.from(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) MediaStore.Images.Media.getBitmap(
                contentResolver, artworkUri
            ) else ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, artworkUri))
        ).generate().getDominantColor(Color.BLACK)
    }
}