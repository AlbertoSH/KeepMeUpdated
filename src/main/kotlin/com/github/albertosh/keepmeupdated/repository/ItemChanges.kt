package com.github.albertosh.keepmeupdated.repository

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ItemChanges<T> {

    private val insertionsSubject = PublishSubject.create<T>()
    private val updatesSubject = PublishSubject.create<T>()
    private val removalsSubject = PublishSubject.create<T>()

    fun itemInserted(item: T) {
        insertionsSubject.onNext(item)
    }

    val insertionsObservable: Observable<T>
        get() = insertionsSubject

    fun itemUpdated(item: T) {
        updatesSubject.onNext(item)
    }

    val updatesObservable: Observable<T>
        get() = updatesSubject

    fun itemRemoved(item: T) {
        removalsSubject.onNext(item)
    }

    val removalsObservable: Observable<T>
        get() = removalsSubject

}
