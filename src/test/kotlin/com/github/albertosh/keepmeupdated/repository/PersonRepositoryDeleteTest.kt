package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.PersonService
import io.kotlintest.specs.ShouldSpec

class PersonRepositoryDeleteTest : ShouldSpec() {
    init {
        "PersonRepositoryDelete" {

            should("update positions of existing items") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoDelete = PersonRepositoryDelete(itemChanges, service)

                service.createPerson("Pablo", "Ku", 0).subscribe()
                service.createPerson("Fire", "Zenk", 1).subscribe()

                repoDelete.deletePerson(1)
                        .test()
                        .assertValue(Person(1, "Pablo", "Ku", 0))

                service.getPerson()
                        .sorted()
                        .test()
                        .assertValues(
                                Person(2, "Fire", "Zenk", 0)
                        )
            }
        }
    }
}