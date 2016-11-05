package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.IPersonService
import com.github.albertosh.keepmeupdated.service.PersonServiceError
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import java.util.*

interface IPersonRepositoryRead {

    fun readPerson(): Flowable<List<Person>>

    fun readPerson(id: Int): Flowable<Person>

}

class PersonRepositoryRead(
        private val personChanges: ItemChanges<Person>,
        private val service: IPersonService
) : IPersonRepositoryRead {

    private sealed class Change {
        class Existing(val values: List<Person>) : Change()
        class Created(val created: Person) : Change()
        class Updated(val updated: Person) : Change()
        class Deleted(val deleted: Person) : Change()
    }

    override fun readPerson(): Flowable<List<Person>> =
            Observable.merge(
                    service.getPerson()
                            //.map { mapItem(it) } if mapping is necessary do it here
                            .toList()
                            .map { Change.Existing(it) }
                            .toObservable(),
                    personChanges.insertionsObservable
                            .map { Change.Created(it) },
                    personChanges.updatesObservable
                            .map { Change.Updated(it) },
                    personChanges.removalsObservable
                            .map { Change.Deleted(it) })

                    .scan(HashMap<Int, Person>(), { acc: Map<Int, Person>, change: Change ->
                        val result = HashMap(acc)
                        when (change) {
                            is Change.Existing -> {
                                change.values.forEach { result.put(it.id, it) }
                                result
                            }
                            is Change.Created -> {
                                val insertedPerson = change.created
                                result
                                        .filterValues { it.position >= insertedPerson.position }
                                        .forEach { result[it.key] = it.value.copy(position = it.value.position + 1) }
                                result[insertedPerson.id] = insertedPerson
                                result
                            }
                            is Change.Updated -> {
                                val updatedPerson = change.updated
                                val updatedPosition = updatedPerson.position
                                val oldPosition = acc[updatedPerson.id]!!.position
                                if (updatedPosition != oldPosition) {
                                    val minPosition = Math.min(updatedPosition, oldPosition)
                                    val maxPosition = Math.max(updatedPosition, oldPosition)

                                    result
                                            .filterValues { it.position >= minPosition }
                                            .filterValues { it.position <= maxPosition }
                                            .forEach {
                                                result[it.key] = it.value.copy(position =
                                                if (updatedPosition > oldPosition)
                                                    it.value.position - 1
                                                else
                                                    it.value.position + 1
                                                )
                                            }
                                }
                                result[updatedPerson.id] = updatedPerson
                                result
                            }
                            is Change.Deleted -> {
                                val deletedPerson = change.deleted
                                result
                                        .filterValues { it.position > deletedPerson.position }
                                        .forEach { result[it.key] = it.value.copy(position = it.value.position - 1) }
                                result.remove(deletedPerson.id)
                                result
                            }
                        }
                    })
                    .skip(1) // Ignore default empty list
                    .map { it.values.sortedBy { it.position } }
                    .toFlowable(BackpressureStrategy.BUFFER)


    override fun readPerson(id: Int): Flowable<Person> =
            Observable.merge(
                    service.getPerson(id)
                            //.map { mapItem(it) } if mapping is necessary do it here
                            .toObservable(),
                    personChanges.updatesObservable
                            .filter { it.id == id },
                    personChanges.removalsObservable
                            .filter { it.id == id }
                            .flatMap {
                                ObservableSource<Person> { it.onError(PersonServiceError.personNotFound) }
                            })
                    .toFlowable(BackpressureStrategy.BUFFER)

}
