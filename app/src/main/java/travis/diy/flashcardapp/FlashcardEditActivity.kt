package travis.diy.flashcardapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.text.Editable
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.entry = intent.extras.get("entry") as Entry?
        this.text = intent.extras.get("text") as String

        setContentView(R.layout.flashcard_edit)
        // configure widgets
        title = "Editing %s".format(text)

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
                // TODO: fill in the missing functions
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
                    "Adjective" -> {}
                    "Adverb" -> {}
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
            else -> {emptyMap()} // TODO: gather the widgets for the rest of the word form here
        }

    private fun populateFormDetails(entry: Entry?, widgets: Map<String,View>)
    {
        if(entry == null) return
        when(entry)
        {
            is Verb ->
            {
                // TODO: can this not be that hard-coded??
                (widgets["aux"] as Spinner).setSelection(if(entry.aux == "sein") 1 else 0) // 0 includes "haben" es well...
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
                (widgets["gender"] as Spinner).setSelection(when(entry.gender)
                {
                    Gender.DER -> 0
                    Gender.DIE -> 1
                    Gender.DAS -> 2
                })

                // the rest is ... also the same
                listOf("genitive","plural").forEach {
                    form -> (widgets[form] as TextInputEditText).text = Editable.Factory().newEditable(when(form)
                    {
                        "genitive" -> entry.genitive
                              else -> entry.plural
                    })
                }
            }
            else -> {
                // TODO: populate form details given other word form
            }
        }
    }

}
