package travis.diy.flashcardapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.text.Editable
import org.w3c.dom.Text

class FlashcardEditActivity : AppCompatActivity() {
    var entry: Entry? = null
    lateinit var text: String
//    constructor(entry: Entry,text: String)
//    {
//        this.entry = entry
//        this.text = text
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.entry = intent.extras.get("entry") as Entry?
        this.text = intent.extras.get("text") as String

        setContentView(R.layout.flashcard_edit)
        // configure widgets
        title = "Editing %s".format(text)
        val textSection = findViewById<TextInputEditText>(R.id.german_text)
        val translationSection = findViewById<TextInputEditText>(R.id.translation_text)

        textSection.text = Editable.Factory().newEditable(text)
        if (entry != null)
        {
            // get the meaning from the entry instance
            translationSection.text = Editable.Factory().newEditable(entry!!.meaning.joinToString(";"))
        }
    }

}
