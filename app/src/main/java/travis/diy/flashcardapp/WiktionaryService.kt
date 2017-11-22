package travis.diy.flashcardapp

import okhttp3.*
import org.xml.sax.InputSource
import rx.Observable
import rx.Subscriber
import rx.Subscription
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
/**
 * Created by travistang on 6/11/2017.
 */
class WiktionaryService {
    private var url = "https://en.wiktionary.org/w/api.php?format=xml&action=query&titles=%s&rvprop=content&prop=revisions&redirects=1"
    private var subscription: Subscription? = null
    private var queryObservable: Observable<in Entry?>? = null
    fun stopSearching()
    {
        subscription?.unsubscribe()
    }
    private fun createQueryObservableWithString(s: String): Observable<in Entry?>?
    {
        queryObservable = Observable.unsafeCreate(object: Observable.OnSubscribe<Entry?>
        {
            override fun call(subscriber: Subscriber<in Entry?>)
            {
                stopSearching()
                val request = Request.Builder()
                        .url(url.format(s))
                        .build()
                OkHttpClient().newCall(request).enqueue(object: Callback
                {
                    override fun onResponse(call: Call?, response: Response?)
                    {
                        val parsedResponse = parseResponse(response)
                        when (parsedResponse)
                        {
                            is Error -> subscriber.onError(Throwable(parsedResponse.toString()))
                            is Entry -> subscriber.onNext(parsedResponse)
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?)
                    {
                        subscriber.onError(e)
                    }
                })


            }
        })
        return queryObservable
    }
    enum class Error(val i: Int)
    {
        NO_SUCH_WORD(0),
        NOT_GERMAN(1),
        BAD_RESPONSE(2),
        UNKNOWN_WORD_FORM(3),
    }


    private fun parseResponse(response: Response?): Any
    {
        // check if the response is null / successful
        response?:return Error.BAD_RESPONSE
        if(!response.isSuccessful) return Error.BAD_RESPONSE

        // parse response as object
        val obj = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(InputSource(
                        StringReader(
                                response.body()?.string()
                        )
                ))

        // get the title of the word
        val title = obj.getElementsByTagName("page").item(0).attributes.getNamedItem("title").nodeValue
        val dictString = obj.getElementsByTagName("rev").item(0).nodeValue
        // TODO: parse this string
        // TODO: remove me
        // TODO: how about adding listener to call for event?
        return parseEntry(dictString,title)?:Error.NOT_GERMAN //TODO: how do you even know what has gone wrong if you get a null?
    }

    // functions for parsing strings with known tags
    private fun String.replaceDoubleBracketSingleStroke() : String =
            """\{\{[\w&&[^|]&&[-]]\}\}"""
            .toRegex()
            .replace(this,"")


    fun searchWord(str: String,subscriber: Subscriber<out Entry?>) : Observable<out Entry?>?
    {
//        subscription = createQueryObservableWithString(str)?.subscribe(subscriber)
        //TODO: this
        return null
    }

}