package de.madem.homium.application

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HomiumApplication : Application() {

    companion object{
        var appContext : Context? = null
            private set(value) {
                if(value != null){
                    field = value
                }
            }


        fun getAvailableApplicationMemory(context: Context = appContext!!) : Int{
            return with(context){
                val activityManager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                activityManager.memoryClass * 1024 * 1024
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