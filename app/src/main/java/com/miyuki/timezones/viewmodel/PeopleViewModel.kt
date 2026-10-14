package com.miyuki.timezones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miyuki.timezones.data.CustomField
import com.miyuki.timezones.data.Person
import com.miyuki.timezones.repository.PeopleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PeopleViewModel(
    private val repository: PeopleRepository
) : ViewModel() {
    val people = repository.people.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val timezones = repository.timezones.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val customFieldNames = repository.customFieldNames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addPerson(
        name: String,
        nickname: String?,
        description: String?,
        timezoneId: Long,
        customFields: List<CustomField>
    ) {
        viewModelScope.launch {
            repository.addPerson(
                name = name,
                nickname = nickname,
                description = description,
                timezoneId = timezoneId,
                customFields = customFields
            )
        }
    }

    fun updatePerson(
        person: Person,
        customFields: List<CustomField>
    ) {
        viewModelScope.launch {
            repository.updatePerson(
                person = person,
                customFields = customFields
            )
        }
    }

    fun reorderTimezones(orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.reorderTimezones(orderedIds)
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            repository.deletePerson(person)
        }
    }

    fun addTimezone(
        ianaId: String,
        displayName: String,
        onCreated: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val timezoneId = repository.addTimezone(
                ianaId = ianaId,
                displayName = displayName,
            )
            onCreated(timezoneId)
        }
    }

    fun observeCustomFields(personId: Long) = repository.observeCustomFields(personId)
}

class PeopleViewModelFactory(
    private val repository: PeopleRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
            return PeopleViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}