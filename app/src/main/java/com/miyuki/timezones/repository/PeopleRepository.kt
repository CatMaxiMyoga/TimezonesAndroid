package com.miyuki.timezones.repository

import androidx.room3.withWriteTransaction
import com.miyuki.timezones.data.AppDatabase
import com.miyuki.timezones.data.CustomField
import com.miyuki.timezones.data.Person
import com.miyuki.timezones.data.Timezone
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

class PeopleRepository(
    private val database: AppDatabase
) {
    private val personDao = database.personDao()
    private val timezoneDao = database.timezoneDao()
    private val customFieldDao = database.customFieldDao()

    val people: Flow<List<Person>> = personDao.observeAll()
    val timezones: Flow<List<Timezone>> = timezoneDao.observeAll()
    val customFieldNames: Flow<List<String>> = customFieldDao.observeAllNames()

    fun observeCustomFields(personId: Long): Flow<List<CustomField>> = customFieldDao.observeForPerson(personId)

    suspend fun addTimezone(
        ianaId: String,
        displayName: String
    ): Long {
        require(ianaId in ZoneId.getAvailableZoneIds()) {
            "Invalid IANA time ID: $ianaId"
        }

        val nextOrder = timezoneDao.getNextOrder()

        return timezoneDao.insert(
            Timezone(
                ianaId = ianaId,
                displayName = displayName,
                order = nextOrder
            )
        )
    }

    suspend fun addPerson(
        name: String,
        nickname: String?,
        description: String?,
        timezoneId: Long,
        customFields: List<CustomField>
    ) {
        database.withWriteTransaction {
            val personId = personDao.insert(
                Person(
                    name = name,
                    nickname = nickname,
                    description = description,
                    timezoneId = timezoneId
                )
            )

            customFields.forEach { field ->
                customFieldDao.insert(
                    field.copy(
                        id = 0,
                        personId = personId
                    )
                )
            }
        }
    }

    suspend fun updatePerson(
        person: Person,
        customFields: List<CustomField>
    ) {
        database.withWriteTransaction {
            personDao.update(person)
            customFieldDao.deleteForPerson(person.id)

            customFields.forEach { field ->
                customFieldDao.insert(
                    field.copy(
                        id = 0,
                        personId = person.id
                    )
                )
            }

            deleteEmptyTimezones()
        }
    }

    suspend fun reorderTimezones(orderedIds: List<Long>) {
        database.withWriteTransaction {
            val existingTimezones = timezoneDao.getAll()
            val byId = existingTimezones.associateBy { it.id }

            require(
                orderedIds.size == existingTimezones.size &&
                    orderedIds.toSet() == byId.keys
            ) {
                "The reordered timezone list doesn't match the database."
            }

            orderedIds.forEachIndexed { index, id ->
                val timezone = requireNotNull(byId[id])
                timezoneDao.update(timezone.copy(order = index))
            }
        }
    }

    suspend fun deletePerson(person: Person) {
        database.withWriteTransaction {
            customFieldDao.deleteForPerson(person.id)
            personDao.delete(person)
            deleteEmptyTimezones()
        }
    }

    private suspend fun deleteEmptyTimezones() {
        val timezones = timezoneDao.getAll()

        for (timezone in timezones) {
            if (personDao.getForTimezone(timezone.id).isEmpty()) {
                timezoneDao.delete(timezone)
            }
        }
    }
}