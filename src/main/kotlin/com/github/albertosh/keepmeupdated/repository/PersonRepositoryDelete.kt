package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.IPersonService
import io.reactivex.Single

interface IPersonRepositoryDelete {

    fun deletePerson(id: Int): Single<Person>

}

class PersonRepositoryDelete(
        private val personChanges: ItemChanges<Person>,
        private val service: IPersonService
) : IPersonRepositoryDelete {

    override fun deletePerson(id: Int): Single<Person> =
            service.deletePerson(id)
                    //.map { mapItem(it) } if mapping is necessary do it here
                    .doOnSuccess { deleted ->
                        service.getPerson()
                                .filter { it.position > deleted.position }
                                .map { it.copy(position = it.position - 1) }
                                .flatMap { service.updatePerson(it).toFlowable() }
                                .subscribe()
                    }
                    .doOnSuccess { personChanges.itemRemoved(it) }

}
