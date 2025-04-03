package com.monext.sdk.internal.threeds.response

import kotlinx.serialization.Serializable

@Serializable
internal data class DirectoryServerSdkKeyResponse(val directoryServerSdkKeyList: Array<DirectoryServerSdkKey>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DirectoryServerSdkKeyResponse

        return directoryServerSdkKeyList.contentEquals(other.directoryServerSdkKeyList)
    }

    override fun hashCode(): Int {
        return directoryServerSdkKeyList.contentHashCode()
    }
}