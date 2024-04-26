package com.bnyro.contacts.util

import android.Manifest
import android.content.Context
import android.provider.CallLog
import com.bnyro.contacts.domain.model.CallLogEntry
import com.bnyro.contacts.util.extension.intValue
import com.bnyro.contacts.util.extension.longValue
import com.bnyro.contacts.util.extension.stringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CallLogHelper {
    suspend fun getCallLog(context: Context): List<CallLogEntry> = withContext(Dispatchers.IO) {
        if (!PermissionHelper.checkPermissions(context, arrayOf(Manifest.permission.READ_CALL_LOG))) return@withContext emptyList()

        val callLog = mutableListOf<CallLogEntry>()

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val phoneNumber = cursor.stringValue(CallLog.Calls.NUMBER)
                val callType = cursor.intValue(CallLog.Calls.TYPE)!!
                val callDate = cursor.longValue(CallLog.Calls.DATE)!!
                val callDuration = cursor.longValue(CallLog.Calls.DURATION)!!
                callLog.add(
                    CallLogEntry(phoneNumber.orEmpty(), callType, callDate, callDuration)
                )
            }
        }

        return@withContext callLog
    }

    suspend fun deleteAll(context: Context, callLog: List<CallLogEntry>) = withContext(Dispatchers.IO) {
        if (!PermissionHelper.checkPermissions(context, arrayOf(Manifest.permission.WRITE_CALL_LOG))) return@withContext

        callLog.distinctBy { it.phoneNumber }.forEach { entry ->
            context.contentResolver.delete(CallLog.Calls.CONTENT_URI, "NUMBER=${entry.phoneNumber}", null)
        }
    }
}