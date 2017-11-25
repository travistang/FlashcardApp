package travis.diy.flashcardapp

import android.os.Parcel
import android.os.Parcelable
import android.view.animation.AnticipateOvershootInterpolator
import kotlin.reflect.KClass

/**
 * Created by travistang on 7/11/2017.
 */
abstract class Entry(open val word: String,
                     open val meaning: List<String>,
                     open val type: Form) : Parcelable

data class Verb(
        override val word: String,
        override val meaning: List<String>,
        // present tense
        val singleSimplePresent: String, // e.g. kommt (kommen)

        // past tense
        val pastTense: String, // e.g. kam (kommen)
        val aux: String?,
        val perfect: String // e.g. gekommen (kommen)
) : Entry(word, meaning, Form.VERB), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.createStringArrayList(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(word)
        writeStringList(meaning)
        writeString(singleSimplePresent)
        writeString(pastTense)
        writeString(aux)
        writeString(perfect)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Verb> = object : Parcelable.Creator<Verb> {
            override fun createFromParcel(source: Parcel): Verb = Verb(source)
            override fun newArray(size: Int): Array<Verb?> = arrayOfNulls(size)
        }
    }
}

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
}

data class Noun(
        override val word: String,
        override val meaning: List<String>,
        val genitive: String,
        val gender: Gender,
        val plural: String

) : Entry(word, meaning, Form.NOUN), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.createStringArrayList(),
            source.readString(),
            Gender.values()[source.readInt()],
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(word)
        writeStringList(meaning)
        writeString(genitive)
        writeInt(gender.ordinal)
        writeString(plural)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Noun> = object : Parcelable.Creator<Noun> {
            override fun createFromParcel(source: Parcel): Noun = Noun(source)
            override fun newArray(size: Int): Array<Noun?> = arrayOfNulls(size)
        }
    }
}

data class Adjective(
        override val word: String,
        override val meaning: List<String>,
        val comparative: String,
        val superlative: String
) : Entry(word, meaning, Form.ADJ), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.createStringArrayList(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(word)
        writeStringList(meaning)
        writeString(comparative)
        writeString(superlative)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Adjective> = object : Parcelable.Creator<Adjective> {
            override fun createFromParcel(source: Parcel): Adjective = Adjective(source)
            override fun newArray(size: Int): Array<Adjective?> = arrayOfNulls(size)
        }
    }
}

//typealias Adverb = Adjective
data class Adverb(override val word: String,
                  override val meaning: List<String>,
                  val comparative: String?,
                  val superlative: String?
) : Entry(word, meaning, Form.ADV), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.createStringArrayList(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(word)
        writeStringList(meaning)
        writeString(comparative)
        writeString(superlative)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Adverb> = object : Parcelable.Creator<Adverb> {
            override fun createFromParcel(source: Parcel): Adverb = Adverb(source)
            override fun newArray(size: Int): Array<Adverb?> = arrayOfNulls(size)
        }
    }
}
