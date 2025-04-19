package com.levin.uploadservice.persistence

interface Persistable {
    fun toPersistableData(): PersistableData

    interface Creator<T> {
        fun createFromPersistableData(data: PersistableData): T
    }
}
