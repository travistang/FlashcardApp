package travis.diy.flashcardapp

import java.util.regex.Matcher
import java.util.regex.Pattern
/**
 * Created by travistang on 8/11/2017.
 */
// helper function for retrieving a section
// a section is something like ==...==...........
fun nSectionRegex(n: Int): Pattern
{
    val eq = "=".repeat(n)
    return Pattern.compile("""(?<!=)$eq(?<tag>[^=]*?)$eq(?!=)(?<content>.+?)(?=(?<!=)={2,$n}(?!=)|${'$'})""")
}

fun getTagsAndComments(str: String,regex: String) : Map<String,String>
{
    val matcher = Pattern.compile(regex).matcher(str)
    val res = mutableMapOf<String,String>()
    while(matcher.find())
    {
        res[matcher.group("tag")] = matcher.group("content")
    }
    return res.toMap()
}
/*
    Expect a string of wikiText of the german section
    i.e. the string should be of the form ==German==.........===....===......
    This converts such string into an entry object, or return null if such string is in an invalid format
 */
fun parseLanguageSection(deContent: String,word: String): Entry?
{
    // some helper functions
    val doubleCurlyBracketRegex = "\\{\\{(?<tag>.+?)\\}\\}"
    // convert a string followed by a triple-equal section to a map of {{...|..|....}} -> ...
    // where the key is the tags inside each double-curly brackets found in the string
    // and the value is the string following the double-curly brackets
    val parseSubSentences : (String) -> Pair<List<String>,String>? = fun(s) : Pair<List<String>,String>?
    {
        val (tag,content) = getTagsAndComments(s,"""$doubleCurlyBracketRegex(?<content>[^#{]+)""")
                .toList()
                .firstOrNull()?: return null
        return Pair(tag.split("|"),content)
    }

    val partOfSpeeches = arrayOf("Verb","Noun","Adjective","Adverb")
    // extract useful content
    // which means ==deTag==deContent...

    // purify string
    var curstr = deContent

    // remove 4 double-equal sections
    curstr = nSectionRegex(4).matcher(curstr).replaceAll("") // remove sections with 4 equals (Ethmology...)
            .replace("\\n","") // remove newline spaces
            .replace("[[","") // remove open double square brackets
            .replace("]]","") // remove close double square brackets

    // curstr should now have the form
    // for each section with format ===...===...{{....|...|...}}......
    val matcher = nSectionRegex(3).matcher(curstr)
    var tag : String? = null
    var content : String? = null
    while (matcher.find())
    {
        if(matcher.group("tag") in partOfSpeeches)
        {
            tag = matcher.group("tag")
            content = matcher.group("content")
            break
        }
    }
    if ((tag == null) or (content == null)) return null
    // work on each sub items, starting with *

    /*
        General information for words in any form

     */
    /*
        subSentenceMap is now a map with a list of attributes of the double curly brackets found in each "subsentence" (separated by "#")
        for the above example, this should now be:
        mapOf(
            listOf("de-verb-strong","class=4","verspricht","versprach","versprochen"...) to "",
            listOf("lb","de","transitive") to "to promise",
        )

        All sub-sentences without a double-curly bracket are dropped as they do not match the regex in parseSubSentences
    */
    val subSentenceMap = content!!.split("#").mapNotNull(parseSubSentences)
    var meaning : MutableList<String> = mutableListOf() // list of meaning, will be concatenated by ";"
    if (subSentenceMap.isEmpty())
    {
        // TODO: probably no word form input... Allow listener as input and call it when this encounters to request another entry
        // TODO: still parse the entry and try to get the original word form directly
        return null
    }
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
            var formDict : MutableMap<String,String> = mutableMapOf()

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

        "Noun" ->
        {
            // TODO: parse a noun entry
            var genitive : String? = null
            var plural : String? = null
            var meaning: List<String>? = null
            subSentenceMap.forEachIndexed{
                i,(dcParts,subsentenceContent) ->
                when
                {
                    dcParts[0] == "de-noun" ->
                    {
                        genitive = dcParts[2].replace("^.*=","")
                        plural = dcParts[3]
                        // keep retrieving the meaning until a "star" is seen at the beginning of the sentence
                        meaning = content!!.split("#")
                                .subList(i + 1,content!!.count { c -> c == '#'} + 1)
                                .takeWhile { part -> part[0] !in listOf(':','*') }
                        return try {
                             Noun(
                                     word = word,
                                     meaning = meaning!!,
                                     gender = when (dcParts[1]){
                                         "f" -> Gender.DIE
                                         "m" -> Gender.DER
                                         "n" -> Gender.DAS
                                         else -> return null
                                     },
                                     plural = plural!!,
                                     genitive = genitive!!
                            )
                        }catch( _ : Throwable) { return null}
                    }
                }
            }
            return null // no sub-sentences, wtf..
        } // end of noun-parsing

        "Adjective" -> {
            var comp : String? = null
            var sup: String? = null
            var meaning: List<String>? = null
            subSentenceMap.forEachIndexed {
                i,(dcParts,subsentenceContent) ->
                    when
                    {
                        dcParts[0] == "de-adj" ->
                        {
                            comp = dcParts[1]
                            sup = dcParts[2]

                            // some regular adj.
                            if(comp!! in listOf("er","r"))
                            {
                                comp = word + comp
                            }
                            if(sup!! in listOf("ten","sten"))
                            {
                                sup = word + sup
                            }

                            // now deal wit the meaning of the adj.
                            val partialMeaningList = content!!.split("#")
                                    .subList(i + 1,content!!.count { c -> c == '#'} + 1)
                                    // get the meaning while the next entry matches
                                    .takeWhile { part -> !listOf("""^\*.*""","""^:.+""","""^ \{\{.*""")
                                            .any{regStr -> regStr.toRegex().matches(part)} }
                            // append meaning
                            meaning = if(meaning == null) partialMeaningList else {meaning!! + partialMeaningList}

                        }
                        dcParts[0] == "lb" -> {
                            val partialMeaningList = subsentenceContent.split("#")
                                    // get the meaning while the next entry matches
                                    .takeWhile { part -> !listOf("""^\*.*""","""^:.+""","""^ \{\{.*""")
                                            .any{regStr -> regStr.toRegex().matches(part)} }
                            // append meaning
                            meaning = if(meaning == null) partialMeaningList else {meaning!! + partialMeaningList}
                        }
                    }
            } // end forEachIndexed
            return try {
                Adjective(
                        word = word,
                        meaning = meaning!!,
                        comparative = comp!!,
                        superlative = sup!!
                )
            }catch( _ : Throwable) { return null}
        }// end adj/adv handling

        "Adverb" ->
        {
            var comp : String? = null
            var sup: String? = null
            var meaning: List<String>? = null
            subSentenceMap.forEachIndexed {
                i,(dcParts,subsentenceContent) ->
                when
                {
                    dcParts[0] == "de-adv" ->
                    {
                        comp = dcParts.getOrNull(1)
                        sup = dcParts.getOrNull(2)

                        // some regular adj.
                        if(comp in listOf("er","r"))
                        {
                            comp = word + comp
                        }
                        if(sup in listOf("ten","sten"))
                        {
                            sup = word + sup
                        }

                        // now deal wit the meaning of the adj.
                        val partialMeaningList = content!!.split("#")
                                .subList(i + 1,content!!.count { c -> c == '#'} + 1)
                                // get the meaning while the next entry matches
                                .takeWhile { part -> !listOf("""^\*.*""","""^:.+""","""^ \{\{.*""")
                                        .any{regStr -> regStr.toRegex().matches(part)} }
                        // append meaning
                        meaning = if(meaning == null) partialMeaningList else {meaning!! + partialMeaningList}

                    }
                    dcParts[0] == "lb" -> {
                        val partialMeaningList = subsentenceContent.split("#")
                                // get the meaning while the next entry matches
                                .takeWhile { part -> !listOf("""^\*.*""","""^:.+""","""^ \{\{.*""")
                                        .any{regStr -> regStr.toRegex().matches(part)} }
                        // append meaning
                        meaning = if(meaning == null) partialMeaningList else {meaning!! + partialMeaningList}
                    }
                }
            } // end forEachIndexed
            return try {
                Adverb(
                        word = word,
                        meaning = meaning!!,
                        comparative = comp,
                        superlative = sup
                )
            }catch( _ : Throwable) { return null}
        }
        // TODO: complete other word form as well.
        else -> {return null }
    }

}

fun parseEntry(pageStr: String,word: String) : Entry?
{
    // "purify" the string first
    val purifiedString = pageStr.replace("\n"," ")
    //target is to retrieve the ==German==..... section
    val (_,content) = getTagsAndComments(purifiedString, nSectionRegex(2).pattern())
            .toList()
            .firstOrNull{(tag,_) -> tag == "German"}
            ?: return null
    return parseLanguageSection(content,word)
}