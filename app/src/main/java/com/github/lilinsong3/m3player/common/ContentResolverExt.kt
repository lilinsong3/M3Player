package com.github.lilinsong3.m3player.common

import android.content.ContentResolver
import android.content.ContentResolver.QUERY_ARG_LIMIT
import android.content.ContentResolver.QUERY_ARG_OFFSET
import android.content.ContentResolver.QUERY_ARG_SQL_SELECTION
import android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
fun createPagingQueryBundle(page: Int, pageSize: Int) = Bundle().apply {
    putInt(
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) QUERY_ARG_LIMIT
        else ContentResolver.QUERY_ARG_SQL_LIMIT, pageSize
    )
    putInt(
        QUERY_ARG_OFFSET, (page - 1) * pageSize
    ) // rows=10, page=2, pageSize=4, limit=4, offset=(2-1)*4
}

@RequiresApi(Build.VERSION_CODES.O)
fun createPagingQueryBundle(
    selection: String,
    selectionArgs: Array<String>,
    page: Int,
    pageSize: Int
) = createPagingQueryBundle(page, pageSize).apply {
    putString(QUERY_ARG_SQL_SELECTION, selection)
    putStringArray(QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
}

fun ContentResolver.pagingQuery(
    uri: Uri,
    projection: Array<String>,
    page: Int,
    pageSize: Int
): Cursor? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) query(
        uri,
        projection,
        null,
        null,
        "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
    ) else query(
        uri,
        projection, createPagingQueryBundle(page, pageSize), null
    )

fun ContentResolver.pagingQuery(
    uri: Uri,
    projection: Array<String>,
    selection: String,
    selectionArgs: Array<String>,
    page: Int,
    pageSize: Int
): Cursor? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) query(
        uri,
        projection,
        selection,
        selectionArgs,
        "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
    ) else query(
        uri,
        projection, createPagingQueryBundle(
            selection,
            selectionArgs, page, pageSize
        ), null
    )