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

package com.cg.decoder.beans


data class HeaderBean(val version: Int,
                      val layer: Int,
                      val protection: Int,
                      val bitrate: Int,
                      val samplingrate: Int,
                      val padding: Int,
                      val private: Int,
                      val channelMode: Int,
                      val modeExtension: Int,
                      val copyright: Int,
                      val original: Int,
                      val emphasis: Int,
                      val frameLength: Int = when (layer) {
                      in 1..2 -> {
                          if(samplingrate != 0) (12 * bitrate / samplingrate + padding) * 4 else 0

                      }
                      3 -> {
                          if(samplingrate != 0)  144 * bitrate / samplingrate + padding else 0

                      }
                      else -> 0
                  }) {

    val MpegVersionTable: ArrayList<String> = arrayListOf(
            "MPEG Version 2.5",
            "reserved",
            "MPEG Version 2 (ISO/IEC 13818-3)",
            "MPEG Version 1 (ISO/IEC 11172-3)"
    )
    val LayerDescriptionTable: ArrayList<String> = arrayListOf(
            "reserved",
            "Layer III",
            "Layer II",
            "Layer I"
    )
    val ProtectionTable: ArrayList<String> = arrayListOf(
            "Protected by CRC (16bit crc follows header)",
            "Not protected"
    )

    /**
     * NOTES: All values are in kbps
     * V1 - MPEG Version 1
     * V2 - MPEG Version 2 and Version 2.5
     * L1 - Layer I
     * L2 - Layer II
     * L3 - Layer III
     */
    val BitrateTable: ArrayList<ArrayList<String>> = arrayListOf(
            arrayListOf("free", "32", "64", "96", "128", "160", "192", "224", "256", "288", "320", "352", "384", "416", "448", "bad"), //	V1,L1
            arrayListOf("free", "32", "48", "56", "64", "80", "96", "112", "128", "160", "192", "224", "256", "320", "384", "bad"), //V1,L2
            arrayListOf("free", "32", "40", "48", "56", "64", "80", "96", "112", "128", "160", "192", "224", "256", "320", "bad"), //V1,L3
            arrayListOf("free", "32", "48", "56", "64", "80", "96", "112", "128", "144", "160", "176", "192", "224", "256", "bad"), //V2,L1
            arrayListOf("free", "8", "16", "24", "32", "40", "48", "56", "64", "80", "96", "112", "128", "144", "160", "bad")//V2, L2 & L3
    )

    //Sampling rate frequency index (values are in Hz)
    val SamplingRateTable: ArrayList<ArrayList<String>> = arrayListOf(
            arrayListOf("44100", "48000", "32000", "reserv."), //MPEG1
            arrayListOf("22050", "24000", "16000", "reserv."), //MPEG2
            arrayListOf("11025", "12000", "8000", "reserv.")//MPEG2.5
    )

    /**
     * Padding is used to fit the bit rates exactly.
     * For an example: 128k 44.1kHz layer II uses a lot of 418 bytes and some of 417 bytes long frames to get the exact 128k bitrate.
     * For Layer I slot is 32 bits long, for Layer II and Layer III slot is 8 bits long.
     */
    val paddingBitTable: ArrayList<String> = arrayListOf(
            "frame is not padded",
            "frame is padded with one extra slot"
    )
    val channelModeTable: ArrayList<String> = arrayListOf(
            "Stereo",
            "Joint stereo (Stereo)",
            "Dual channel (Stereo)",
            "Single channel (Mono)"
    )

    /**Mode extension is used to join informations that are of no use for stereo effect, thus reducing needed resources.
     * These bits are dynamically determined by an encoder in Joint stereo mode.
     *
     * Complete frequency range of MPEG file is divided in subbands There are 32 subbands.
     * For Layer I & II these two bits determine frequency range (bands) where intensity stereo is applied.
     * For Layer III these two bits determine which type of joint stereo is used (intensity stereo or m/s stereo).
     * Frequency range is determined within decompression algorythm.
     */
    val modeExtensionTable: ArrayList<ArrayList<String>> = arrayListOf(
            arrayListOf("bands 4 to 31", "bands 8 to 31", "bands 12 to 31", "bands 16 to 31"),
            arrayListOf("Intensity stereo is off and MS stereo is off too",
                    "Intensity stereo is on and MS stereo is off",
                    "Intensity stereo is off and MS stereo is on",
                    "Intensity stereo is on and MS stereo is on too"
            )
    )

    val copyrightTable: ArrayList<String> = arrayListOf("Audio is not copyrighted", "Audio is copyrighted")
    val originalTable: ArrayList<String> = arrayListOf("Copy of original media", "Original media")
    val emphasisTable: ArrayList<String> = arrayListOf("none", "50/15 ms", "reserved", "CCIT J.17")

    fun valid(): Boolean{
        return !(version == 1 || layer == 0 || bitrate == 0 || bitrate == 0xF || samplingrate == 0x3)
    }

    override fun toString(): String {
        if (version == 1 || layer == 0 || bitrate == 0 || bitrate == 0xF || samplingrate == 0x3) {
            return "This frame is invalid"
        }

        val versionString: String = MpegVersionTable[version]
        val layerString: String = LayerDescriptionTable[layer]
        val protectionString: String = ProtectionTable[protection]
        val bitrateString: String
        val samplingrateString: String
        val paddingString: String = paddingBitTable[padding]
        val privateString: String = "unspecified meaning"
        val channelModeString: String = channelModeTable[channelMode]
        var modeExtensionString: String
        val copyrightString: String = copyrightTable[copyright]
        val originalString: String = originalTable[original]
        val emphasisString: String = emphasisTable[emphasis]

        if (version == 0 || version == 2) {
            bitrateString = when (layer) {
                1 -> BitrateTable[4][bitrate and 0xFF]
                2 -> BitrateTable[4][bitrate and 0xFF]
                3 -> BitrateTable[3][bitrate and 0xFF]
                else -> "Unknown"
            }
            samplingrateString = if (version == 0) {
                SamplingRateTable[2][samplingrate and 0xFF]
            } else {
                SamplingRateTable[1][samplingrate and 0xFF]
            }
        } else if (version == 3) {
            bitrateString = when (layer) {
                1 -> BitrateTable[2][bitrate and 0xFF]
                2 -> BitrateTable[1][bitrate and 0xFF]
                3 -> BitrateTable[0][bitrate and 0xFF]
                else -> "Unknown"
            }
            samplingrateString = SamplingRateTable[0][samplingrate and 0xFF]
        } else {
            bitrateString = "Unknown"
            samplingrateString = "Unknown"
        }

        modeExtensionString = when (layer) {
            in 1..2 -> {
                modeExtensionTable[0][modeExtension]
            }
            3 -> {
                modeExtensionTable[1][modeExtension and 0xFF]
            }
            else -> {
                "Unknown"
            }
        }


        return """--------------------------------------------------
MPEG Audio version ID: $versionString
Layer description: $layerString
Protection bit: $protectionString
Bitrate: $bitrateString
Sampling rate: $samplingrateString
Padding bit: $paddingString
Private bit: $privateString
Channel Mode: $channelModeString
Mode extension: $modeExtensionString
Copyright: $copyrightString
Original: $originalString
Emphasis: $emphasisString
--------------------------------------------------"""
    }
}