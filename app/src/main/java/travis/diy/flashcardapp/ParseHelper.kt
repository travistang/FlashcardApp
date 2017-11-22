package travis.diy.flashcardapp

import java.util.Comparator

/**
 * Created by travistang on 8/11/2017.
 */
// helper function for retrieving a section
// a section is something like ==...==...........
fun nSectionRegex(n: Int): Regex
{
    val eq = "=".repeat(n)
    return """(?<!=)$eq(?<tag>[^=]*?)$eq(?!=)(?<content>.+?)(?=(?<!=)={2,$n}(?!=)|${'$'})""".toRegex()
}

/*
    Expect a string of wikiText of the german section
    i.e. the string should be of the form ==German==.........===....===......
    This converts such string into an entry object, or return null if such string is in an invalid format
 */
fun parseLanguageSection(germanSectionMatchResult: MatchResult,word: String): Entry?
{
    // some helper functions
    val doubleCurlyBracketRegex = "\\{\\{(?<tag>.+?)}}"

    // convert a string followed by a triple-equal section to a map of {{...|..|....}} -> ...
    // where the key is the tags inside each double-curly brackets found in the string
    // and the value is the string following the double-curly brackets
    val parseSubSentences : (String) -> Pair<List<String>,String>? = fun(s) : Pair<List<String>,String>?
    {
        val match = """$doubleCurlyBracketRegex(?<content>[^#{]+)"""
                .toRegex()
                .find(s)?:return null

        return Pair(match.groups["tag"]!!.value.split("|"),match.groups["content"]!!.value)
    }

    val partOfSpeeches = arrayOf("Verb","Noun","Pronoun","Adjective","Adverb")
    // extract useful content
    // which means ==deTag==deContent...
    val deContent = germanSectionMatchResult.groups["content"]!!

    // purify string
    var curstr = deContent.value

    // remove 4 double-equal sections
    curstr = nSectionRegex(4).replace(curstr,"") // remove sections with 4 equals (Ethmology...)
            .replace("\\n","") // remove newline spaces
            .replace("[[","") // remove open double square brackets
            .replace("]]","") // remove close double square brackets

    // curstr should now have the form
    // for each section with format ===...===...{{....|...|...}}......
    val matchResult = nSectionRegex(3)
            .findAll(curstr) // find all sections that describes the word form of the given word
            /*
                TODO: not sure whether there are words that have multiple word forms in German...
                Currently only the first word form is processed. Hopefully other word forms can be supported later
             */
            .first{ matchResult ->
                matchResult.groups["tag"]?.value in partOfSpeeches
            }

    // matching ===*tag*===*content* ...., where tag is one of "Noun","Verb"...
    val tag = matchResult.groups["tag"]!!.value
    val content = matchResult.groups["content"]!!.value
    // work on each sub items, starting with *
    // the
    when (tag)
    {
        "Verb" ->
        {
            /*
                Then content should be, for example:

                {{de-verb-strong|class=4|verspricht|versprach|versprochen|past subjunctive=verspräche}}
                # {{lb|de|transitive}} to promise
                #: ''Ich '''verspreche''' es dir.''
                #:: I '''promise''' you. (''Literally:'' I promise it to you.)
                ...


             */
            val subSentenceMap = content.split("#").mapNotNull(parseSubSentences)
            /*
                subSentenceMap is now a map with a list of attributes of the double curly brackets found in each "subsentence" (separated by "#")
                for the above example, this should now be:
                mapOf(
                    listOf("de-verb-strong","class=4","verspricht","versprach","versprochen"...) to "",
                    listOf("lb","de","transitive") to "to promise",
                )

                All sub-sentences without a double-curly bracket are dropped as they do not match the regex in parseSubSentences

            */

            var meaning : MutableList<String> = mutableListOf() // list of meaning, will be concatenated by ";"
            var formDict : MutableMap<String,String> = mutableMapOf()

            if (subSentenceMap.isEmpty())
            {
                // TODO: probably no word form input... Allow listener as input and call it when this encounters to request another entry
                // TODO: still parse the entry and try to get the original word form directly
                return null
            }
            subSentenceMap.forEach{
                (dcParts,subsentenceContent) ->
                when
                {
                    dcParts[0] == "de-verb-strong" ->
                    {
                        // we have the word form tag...
                        // TODO: collect word forms to the entry
                        formDict.put("present",dcParts[2])
                        formDict.put("past",dcParts[3])
                        formDict.put("perfect",dcParts[4])
                        val aux = dcParts.firstOrNull{part -> "aux" in part}
                        if(aux != null) {
                            val a = if("haben" in aux) "haben" else "sein"
                            formDict.put("aux", a)
                        }
                    }

                    (dcParts[0] == "lb") and (dcParts[1] == "de") ->
                    {
                        // we have a meaning for this word!
                        meaning.add(subsentenceContent)
                    }
                    else -> {} // neither of these... what is that?
                }
            }
            return try {
                Verb(
                        word = word,
                        meaning = meaning,
                        singleSimplePresent = formDict["present"]!!,
                        pastTense = formDict["past"]!!,
                        perfect = formDict["perfect"]!!,
                        aux = formDict["aux"]
                )
            }catch ( _ : Throwable)
            {
                return null // some attributes are missing
            }
        }
        // TODO: complete other word form as well. Now test the verb-parsing first
        else -> {return null }
    }

}

fun parseEntry(pageStr: String,word: String) : Entry?
{
    //target is to retrieve the ==German==..... section
   val germanSectionMatchResult = nSectionRegex(2)
           .findAll(pageStr)
           .firstOrNull{matchResult ->  matchResult.groups["tag"]!!.value == "German"}
           ?:return null
    return parseLanguageSection(germanSectionMatchResult,word)
}