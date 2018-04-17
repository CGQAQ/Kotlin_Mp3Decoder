/*
 *     Copyright (C)  2018  Jason<m.jason.liu@outlook.com> @CGQAQ
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.cg.decoder.tags

import com.cg.decoder.utils.BitStream
import com.cg.decoder.utils.getItemByIndex
import com.cg.decoder.utils.sandbox
import java.nio.ByteBuffer

class TagV1(val bitStream: BitStream) : ITag {

    var Tag: String = ""
        private set
    var Title: String = ""
        private set
    var Artist: String = ""
        private set
    var Album: String = ""
        private set
    var Year: String = ""
        private set
    var Comment: String = ""
        private set
    var Genre: String = ""
        private set

    val GenreTable: ArrayList<String> = arrayListOf(
            //Genre is a numeric field which may have one of the following values:
            "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies",
            "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
            "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
            "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock",
            "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult",
            "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes",
            "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock",
            //WinAmp expanded this table with next codes:
            "Folk", "Folk-Rock", "National Folk",
            "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "'Gothic Rock", "Progressive Rock", "Psychedelic Rock",
            "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata",
            "Symphony", "Booty Brass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "'Poweer Ballad",
            "Rhytmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A Capela", "Euro-House", "Dance Hall"
    )

    override fun printTag() {
        if (Tag == "TAG") {
            println("\n-------------------------------------------------------------------------")
            print("Title: ${if(Title=="") "Unknown" else Title}\nArtist: ${if(Artist=="") "Unknown" else Artist}\nAlbum: ${if(Album=="") "Unknown" else Album}\nYear: ${if(Year=="") "Unknown" else Year}\nComment: ${if(Comment=="") "Unknown" else Comment}\nGenre: $Genre")
            println("\n-------------------------------------------------------------------------")
        }
    }

    override fun parseTag() {
        var tagData: ByteBuffer
        bitStream.sandbox {
            bitStream.seek(-128)
            tagData = bitStream.readBytes(128)

            for (i in -128..-1) {
                when (i) {
                    in -128..-126 -> {
                        val c = tagData.getItemByIndex(i)
                        if (c != 0.toByte())
                            Tag += c.toChar()
                    }
                    in -125..-96 -> {
                        val c = tagData.getItemByIndex(i)
                        if (c != 0.toByte())
                            Title += c.toChar()
                    }
                    in -95..-66 -> {
                        val c = tagData.getItemByIndex(i)
                        if (c != 0.toByte())
                            Artist += c.toChar()
                    }
                    in -65..-36 ->  {
                        val c = tagData.getItemByIndex(i)
                        if (c != 0.toByte())
                            Album += c.toChar()
                    }
                    in -35..-32 ->  {
                        val c = tagData.getItemByIndex(i)
                        if (c != 0.toByte())
                            Year += c.toChar()
                    }
                    in -31..-2 ->  {
                        val c = tagData.getItemByIndex(i)
                        if (c != 0.toByte())
                            Comment += c.toChar()
                    }
                    -1 -> {
                        Genre += if (tagData.getItemByIndex(i) in 0..115) {
                            GenreTable[tagData.getItemByIndex(i).toInt()]
                        } else {
                            "Unknown"
                        }
                    }
                }
            }
        }
    }
}