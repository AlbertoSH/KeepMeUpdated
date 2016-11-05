package com.github.albertosh.keepmeupdated.repository

import com.github.albertosh.keepmeupdated.model.Person
import com.github.albertosh.keepmeupdated.service.PersonService
import com.github.albertosh.keepmeupdated.service.PersonServiceError
import io.kotlintest.specs.ShouldSpec

class PersonRepositoryReadTest : ShouldSpec() {
    init {
        "PersonRepositoryRead.readPerson" {
            should("emit an empty list if there is no person") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)

                repoRead.readPerson()
                        .test()
                        .assertValue(listOf())
            }
            should("emit the ordered initial list of person") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()
                service.createPerson("Pablo", "Ku", 0).subscribe()

                repoRead.readPerson()
                        .test()
                        .assertValue(listOf(
                                Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                Person(id = 1, name = "Fire", surname = "Zenk", position = 1)
                        ))
            }
            should("emit an updated list if a person is created through repository in default position") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoCreate = PersonRepositoryCreate(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()
                service.createPerson("Pablo", "Ku", 0).subscribe()

                val observer = repoRead.readPerson()
                        .test()

                repoCreate.createPerson("Miguel", "Catalan").subscribe()

                observer
                        .assertValues(
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 1)
                                ),
                                listOf(
                                        Person(id = 3, name = "Miguel", surname = "Catalan", position = 0),
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 1),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 2)
                                )
                        )
            }
            should("emit an updated list if a person is created through repository in an intermediate position") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoCreate = PersonRepositoryCreate(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()
                service.createPerson("Pablo", "Ku", 0).subscribe()

                val observer = repoRead.readPerson()
                        .test()

                repoCreate.createPerson("Miguel", "Catalan", 1).subscribe()

                observer
                        .assertValues(
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 1)
                                ),
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 3, name = "Miguel", surname = "Catalan", position = 1),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 2)
                                )
                        )
            }
            should("emit an updated list if a person is created through repository in last position") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoCreate = PersonRepositoryCreate(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()
                service.createPerson("Pablo", "Ku", 0).subscribe()

                val observer = repoRead.readPerson()
                        .test()

                repoCreate.createPerson("Miguel", "Catalan", Int.MAX_VALUE).subscribe()

                observer
                        .assertValues(
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 1)
                                ),
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 1),
                                        Person(id = 3, name = "Miguel", surname = "Catalan", position = 2)
                                )
                        )
            }
            should("emit an updated list if a person is deleted through repository") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoDelete = PersonRepositoryDelete(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()
                service.createPerson("Pablo", "Ku", 0).subscribe()

                val observer = repoRead.readPerson()
                        .test()

                repoDelete.deletePerson(2).subscribe()

                observer
                        .assertValues(
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 1)
                                ),
                                listOf(
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 0)
                                )
                        )
            }
            should("emit an updated list if a person is updated through repository") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoUpdate = PersonRepositoryUpdate(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()
                service.createPerson("Pablo", "Ku", 0).subscribe()

                val observer = repoRead.readPerson()
                        .test()

                repoUpdate.updatePerson(Person(1, "Miguel", "Catalan", 0)).subscribe()

                observer
                        .assertValues(
                                listOf(
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 0),
                                        Person(id = 1, name = "Fire", surname = "Zenk", position = 1)
                                ),
                                listOf(
                                        Person(id = 1, name = "Miguel", surname = "Catalan", position = 0),
                                        Person(id = 2, name = "Pablo", surname = "Ku", position = 1)
                                )
                        )
            }
        }
        "PersonRepositoryRead.readPerson with id " {
            should("emit an error if the person doesn't exist") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)

                repoRead.readPerson(1)
                        .test()
                        .assertError(PersonServiceError.personNotFound)
            }
            should("emit the person if it exists") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()

                repoRead.readPerson(1)
                        .test()
                        .assertValue(Person(1, "Fire", "Zenk", 1))
            }
            should("emit the updates of a person when they happens") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoUpdate = PersonRepositoryUpdate(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()

                val observer = repoRead.readPerson(1)
                        .test()

                repoUpdate.updatePerson(Person(1, "Pablo", "Ku", 0)).subscribe()

                observer
                        .assertValues(
                                Person(1, "Fire", "Zenk", 1),
                                Person(1, "Pablo", "Ku", 0)
                        )
            }
            should("emit an error when the person is deleted") {
                val itemChanges = ItemChanges<Person>()
                val service = PersonService()
                val repoRead = PersonRepositoryRead(itemChanges, service)
                val repoDelete = PersonRepositoryDelete(itemChanges, service)

                service.createPerson("Fire", "Zenk", 1).subscribe()

                val observer = repoRead.readPerson(1)
                        .test()

                repoDelete.deletePerson(1).subscribe()

                observer
                        .assertValue(Person(1, "Fire", "Zenk", 1))
                        .assertError(PersonServiceError.personNotFound)
            }
        }
    }
}