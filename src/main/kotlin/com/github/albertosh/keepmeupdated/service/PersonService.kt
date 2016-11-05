package com.github.albertosh.keepmeupdated.service

import com.github.albertosh.keepmeupdated.model.Person
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicInteger

interface IPersonService {

    fun createPerson(name: String, surname: String, position: Int): Single<Person>

    fun getPerson(id: Int): Single<Person>

    fun getPerson(): Flowable<Person>

    fun updatePerson(person: Person): Single<Person>

    fun deletePerson(id: Int): Single<Person>

}

sealed class PersonServiceError(message: String) : Throwable(message) {
    object personNotFound : PersonServiceError("Person not found")
}

/**
 * Basic in memory storage
 */
class PersonService : IPersonService {

    private val personMap = mutableMapOf<Int, Person>()
    private val idGenerator = AtomicInteger()

    override fun createPerson(name: String, surname: String, position: Int): Single<Person> = Single.create { source ->
        val id = idGenerator.incrementAndGet()
        val person = Person(
                id = id,
                name = name,
                surname = surname,
                position = position
        )
        personMap.put(id, person)
        source.onSuccess(person)
    }

    override fun getPerson(id: Int): Single<Person> = Single.create { source ->
        val person = personMap.get(id)
        if (person != null)
            source.onSuccess(person)
        else
            source.onError(PersonServiceError.personNotFound)
    }

    override fun getPerson(): Flowable<Person> = Flowable.fromIterable(personMap.values)

    override fun updatePerson(person: Person): Single<Person> = Single.create { source ->
        val oldPerson = personMap.get(person.id)
        if (oldPerson != null) {
            personMap.put(person.id, person)
            source.onSuccess(oldPerson)
        } else {
            source.onError(PersonServiceError.personNotFound)
        }
    }

    override fun deletePerson(id: Int): Single<Person> = Single.create { source ->
        val person = personMap.remove(id)
        if (person != null)
            source.onSuccess(person)
        else
            source.onError(PersonServiceError.personNotFound)
    }

}
