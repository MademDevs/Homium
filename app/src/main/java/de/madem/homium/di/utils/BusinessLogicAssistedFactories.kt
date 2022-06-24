package de.madem.homium.di.utils

import android.content.Context
import dagger.assisted.AssistedFactory
import de.madem.homium.speech.SpeechAssistant
import de.madem.homium.ui.fragments.shopping.ShoppingToInventoryHandler
import de.madem.homium.utilities.CookingAssistant
import java.lang.ref.WeakReference

@AssistedFactory
interface CookingAssistantAssistedFactory {
    fun create(contextRef: WeakReference<Context>) : CookingAssistant
}

@AssistedFactory
interface ShoppingToInventoryHandlerAssistedFactory {
    fun create(context: Context) : ShoppingToInventoryHandler
}

@AssistedFactory
interface SpeechAssistantAssistedFactory {
    fun create(context: Context) : SpeechAssistant
}
