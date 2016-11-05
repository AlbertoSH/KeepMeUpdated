package com.github.albertosh.keepmeupdated.model

data class Person(
        val id: Int,
        val name: String,
        val surname: String,
        val position: Int) : Comparable<Person> {

    override fun compareTo(other: Person) = position.compareTo(other.position)

}
