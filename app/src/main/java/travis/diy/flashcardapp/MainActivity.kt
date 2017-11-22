package travis.diy.flashcardapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onStart() {
        super.onStart()
        // just temporary: remove me
        val wiki = WiktionaryService()
        wiki.search("schreiben",{observable ->
            observable.doOnNext {
                entry -> System.out.println(entry.toString())
            }.subscribe()
        })

    }
}
