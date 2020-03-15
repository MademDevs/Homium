package de.madem.homium.application

import android.app.Application
import android.content.Context

class HomiumApplication : Application() {

    companion object{
        var appContext : Context? = null
            private set(value) {
                if(value != null){
                    field = value
                }
            }
    }

    override fun onCreate() {
        println("APPLICATION: CREATING HOMIUM-APPLICATION")
        appContext = applicationContext
        HomiumSettings.initialize(applicationContext);
        super.onCreate()
    }
}