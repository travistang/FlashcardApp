package travis.diy.flashcardapp
import org.junit.Test

import org.junit.Assert.*
/**
 * Created by travistang on 22/11/2017.
 */
class ParseHelperTest {
    @Test
    fun testParseLanguageSection() {
        val inputStr = """==German== ===Etymology=== From {{inh|de|gmh|versprechen||to guarantee}}, from {{inh|de|goh|firsprehhan||to defend verbally, to warrant}}. Analyzable as {{prefix|ver|sprechen|lang=de}}. The sense “to misspeak” is a later transparent derivation (see the prefix). ===Pronunciation=== * {{IPA|/fɛɐ̯ˈʃpʁɛçən/|/fɐ-/|[-ˈʃpʁɛçən]|[-ˈʃpʁɛçn̩]|lang=de}} * {{audio|De-versprechen.ogg|audio|lang=de}} ===Verb=== {{de-verb-strong|class=4|verspricht|versprach|versprochen|past subjunctive=verspräche}} # {{lb|de|transitive}} to [[promise]] #: ''Ich '''verspreche''' es dir.'' #:: I '''promise''' you. (''Literally:'' I promise it to you.) #: ''Er hat '''versprochen''', Kuchen mitzubringen.'' #:: He’s '''promised''' to bring along cake. #* '''2010''', ''{{w|Der Spiegel}}'', issue [http://www.spiegel.de/spiegel/print/index-2010-52.html 52/2010], page 26: #*: Wirtschaft und Regierung blicken mit Zuversicht ins nächste Jahr. 2011 '''verspricht''' eine Fortsetzung des Aufschwungs. #*:: Industry and government look with confidence into the next year. 2011 promises a continuation of the economic upswing. # {{lb|de|transitive|with reflexive dative}} to [[expect]] (something positive); to [[hope]] for #: ''Ich '''verspreche''' mir viel davon.'' #:: ''I '''expect''' a lot from it.'' (''Literally:'' I promise myself a lot...) # {{lb|de|reflexive|with dative object}} to [[promise]] oneself (to) #: ''Er hatte sich einer Frau '''versprochen''' und heiratete dann eine andere.'' #:: He had '''promised''' himself to one woman and then married another. # {{lb|de|reflexive}} to make a [[verbal]] [[slip]]; to [[misspeak]] #: ''Entschuldigung, ich hatte mich '''versprochen.''' Ich meinte „hundert“, nicht „tausend“.'' #:: Sorry, I '''misspoke'''. I meant “a hundred”, not “a thousand”. ====Conjugation==== {{de-conj-strong|versprech|versprach|versprochen|h||versprich|verspräch|a|a}} ====Synonyms==== * {{sense|to promise}} {{l|de|geloben}}; {{l|de|schwören}} ====Derived terms==== * {{l|de|Versprechen}} * {{l|de|Versprecher}} * {{l|de|Versprechung}} * {{l|de|vielversprechend}} ===Further reading=== * {{R:Duden}}"""
        val germanSectionMatchResult = nSectionRegex(2)
                .findAll(inputStr)
                .firstOrNull{matchResult ->  matchResult.groups["tag"]!!.value == "German"}
                ?:assert(false) as MatchResult
        val entry = parseLanguageSection(germanSectionMatchResult,"versprechen")
        assertNotNull(entry)
    }

    @Test
    fun testParseEntry() {
        val inputStr = """==German== ===Etymology=== From {{inh|de|gmh|versprechen||to guarantee}}, from {{inh|de|goh|firsprehhan||to defend verbally, to warrant}}. Analyzable as {{prefix|ver|sprechen|lang=de}}. The sense “to misspeak” is a later transparent derivation (see the prefix). ===Pronunciation=== * {{IPA|/fɛɐ̯ˈʃpʁɛçən/|/fɐ-/|[-ˈʃpʁɛçən]|[-ˈʃpʁɛçn̩]|lang=de}} * {{audio|De-versprechen.ogg|audio|lang=de}} ===Verb=== {{de-verb-strong|class=4|verspricht|versprach|versprochen|past subjunctive=verspräche}} # {{lb|de|transitive}} to [[promise]] #: ''Ich '''verspreche''' es dir.'' #:: I '''promise''' you. (''Literally:'' I promise it to you.) #: ''Er hat '''versprochen''', Kuchen mitzubringen.'' #:: He’s '''promised''' to bring along cake. #* '''2010''', ''{{w|Der Spiegel}}'', issue [http://www.spiegel.de/spiegel/print/index-2010-52.html 52/2010], page 26: #*: Wirtschaft und Regierung blicken mit Zuversicht ins nächste Jahr. 2011 '''verspricht''' eine Fortsetzung des Aufschwungs. #*:: Industry and government look with confidence into the next year. 2011 promises a continuation of the economic upswing. # {{lb|de|transitive|with reflexive dative}} to [[expect]] (something positive); to [[hope]] for #: ''Ich '''verspreche''' mir viel davon.'' #:: ''I '''expect''' a lot from it.'' (''Literally:'' I promise myself a lot...) # {{lb|de|reflexive|with dative object}} to [[promise]] oneself (to) #: ''Er hatte sich einer Frau '''versprochen''' und heiratete dann eine andere.'' #:: He had '''promised''' himself to one woman and then married another. # {{lb|de|reflexive}} to make a [[verbal]] [[slip]]; to [[misspeak]] #: ''Entschuldigung, ich hatte mich '''versprochen.''' Ich meinte „hundert“, nicht „tausend“.'' #:: Sorry, I '''misspoke'''. I meant “a hundred”, not “a thousand”. ====Conjugation==== {{de-conj-strong|versprech|versprach|versprochen|h||versprich|verspräch|a|a}} ====Synonyms==== * {{sense|to promise}} {{l|de|geloben}}; {{l|de|schwören}} ====Derived terms==== * {{l|de|Versprechen}} * {{l|de|Versprecher}} * {{l|de|Versprechung}} * {{l|de|vielversprechend}} ===Further reading=== * {{R:Duden}}"""
        val entry = parseEntry(inputStr,"versprechen")
        assertNotNull(entry)
        assert(entry is Verb)
        assert((entry as Verb).perfect == "versprochen")
    }
}