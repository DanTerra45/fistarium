package wiki.tk.fistarium

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import wiki.tk.fistarium.di.appModule

class TekkenApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TekkenApplication)
            modules(appModule)
        }
    }
}