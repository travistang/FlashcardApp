package travis.diy.flashcardapp

import android.app.AlertDialog
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.lapism.searchview.SearchAdapter
import com.lapism.searchview.SearchItem
import com.lapism.searchview.SearchView
import org.w3c.dom.Text
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.observables.SyncOnSubscribe
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

//import java.util.*

class MainActivity : AppCompatActivity() {

    val wiki = WiktionaryService()
    private var openSearchSubscription : Subscription? = null
    private var searchBox : SearchView? = null
    private val suggestionList: MutableList<SearchItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "German Flashcards"
    }

    override fun onStart() {
        super.onStart()
        configureSearchBox()
    }

    private fun configureSearchBox()
    {

        // this observable is for the wiktionaryService object to handle the "live" searching options
        openSearchSubscription = Observable.unsafeCreate<String> {
            subscriber: Subscriber<in String>? ->
            searchBox = findViewById(R.id.searchView)
            searchBox?.setOnOpenCloseListener(object: SearchView.OnOpenCloseListener{
                override fun onOpen(): Boolean {
                    // TODO: show search history
                    return true
                }

                override fun onClose(): Boolean {
                    searchBox?.hideProgress()
                    return true
                }
            })
            runOnUiThread {
                // searchBox initialization
                val searchText: (String?) -> Unit = {
                    text ->
                    when(text)
                    {
                        null -> {}
                        else ->
                        {
                            wiki.search(text, {
                                observable ->
                                    observable.doOnNext {
                                        payload ->
                                        if (payload is Error)
                                            runOnUiThread {
                                                challengeUserOnError(text)
                                            }
                                    }
                                    .onErrorReturn { _ -> null }
                                    .subscribe { entry ->
                                        toFlashcardEditingPage(entry as Entry?,text)
                                    }
                        })
                        }
                    }
                }
                val adapter = SearchAdapter(this, suggestionList)
                adapter.setOnSearchItemClickListener{view, position, text ->
                    searchText(text)
                }
                searchBox?.adapter = adapter
                searchBox?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String?): Boolean {
                        subscriber?.onNext(newText)
                        return true
                    }

                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        if (p0 == null) return false
                        searchText(p0)
                        return true
                    }
                })
            }
        } // end of observable definitions
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .doOnNext {
            // this is the time before the result comes but when the user input characters
            _ ->
            runOnUiThread{
//                searchBox?.hideSuggestions()
                searchBox?.showProgress()
            }
        }
        .sample(500L,TimeUnit.MILLISECONDS) // prevent flooding the server...

        .subscribe {
            searchTerm -> wiki.opensearch(searchTerm,{
                openSearchObservable ->
                openSearchObservable.subscribe{
                    openSearchResult ->
                    runOnUiThread{
                        searchBox?.hideProgress()
                        // add all search result upon receiving open search result
                        searchBox?.setSuggestionsList(openSearchResult.map{
                            result -> SearchItem(result)
                        })
                        searchBox?.adapter?.notifyDataSetChanged()
                        // attempt to redraw the activity in order to show the search results
                        searchBox?.showSuggestions()
                    }
                }
            })
        }
    }

    private fun toFlashcardEditingPage(entry: Entry?,text: String)
    {
        //TODO: given the entry from search, lead the user to the search page
        runOnUiThread{
            if(entry == null)
            {
                challengeUserOnError(text)
            }
            else {
                startFlashcardEditActivity(entry,text)
                searchBox?.hideSuggestions()
            }
        }
    }

    private fun challengeUserOnError(word: String)
    {
        // TODO: make a pop-up box to warn the user something has occurred, ask if they should proceed
        AlertDialog.Builder(this)
                .setMessage("%s is either not a german word, or is not a verb/noun/adjective/adverb. Do you want to make a flashcard for this word anyway?".format(word))
                .setPositiveButton("Add",{
                    dialog, which ->
                    startFlashcardEditActivity(null,word)
                })
                .setNegativeButton("Cancel", {
                    // The user is not adding words
                    dialog, which -> dialog.cancel()
                })
                .create()
                .show()
        // TODO: hide suggestions if the user agrees to add the word that has never seen / supported
    }

    private fun startFlashcardEditActivity(entry: Entry?,text: String)
    {
        // TODO: start the activity here
        val intent = Intent(this, FlashcardEditActivity::class.java)
        intent.putExtra("entry",entry)
        intent.putExtra("text",text)
        startActivityForResult(intent,444)
    }
}
