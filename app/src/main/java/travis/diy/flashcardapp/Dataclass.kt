package travis.diy.flashcardapp

import android.view.animation.AnticipateOvershootInterpolator
import kotlin.reflect.KClass

/**
 * Created by travistang on 7/11/2017.
 */
abstract class Entry(open val word: String,
                     open val meaning: List<String>,
                     open val type: Form)

data class Verb(
        override val word: String,
        override val meaning: List<String>,
        // present tense
        val singleSimplePresent: String, // e.g. kommt (kommen)

        // past tense
        val pastTense: String, // e.g. kam (kommen)
        val aux: String?,
        val perfect: String // e.g. gekommen (kommen)
) : Entry(word,meaning,Form.VERB)

// For the nouns
enum class Gender(val short: Char)
{
    DER('r'),
    DIE('e'),
    DAS('s'),
}

enum class Form(val cls: KClass<out Any>)
{
    VERB(Verb::class),
    NOUN(Noun::class),
    ADJ(Adjective::class),
    ADV(Adverb::class),
    PRONOUN(Pronoun::class),
}
data class Noun(
        override val word: String,
        override val meaning: List<String>,
        val gender: Gender,
        val plural: String

) : Entry(word,meaning,Form.NOUN)

data class Adjective(
        override val word: String,
        override val meaning: List<String>,
        val comparative: String,
        val superlative: String
) : Entry(word,meaning,Form.ADJ)

data class Adverb(override val word: String,
                  override val meaning: List<String>
) : Entry(word,meaning,Form.ADV)

data class Pronoun(override val word: String,
            override val meaning: List<String>
) : Entry(word,meaning,Form.PRONOUN)