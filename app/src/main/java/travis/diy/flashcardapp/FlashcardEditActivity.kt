package travis.diy.flashcardapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.text.Editable
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.mobsandgeeks.saripaar.annotation.NotEmpty

class FlashcardEditActivity : AppCompatActivity() {
    private var entry: Entry? = null
    private lateinit var text: String

    @NotEmpty
    private lateinit var textSection: TextInputEditText

    @NotEmpty
    private lateinit var translationSection: TextInputEditText

    @NotEmpty
    private lateinit var formSpinner: Spinner

    private lateinit var formDetailValidationFun : () -> Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.entry = intent.extras.get("entry") as Entry?
        this.text = intent.extras.get("text") as String

        setContentView(R.layout.flashcard_edit)

        // configure widgets
        title = "Editing %s".format(text)

        // configure entry detail form
        textSection = findViewById<TextInputEditText>(R.id.german_text)
        translationSection = findViewById<TextInputEditText>(R.id.translation_text)
        formSpinner = findViewById<Spinner>(R.id.form)

        configureSpinner(formSpinner)

        if (entry != null)
        {
            // get the word itself
            textSection.text = Editable.Factory().newEditable(entry!!.word)
            // get the meaning from the entry instance
            translationSection.text = Editable.Factory().newEditable(entry!!.meaning.joinToString(";"))
        }
    }

    private fun configureSpinner(spinner: Spinner)
    {
        // create an array from resources
        val formList = resources.getStringArray(R.array.word_form)
        // create an adapter for the form-spinner
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.word_form,android.R.layout.simple_spinner_item)
        // some configurations here
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val viewGroup = findViewById<FrameLayout>(R.id.form_details)
                // empty all views
                viewGroup.removeAllViews()
                // first remove all view groups...
                when(formList[position])
                {
                    "Verb" ->
                    {
                        val layout = layoutInflater.inflate(R.layout.verb_details_layout,viewGroup)
                        val widgets = getFormDetailsWidgets(Form.VERB,layout)
                        val auxAdapter = ArrayAdapter.createFromResource(
                                this@FlashcardEditActivity,
                                R.array.aux,
                                android.R.layout.simple_spinner_item)
                        auxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        (widgets["aux"] as Spinner).adapter = auxAdapter

                        // configure aux. verb
                        populateFormDetails(entry,widgets)
                    }
                    "Noun" -> {
                        val layout = layoutInflater.inflate(R.layout.noun_details_layout,viewGroup)
                        val widgets = getFormDetailsWidgets(Form.NOUN,layout)
                        val genderAdapter = ArrayAdapter.createFromResource(
                                this@FlashcardEditActivity,
                                R.array.gender,
                                android.R.layout.simple_spinner_item
                        )
                        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        (widgets["gender"] as Spinner).adapter = genderAdapter

                        populateFormDetails(entry,widgets)
                    }
                    in listOf("Adjective","Adverb") -> {
                        val layout = layoutInflater.inflate(R.layout.adj_details_layout,viewGroup)
                        val widgets = getFormDetailsWidgets(Form.ADJ,layout) // whether its an adj or adv should matter here
                        populateFormDetails(entry,widgets)
                    }
                    else -> {} // impossible
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        spinner.adapter = adapter

        // pre-set the values for this spinner when an entry is given
        if (entry != null)
        {
            formSpinner.setSelection(formList.indexOf(when(entry){
                is Verb -> "Verb"
                is Noun -> "Noun"
                is Adjective -> "Adjective"
                else -> "Adverb"
            }))
            formSpinner.isEnabled = false

            adapter.notifyDataSetChanged() // trigger the sub-field rendering
        }
    }

    private fun getFormDetailsWidgets(form : Form,detailLayout: View) : Map<String,View>
        = when (form)
        {
            Form.VERB ->
            {
                mapOf(
                "aux" to detailLayout.findViewById<Spinner>(R.id.aux),
                        "present_tense" to detailLayout.findViewById<TextInputEditText>(R.id.present_tense),
                        "past_tense" to detailLayout.findViewById<TextInputEditText>(R.id.past_tense),
                        "perfect_tense" to detailLayout.findViewById<TextInputEditText>(R.id.perfect_tense)
                )
            }
            Form.NOUN ->
            {
                mapOf(
                        "gender" to detailLayout.findViewById<Spinner>(R.id.gender),
                        "genitive" to detailLayout.findViewById<TextInputEditText>(R.id.genitive),
                        "plural" to detailLayout.findViewById<TextInputEditText>(R.id.plural)
                )
            }
            else -> { // adj/adv
                mapOf(
                        "comparative" to detailLayout.findViewById<TextInputEditText>(R.id.comparative),
                        "superlative" to detailLayout.findViewById<TextInputEditText>(R.id.superlative)
                )
            }
        }

    private fun populateFormDetails(entry: Entry?, widgets: Map<String,View>)
    {
        if(entry == null) return
        when(entry)
        {
            is Verb ->
            {
                // TODO: can this not be that hard-coded??
                with(widgets["aux"] as Spinner) {
                    setSelection(if (entry.aux == "sein") 1 else 0) // default is 0 (i.e. haben)...}
                    isEnabled = false
                }
                // the rest is just the same... map it
                listOf("present_tense","past_tense","perfect_tense").forEach {
                    tense -> (widgets[tense] as TextInputEditText).text = Editable.Factory().newEditable(when(tense)
                    {
                        "present_tense" -> entry.singleSimplePresent
                        "past_tense" -> entry.pastTense
                        "perfect_tense" -> entry.perfect
                        else -> ""      // this is impossible, but anyways...
                    })
                }

            }

            is Noun ->
            {
                with(widgets["gender"] as Spinner) {
                    setSelection(when (entry.gender) {
                        Gender.DER -> 0
                        Gender.DIE -> 1
                        Gender.DAS -> 2
                    })
                    isEnabled = false
                }
                // the rest is ... also the same
                listOf("genitive","plural").forEach {
                    form -> (widgets[form] as TextInputEditText).text = Editable.Factory().newEditable(when(form)
                    {
                        "genitive" -> entry.genitive
                              else -> entry.plural
                    })
                }
            }

            is Adjective -> { // Adjective / Adverb
                (widgets["comparative"] as TextInputEditText).text = Editable.Factory().newEditable(entry.comparative)
                (widgets["superlative"] as TextInputEditText).text = Editable.Factory().newEditable(entry.superlative)
            }

            is Adverb -> {
                (widgets["comparative"] as TextInputEditText).text = Editable.Factory().newEditable(entry.comparative)
                (widgets["superlative"] as TextInputEditText).text = Editable.Factory().newEditable(entry.superlative)
            }

            else -> {} // Impossible
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.flashcard_edit_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.flashcard_add)
        {
            val intent = Intent()
            intent.putExtra("entry",entry)
            setResult(444,intent)
            // TODO: really save the flashcard
            finish()
        }
        return true
    }
}
