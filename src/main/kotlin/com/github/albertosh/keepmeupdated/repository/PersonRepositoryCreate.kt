package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.IPersonService
import io.reactivex.Single

interface IPersonRepositoryCreate {

    fun createPerson(name: String, surname: String, position: Int = 0): Single<Person>

}

class PersonRepositoryCreate(
        private val personChanges: ItemChanges<Person>,
        private val service: IPersonService
) : IPersonRepositoryCreate {

    override fun createPerson(name: String, surname: String, position: Int): Single<Person> =
            service.getPerson().count()
                    .map { Math.min(it, position.toLong()) }
                    .map { Math.max(it, 0) }
                    .doOnSuccess { p ->
                        service.getPerson()
                                .filter { it.position >= p }
                                .map { it.copy(position = it.position + 1) }
                                .flatMap { service.updatePerson(it).toFlowable() }
                                .subscribe()
                    }
                    .flatMap { service.createPerson(name, surname, it.toInt()) }
                    //.map { mapItem(it) } if mapping is necessary do it here
                    .doOnSuccess { personChanges.itemInserted(it) }

}
