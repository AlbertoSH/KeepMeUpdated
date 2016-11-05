package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.PersonService
import io.kotlintest.specs.ShouldSpec

class PersonRepositoryUpdateTest : ShouldSpec() {
    init {
        "PersonRepositoryUpdate" {

            should("update positions of existing items") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoUpdate = PersonRepositoryUpdate(itemChanges, service)

                service.createPerson("Pablo", "Ku", 0).subscribe()
                service.createPerson("Fire", "Zenk", 1).subscribe()

                repoUpdate.updatePerson(Person(1, "Miguel", "Catalan", 1))
                        .test()
                        .assertValue(Person(1, "Miguel", "Catalan", 1))

                service.getPerson()
                        .sorted()
                        .test()
                        .assertValues(
                                Person(2, "Fire", "Zenk", 0),
                                Person(1, "Miguel", "Catalan", 1)
                        )
            }
        }
    }
}