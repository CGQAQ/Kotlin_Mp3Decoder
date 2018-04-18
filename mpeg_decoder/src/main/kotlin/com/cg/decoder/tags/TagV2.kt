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
import com.cg.decoder.utils.sandbox
import javax.print.attribute.standard.Fidelity


/**
 * The first three bytes of the tag are always "ID3" to indicate that this is an ID3v2 tag,
 * directly followed by the two version bytes. The first byte of ID3v2 version is it's major version,
 * while the second byte is its revision number.
 * In this case this is ID3v2.3.0.
 * All revisions are backwards compatible while major versions are not.
 * If software with ID3v2.2.0 and below support should encounter version three or higher it should simply ignore the whole tag.
 * Version and revision will never be $FF.
 *
 *
 *  $49 44 33 yy yy xx zz zz zz zz   total 4Bytes
 *  The ID3v2 tag size is encoded with four bytes where the most significant bit (bit 7) is set to zero in every byte,
 *  making a total of 28 bits. The zeroed bits are ignored, so a 257 bytes long tag is represented as $00 00 02 01.
 */

class TagV2(val bitStream: BitStream) : ITag {
    var version: Int = 0
        private set
    var reversion: Int = 0
        private set

    // flags
    var unsynchronisation: Int = 0
        private set
    var extendedHeader: Int = 0
        private set
    var experimentalIndicator: Int = 0
        private set
    // flags end


    // The ID3v2 tag size is the size of the complete tag after unsychronisation, including padding,
    // excluding the header but not excluding the extended header (total tag size - 10).
    // Only 28 bits (representing up to 256MB) are used in the size description to avoid the introducuction of 'false syncsignals'.
    var size: Int = 0  // Bytes
        private set


    private fun parseHeader() {
        // version and reversion
        val version = bitStream.readBytes(2)
        this.version = version[0].toInt().and(0xFF)
        this.reversion = version[1].toInt().and(0xFF)

        // flags
        val flags = bitStream.readBits(3)
        this.unsynchronisation = flags[0].toInt().and(0x1)
        this.extendedHeader = flags[1].toInt().and(0x1)
        this.experimentalIndicator = flags[2].toInt().and(0x1)

        // size
        val size = bitStream.readBytes(4)
        this.size =
                size[0].toInt().and(0x7F).shl(21) +
                size[1].toInt().and(0x7F).shl(14) +
                size[2].toInt().and(0x7F).shl(7) +
                size[3].toInt().and(0x7F)
    }

    private fun parseExtendedHeader() {
        //optional
        //todo: implements it

    }

    private fun parseFrame() {
        // Frame ID       $xx xx xx xx (four characters)
        val FID = String(bitStream.readBytes(4).array())
        val sizeBits = bitStream.readBytes(4)
        val size = sizeBits[0].toInt().and(0xFF).shl(24) +
                sizeBits[1].toInt().and(0xFF).shl(16) +
                sizeBits[2].toInt().and(0xFF).shl(8) +
                sizeBits[3].toInt().and(0xFF)
        val f1Bits = bitStream.readBytes(1)[0]
        val f2Bits = bitStream.readBytes(1)[0]

        // a - Tag alter preservation
        // This flag tells the software what to do with this frame if it is unknown and the tag is altered in any way.
        // This applies to all kinds of alterations, including adding more padding and reordering the frames.
        //          0    Frame should be preserved.
        //          1    Frame should be discarded.
        val a = f1Bits.toInt().and(0xFF).shr(7)

        // b - File alter preservation
        // This flag tells the software what to do with this frame if it is unknown and the file, excluding the tag, is altered.
        // This does not apply when the audio is completely replaced with other audio data.
        //          0    Frame should be preserved.
        //          1    Frame should be discarded.
        val b = f1Bits.toInt().and(0xFF).shr(6)

        // c - Read only
        // This flag, if set, tells the software that the contents of this frame is intended to be read only.
        // Changing the contents might break something, e.g. a signature.
        // If the contents are changed, without knowledge in why the frame was flagged read only and without taking the proper means to compensate,
        // e.g. recalculating the signature, the bit should be cleared.
        val c = f1Bits.toInt().and(0xFF).shr(5)

        // i - Compression
        // This flag indicates whether or not the frame is compressed.
        //          0   Frame is not compressed.
        //          1   Frame is compressed using [#ZLIB zlib] with 4 bytes for 'decompressed size' appended to the frame header.
        val i = f2Bits.toInt().and(0xFF).shr(7)

        // j - Encryption
        // This flag indicates wether or not the frame is enrypted.
        // If set one byte indicating with which method it was encrypted will be appended to the frame header.
        // See section 4.26.
        // for more information about encryption method registration.
        //          0   Frame is not encrypted.
        //          1    Frame is encrypted.
        val j = f2Bits.toInt().and(0xFF).shr(6)

        // k - Grouping identity
        // This flag indicates whether or not this frame belongs in a group with other frames.
        // If set a group identifier byte is added to the frame header.
        // Every frame with the same group identifier belongs to the same group.
        //          0    Frame does not contain group information
        //          1    Frame contains group information
        val k = f2Bits.toInt().and(0xFF).shr(5)

        if (i != 0) {
            // need decompress content first
            val decompressedSizeBits = bitStream.readBytes(4)
            return
        }
        if (j != 0) {
            // todo It's encrypted, won't handle
            return
        }

        // Frames that allow different types of text encoding contains a text
        // encoding description byte. Possible encodings:
        // $00   ISO-8859-1 [ISO-8859-1]. Terminated with $00.
        // $01   UTF-16 [UTF-16] encoded Unicode [UNICODE] with BOM. All
        // strings in the same frame SHALL have the same byteorder.
        // Terminated with $00 00.
        // $02   UTF-16BE [UTF-16] encoded Unicode [UNICODE] without BOM.
        // Terminated with $00 00.
        // $03   UTF-8 [UTF-8] encoded Unicode [UNICODE]. Terminated with $00.

        val rawString: String = when (bitStream.readBytes(1)[0]) {
            0.toByte() -> {
                // ISO-8859-1
                val content = bitStream.readBytes(size - 1).rewind()
                val decoder = charset("ISO-8859-1")
                decoder.decode(content).toString()
            }
            1.toByte() -> {
                // UTF-16 [UTF-16] encoded Unicode [UNICODE] with BOM
                val content = bitStream.readBytes(size - 1).rewind()
                val decoder = charset("UTF-16")
                val a = decoder.decode(content)
                a.toString()
            }
            2.toByte() -> {
                // UTF-16BE [UTF-16] encoded Unicode [UNICODE] without BOM.
                val content = bitStream.readBytes(size - 1).rewind()
                val decoder = charset("UTF-16BE")
                decoder.decode(content).toString()
            }
            3.toByte() -> {
                // UTF-8 [UTF-8] encoded Unicode [UNICODE]. Terminated with $00.
                val content = bitStream.readBytes(size - 1).rewind()
                val decoder = charset("UTF-8")
                decoder.decode(content).toString()
            }
            else -> "Unknown Charset"
        }


        when (FID) {
            "AENC" -> {
                AENC = rawString
            }
            "APIC" -> {
                APIC = rawString
            }
            "COMM" -> {
                COMM = rawString
            }
            "COMR" -> {
                COMR = rawString
            }
            "ENCR" -> {
                ENCR = rawString
            }
            "EQUA" -> {
                EQUA = rawString
            }
            "ETCO" -> {
                ETCO = rawString
            }
            "GEOB" -> {
                GEOB = rawString
            }
            "GRID" -> {
                GRID = rawString
            }
            "IPLS" -> {
                IPLS = rawString
            }
            "LINK" -> {
                LINK = rawString
            }
            "MCDI" -> {
                MCDI = rawString
            }
            "MLLT" -> {
                MLLT = rawString
            }
            "OWNE" -> {
                OWNE = rawString
            }
            "PCNT" -> {
                PCNT = rawString
            }
            "POPM" -> {
                POPM = rawString
            }
            "POSS" -> {
                POSS = rawString
            }
            "RBUF" -> {
                RBUF = rawString
            }
            "RVAD" -> {
                RVAD = rawString
            }
            "RVRB" -> {
                RVRB = rawString
            }
            "SYLT" -> {
                SYLT = rawString
            }
            "SYTC" -> {
                SYTC = rawString
            }
            "TALB" -> {
                TALB = rawString
            }
            "TBPM" -> {
                TBPM = rawString
            }
            "TCOM" -> {
                TCOM = rawString
            }
            "TCON" -> {
                TCON = rawString
            }
            "TCOP" -> {
                TCOP = rawString
            }
            "TDAT" -> {
                TDAT = rawString
            }
            "TDLY" -> {
                TDLY = rawString
            }
            "TENC" -> {
                TENC = rawString
            }
            "TEXT" -> {
                TEXT = rawString
            }
            "TFLT" -> {
                TFLT = rawString
            }
            "TIME" -> {
                TIME = rawString
            }
            "TIT1" -> {
                TIT1 = rawString
            }
            "TIT2" -> {
                TIT2 = rawString
            }
            "TIT3" -> {
                TIT3 = rawString
            }
            "TKEY" -> {
                TKEY = rawString
            }
            "TLAN" -> {
                TLAN = rawString
            }
            "TLEN" -> {
                TLEN = rawString
            }
            "TMED" -> {
                TMED = rawString
            }
            "TOAL" -> {
                TOAL = rawString
            }
            "TOFN" -> {
                TOFN = rawString
            }
            "TOLY" -> {
                TOLY = rawString
            }
            "TOPE" -> {
                TOPE = rawString
            }
            "TORY" -> {
                TORY = rawString
            }
            "TOWN" -> {
                TOWN = rawString
            }
            "TPE1" -> {
                TPE1 = rawString
            }
            "TPE2" -> {
                TPE2 = rawString
            }
            "TPE3" -> {
                TPE3 = rawString
            }
            "TPE4" -> {
                TPE4 = rawString
            }
            "TPOS" -> {
                TPOS = rawString
            }
            "TPUB" -> {
                TPUB = rawString
            }
            "TRCK" -> {
                TRCK = rawString
            }
            "TRDA" -> {
                TRDA = rawString
            }
            "TRSN" -> {
                TRSN = rawString
            }
            "TRSO" -> {
                TRSO = rawString
            }
            "TSIZ" -> {
                TSIZ = rawString
            }
            "TSRC" -> {
                TSRC = rawString
            }
            "TSSE" -> {
                TSSE = rawString
            }
            "TYER" -> {
                TYER = rawString
            }
            "TXXX" -> {
                TXXX = rawString
            }
            "UFID" -> {
                UFID = rawString
            }
            "USER" -> {
                USER = rawString
            }
            "USLT" -> {
                USLT = rawString
            }
            "WCOM" -> {
                WCOM = rawString
            }
            "WCOP" -> {
                WCOP = rawString
            }
            "WOAF" -> {
                WOAF = rawString
            }
            "WOAR" -> {
                WOAR = rawString
            }
            "WOAS" -> {
                WOAS = rawString
            }
            "WORS" -> {
                WORS = rawString
            }
            "WPAY" -> {
                WPAY = rawString
            }
            "WPUB" -> {
                WPUB = rawString
            }
            "WXXX" -> {
                WXXX = rawString
            }
        }
    }

    override fun parseTag() {

        bitStream.sandbox {
            val ID3 = String(it.readBytes(3).array())
            if (ID3 == "ID3") {
                //存在
                parseHeader()
                parseExtendedHeader()
                while (bitStream.position() < this.size) {
                    //It's ok to not minus 10(ID3tag HeaderBean size)
                    parseFrame()
                }
            } else {
                //不存在
                return@sandbox
            }
        }
    }

    override fun printTag() {
        val _AENC = if (AENC == defultValue) "" else "Audio encryption:  $AENC\n"
        val _APIC = if (APIC == defultValue) "" else "Attached picture:  $APIC\n"
        val _COMM = if (COMM == defultValue) "" else "Comments:  $COMM\n"
        val _COMR = if (COMR == defultValue) "" else "Commercial frame:  $COMR\n"
        val _ENCR = if (ENCR == defultValue) "" else "Encryption method registration:  $ENCR\n"
        val _EQUA = if (EQUA == defultValue) "" else "Equalization:  $EQUA\n"
        val _ETCO = if (ETCO == defultValue) "" else "Event timing codes:  $ETCO\n"
        val _GEOB = if (GEOB == defultValue) "" else "General encapsulated object:  $GEOB\n"
        val _GRID = if (GRID == defultValue) "" else "Group identification registration:  $GRID\n"
        val _IPLS = if (IPLS == defultValue) "" else "Involved people list:  $IPLS\n"
        val _LINK = if (LINK == defultValue) "" else "Linked information:  $LINK\n"
        val _MCDI = if (MCDI == defultValue) "" else "Music CD identifier:  $MCDI\n"
        val _MLLT = if (MLLT == defultValue) "" else "MPEG location lookup table:  $MLLT\n"
        val _OWNE = if (OWNE == defultValue) "" else "Ownership frame:  $OWNE\n"
        val _PRIV = if (PRIV == defultValue) "" else "Private frame:  $PRIV\n"
        val _PCNT = if (PCNT == defultValue) "" else "Play counter:  $PCNT\n"
        val _POPM = if (POPM == defultValue) "" else " Popularimeter:  $POPM\n"
        val _POSS = if (POSS == defultValue) "" else " Position synchronisation frame:  $POSS\n"
        val _RBUF = if (RBUF == defultValue) "" else " Recommended buffer size:  $RBUF\n"
        val _RVAD = if (RVAD == defultValue) "" else " Relative volume adjustment:  $RVAD\n"
        val _RVRB = if (RVRB == defultValue) "" else "Reverb:  $RVRB\n"
        val _SYLT = if (SYLT == defultValue) "" else "Synchronized lyric/text:  $SYLT\n"
        val _SYTC = if (SYTC == defultValue) "" else "Synchronized tempo codes:  $SYTC\n"
        val _TALB = if (TALB == defultValue) "" else "Album/Movie/Show title:  $TALB\n"
        val _TBPM = if (TBPM == defultValue) "" else "BPM (beats per minute):  $TBPM\n"
        val _TCOM = if (TCOM == defultValue) "" else "Composer:  $TCOM\n"
        val _TCON = if (TCON == defultValue) "" else "Content type:  $TCON\n"
        val _TCOP = if (TCOP == defultValue) "" else "Copyright message:  $TCOP\n"
        val _TDAT = if (TDAT == defultValue) "" else "Date:  $TDAT\n"
        val _TDLY = if (TDLY == defultValue) "" else "Playlist delay:  $TDLY\n"
        val _TENC = if (TENC == defultValue) "" else "Encoded by:  $TENC\n"
        val _TEXT = if (TEXT == defultValue) "" else "Lyricist/Text writer:  $TEXT\n"
        val _TFLT = if (TFLT == defultValue) "" else "File type:  $TFLT\n"
        val _TIME = if (TIME == defultValue) "" else "Time:  $TIME\n"
        val _TIT1 = if (TIT1 == defultValue) "" else "Content group description:  $TIT1\n"
        val _TIT2 = if (TIT2 == defultValue) "" else "Title/songname/content description:  $TIT2\n"
        val _TIT3 = if (TIT3 == defultValue) "" else "Subtitle/Description refinement:  $TIT3\n"
        val _TKEY = if (TKEY == defultValue) "" else "Initial key:  $TKEY\n"
        val _TLAN = if (TLAN == defultValue) "" else "Language(s):  $TLAN\n"
        val _TLEN = if (TLEN == defultValue) "" else "Length:  $TLEN\n"
        val _TMED = if (TMED == defultValue) "" else "Media type:  $TMED\n"
        val _TOAL = if (TOAL == defultValue) "" else "Original album/movie/show title:  $TOAL\n"
        val _TOFN = if (TOFN == defultValue) "" else "Original filename:  $TOFN\n"
        val _TOLY = if (TOLY == defultValue) "" else "Original lyricist(s)/text writer(s):  $TOLY\n"
        val _TOPE = if (TOPE == defultValue) "" else "Original artist(s)/performer(s):  $TOPE\n"
        val _TORY = if (TORY == defultValue) "" else "Original release year:  $TORY\n"
        val _TOWN = if (TOWN == defultValue) "" else "File owner/licensee:  $TOWN\n"
        val _TPE1 = if (TPE1 == defultValue) "" else "Lead performer(s)/Soloist(s):  $TPE1\n"
        val _TPE2 = if (TPE2 == defultValue) "" else "Band/orchestra/accompaniment:  $TPE2\n"
        val _TPE3 = if (TPE3 == defultValue) "" else "Conductor/performer refinement:  $TPE3\n"
        val _TPE4 = if (TPE4 == defultValue) "" else "Interpreted, remixed, or otherwise modified by:  $TPE4\n"
        val _TPOS = if (TPOS == defultValue) "" else "Part of a set:  $TPOS\n"
        val _TPUB = if (TPUB == defultValue) "" else "Publisher:  $TPUB\n"
        val _TRCK = if (TRCK == defultValue) "" else "Track number/Position in set:  $TRCK\n"
        val _TRDA = if (TRDA == defultValue) "" else "Recording dates:  $TRDA\n"
        val _TRSN = if (TRSN == defultValue) "" else "Internet radio station name:  $TRSN\n"
        val _TRSO = if (TRSO == defultValue) "" else "Internet radio station owner:  $TRSO\n"
        val _TSIZ = if (TSIZ == defultValue) "" else "Size:  $TSIZ\n"
        val _TSRC = if (TSRC == defultValue) "" else "ISRC (international standard recording code):  $TSRC\n"
        val _TSSE = if (TSSE == defultValue) "" else "Software/Hardware and settings used for encoding:  $TSSE\n"
        val _TYER = if (TYER == defultValue) "" else "Year:  $TYER\n"
        val _TXXX = if (TXXX == defultValue) "" else "User defined text information frame:  $TXXX\n"
        val _UFID = if (UFID == defultValue) "" else "Unique file identifier:  $UFID\n"
        val _USER = if (USER == defultValue) "" else "Terms of use:  $USER\n"
        val _USLT = if (USLT == defultValue) "" else "Unsychronized lyric/text transcription: $USLT\n"
        val _WCOM = if (WCOM == defultValue) "" else "Commercial information: $WCOM\n"
        val _WCOP = if (WCOP == defultValue) "" else "Copyright/Legal information: $WCOP\n"
        val _WOAF = if (WOAF == defultValue) "" else "Official audio file webpage:  $WOAF\n"
        val _WOAR = if (WOAR == defultValue) "" else "Official artist/performer webpage:  $WOAR\n"
        val _WOAS = if (WOAS == defultValue) "" else "Official audio source webpage:  $WOAS\n"
        val _WORS = if (WORS == defultValue) "" else "Official internet radio station homepage: $WORS\n"
        val _WPAY = if (WPAY == defultValue) "" else "Payment:  $WPAY\n"
        val _WPUB = if (WPUB == defultValue) "" else "Publishers official webpage:  $WPUB\n"
        val _WXXX = if (WXXX == defultValue) "" else "User defined URL link frame:  $WXXX\n"
        println("""$_AENC$_APIC$_COMM$_COMR$_ENCR$_EQUA$_ETCO$_GEOB$_GRID$_IPLS$_LINK$_MCDI$_MLLT$_OWNE$_PRIV$_PCNT$_POPM$_POSS$_RBUF$_RVAD$_RVRB$_SYLT$_SYTC$_TALB$_TBPM$_TCOM$_TCON$_TCOP$_TDAT$_TDLY$_TENC$_TEXT$_TFLT$_TIME$_TIT1$_TIT2$_TIT3$_TKEY$_TLAN$_TLEN$_TMED$_TOAL$_TOFN$_TOLY$_TOPE$_TORY$_TOWN$_TPE1$_TPE2$_TPE3$_TPE4$_TPOS$_TPUB$_TRCK$_TRDA$_TRSN$_TRSO$_TSIZ$_TSRC$_TSSE$_TYER$_TXXX$_UFID$_USER$_USLT$_WCOM$_WCOP$_WOAF$_WOAR$_WOAS$_WORS$_WPAY$_WPUB$_WXXX""")
    }


    /**
     *  reference to http://id3.org/id3v2.3.0
     */
    val defultValue = "Unknown"


    var AENC = defultValue  // [[#sec4.20|Audio encryption]]
        private set
    var APIC = defultValue  // [#sec4.15 Attached picture]
        private set
    var COMM = defultValue  // [#sec4.11 Comments]
        private set
    var COMR = defultValue  // [#sec4.25 Commercial frame]
        private set
    var ENCR = defultValue  // [#sec4.26 Encryption method registration]
        private set
    var EQUA = defultValue  // [#sec4.13 Equalization]
        private set
    var ETCO = defultValue  // [#sec4.6 Event timing codes]
        private set
    var GEOB = defultValue  // [#sec4.16 General encapsulated object]
        private set
    var GRID = defultValue  // [#sec4.27 Group identification registration]
        private set
    var IPLS = defultValue  // [#sec4.4 Involved people list]
        private set
    var LINK = defultValue  // [#sec4.21 Linked information]
        private set
    var MCDI = defultValue  // [#sec4.5 Music CD identifier]
        private set
    var MLLT = defultValue  // [#sec4.7 MPEG location lookup table]
        private set
    var OWNE = defultValue  // [#sec4.24 Ownership frame]
        private set
    var PRIV = defultValue  // [#sec4.28 Private frame]
        private set
    var PCNT = defultValue  // [#sec4.17 Play counter]
        private set
    var POPM = defultValue  // [#sec4.18 Popularimeter]
        private set
    var POSS = defultValue  // [#sec4.22 Position synchronisation frame]
        private set
    var RBUF = defultValue  // [#sec4.19 Recommended buffer size]
        private set
    var RVAD = defultValue  // [#sec4.12 Relative volume adjustment]
        private set
    var RVRB = defultValue  // [#sec4.14 Reverb]
        private set
    var SYLT = defultValue  // [#sec4.10 Synchronized lyric/text]
        private set
    var SYTC = defultValue  // [#sec4.8 Synchronized tempo codes]
        private set
    var TALB = defultValue  // [#TALB Album/Movie/Show title]
        private set
    var TBPM = defultValue  // [#TBPM BPM (beats per minute)]
        private set
    var TCOM = defultValue  // [#TCOM Composer]
        private set
    var TCON = defultValue  // [#TCON Content type]
        private set
    var TCOP = defultValue  // [#TCOP Copyright message]
        private set
    var TDAT = defultValue  // [#TDAT Date]
        private set
    var TDLY = defultValue  // [#TDLY Playlist delay]
        private set
    var TENC = defultValue  // [#TENC Encoded by]
        private set
    var TEXT = defultValue  // [#TEXT Lyricist/Text writer]
        private set
    var TFLT = defultValue  // [#TFLT File type]
        private set
    var TIME = defultValue  // [#TIME Time]
        private set
    var TIT1 = defultValue  // [#TIT1 Content group description]
        private set
    var TIT2 = defultValue  // [#TIT2 Title/songname/content description]
        private set
    var TIT3 = defultValue  // [#TIT3 Subtitle/Description refinement]
        private set
    var TKEY = defultValue  // [#TKEY Initial key]
        private set
    var TLAN = defultValue  // [#TLAN Language(s)]
        private set
    var TLEN = defultValue  // [#TLEN Length]
        private set
    var TMED = defultValue  // [#TMED Media type]
        private set
    var TOAL = defultValue  // [#TOAL Original album/movie/show title]
        private set
    var TOFN = defultValue  // [#TOFN Original filename]
        private set
    var TOLY = defultValue  // [#TOLY Original lyricist(s)/text writer(s)]
        private set
    var TOPE = defultValue  // [#TOPE Original artist(s)/performer(s)]
        private set
    var TORY = defultValue  // [#TORY Original release year]
        private set
    var TOWN = defultValue  // [#TOWN File owner/licensee]
        private set
    var TPE1 = defultValue  // [#TPE1 Lead performer(s)/Soloist(s)]
        private set
    var TPE2 = defultValue  // [#TPE2 Band/orchestra/accompaniment]
        private set
    var TPE3 = defultValue  // [#TPE3 Conductor/performer refinement]
        private set
    var TPE4 = defultValue  // [#TPE4 Interpreted, remixed, or otherwise modified by]
        private set
    var TPOS = defultValue  // [#TPOS Part of a set]
        private set
    var TPUB = defultValue  // [#TPUB Publisher]
        private set
    var TRCK = defultValue  // [#TRCK Track number/Position in set]
        private set
    var TRDA = defultValue  // [#TRDA Recording dates]
        private set
    var TRSN = defultValue  // [#TRSN Internet radio station name]
        private set
    var TRSO = defultValue  // [#TRSO Internet radio station owner]
        private set
    var TSIZ = defultValue  // [#TSIZ Size]
        private set
    var TSRC = defultValue  // [#TSRC ISRC (international standard recording code)]
        private set
    var TSSE = defultValue  // [#TSEE Software/Hardware and settings used for encoding]
        private set
    var TYER = defultValue  // [#TYER Year]
        private set
    var TXXX = defultValue  // [#TXXX User defined text information frame]
        private set
    var UFID = defultValue  // [#sec4.1 Unique file identifier]
        private set
    var USER = defultValue  // [#sec4.23 Terms of use]
        private set
    var USLT = defultValue  // [#sec4.9 Unsychronized lyric/text transcription]
        private set
    var WCOM = defultValue  // [#WCOM Commercial information]
        private set
    var WCOP = defultValue  // [#WCOP Copyright/Legal information]
        private set
    var WOAF = defultValue  // [#WOAF Official audio file webpage]
        private set
    var WOAR = defultValue  // [#WOAR Official artist/performer webpage]
        private set
    var WOAS = defultValue  // [#WOAS Official audio source webpage]
        private set
    var WORS = defultValue  // [#WORS Official internet radio station homepage]
        private set
    var WPAY = defultValue  // [#WPAY Payment]
        private set
    var WPUB = defultValue  // [#WPUB Publishers official webpage]
        private set
    var WXXX = defultValue  // [#WXXX User defined URL link frame]
        private set

}