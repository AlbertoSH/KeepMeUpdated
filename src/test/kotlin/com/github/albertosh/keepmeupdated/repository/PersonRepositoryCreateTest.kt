package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.PersonService
import io.kotlintest.specs.ShouldSpec

class PersonRepositoryCreateTest : ShouldSpec() {
    init {
        "PersonRepositoryCreate" {

            should("wrap position to 0 if a negative position is supplied") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoCreate = PersonRepositoryCreate(itemChanges, service)

                repoCreate.createPerson("name", "surname", -1)
                        .test()
                        .assertValue(Person(1, "name", "surname", 0))
            }

            should("wrap position to last position if very high position is supplied") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoCreate = PersonRepositoryCreate(itemChanges, service)

                service.createPerson("Pablo", "Ku", 0).subscribe()

                repoCreate.createPerson("name", "surname", Int.MAX_VALUE)
                        .test()
                        .assertValue(Person(2, "name", "surname", 1))
            }

            should("update positions of existing items") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoCreate = PersonRepositoryCreate(itemChanges, service)

                service.createPerson("Pablo", "Ku", 0).subscribe()
                service.createPerson("Fire", "Zenk", 1).subscribe()

                repoCreate.createPerson("name", "surname", Int.MIN_VALUE)
                        .test()
                        .assertValue(Person(3, "name", "surname", 0))

                service.getPerson()
                        .sorted()
                        .test()
                        .assertValues(
                                Person(3, "name", "surname", 0),
                                Person(1, "Pablo", "Ku", 1),
                                Person(2, "Fire", "Zenk", 2)
                        )
            }
        }
    }
}