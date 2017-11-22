package travis.diy.flashcardapp
import org.junit.Test

import org.junit.Assert.*
/**
 * Created by travistang on 22/11/2017.
 */
class WiktionaryServiceTest {
    @Test
    fun testParseResponse()
    {
        val searchWord = "versprechen"
        val service = WiktionaryService()
        var entry : Entry? = null
        service.search(searchWord, {
            observable ->
            observable.doOnNext { entry -> System.out.println(entry.toString()) }

                    .subscribe{e -> when (e) {
                        is Error -> {}
                        is Entry -> entry = e
                    }}
        })
        Thread.sleep(10000)
        assertTrue(entry is Verb)
    }
}