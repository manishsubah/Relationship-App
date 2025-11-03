package com.manishsubah.amora.data.local

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint to access SessionManager from non-injectable contexts.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SessionManagerEntryPoint {
    fun sessionManager(): SessionManager
}

