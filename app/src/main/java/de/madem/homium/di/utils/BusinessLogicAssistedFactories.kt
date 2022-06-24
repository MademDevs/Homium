package de.madem.homium.di.utils

import android.content.Context
import dagger.assisted.AssistedFactory
import de.madem.homium.utilities.CookingAssistant
import java.lang.ref.WeakReference

@AssistedFactory
interface CookingAssistantAssistedFactory {
    fun create(contextRef: WeakReference<Context>) : CookingAssistant
}