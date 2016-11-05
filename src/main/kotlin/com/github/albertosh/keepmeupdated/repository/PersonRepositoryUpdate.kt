package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.IPersonService
import io.reactivex.Single

interface IPersonRepositoryUpdate {

    fun updatePerson(person: Person): Single<Person>

}

class PersonRepositoryUpdate(
        private val personChanges: ItemChanges<Person>,
        private val service: IPersonService
) : IPersonRepositoryUpdate {

    override fun updatePerson(person: Person): Single<Person> =
            service.getPerson().count()
                    .map { Math.min(it - 1, person.position.toLong()) }
                    .map { Math.max(it, 0) }
                    .flatMap { service.updatePerson(person.copy(position = it.toInt())) }
                    //.map { mapItem(it) } if mapping is necessary do it here
                    .doOnSuccess { oldPerson ->
                        val minPosition = Math.min(oldPerson.position, person.position)
                        val maxPosition = Math.max(oldPerson.position, person.position)

                        service.getPerson()
                                .filter { it.position >= minPosition }
                                .filter { it.position <= maxPosition }
                                .filter { it.id != person.id }
                                .map {
                                    it.copy(position =
                                    if (oldPerson.position < person.position)
                                        it.position - 1
                                    else
                                        it.position + 1
                                    )
                                }
                                .flatMap { service.updatePerson(it).toFlowable() }
                                .subscribe()
                    }
                    .map { person }
                    .doOnSuccess { personChanges.itemUpdated(it) }

}
