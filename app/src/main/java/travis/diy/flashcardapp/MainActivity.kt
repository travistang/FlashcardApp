package travis.diy.flashcardapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.w3c.dom.Text
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onStart() {
        super.onStart()
        // just temporary: remove me
        val wiki = WiktionaryService()
        wiki.search("lustig",{observable ->
            observable.doOnNext {
                entry ->
                val text = findViewById<TextView>(R.id.hello_text)
                runOnUiThread{ text.text = entry.toString() }

            }.subscribeOn(Schedulers.io()).subscribe()
        })

    }
}
