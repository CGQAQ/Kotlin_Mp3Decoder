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

package com.cg.decoder

import com.cg.decoder.tags.TagV1
import com.cg.decoder.tags.TagV2
import com.cg.decoder.utils.BitStream

//decoder

class Decoder(val bitStream: BitStream){
    val header: Header

    val tagV1: TagV1
    val tagV2: TagV2

    init {
        if (bitStream.length() != 0.toLong()){
            header = Header(bitStream)

            tagV1 = TagV1(bitStream)
            tagV1.parseTag()

            tagV2 = TagV2(bitStream)
            tagV2.parseTag()

            bitStream.skip(tagV2.size)
        } else{
            throw Exception("Bitstream is invalid!")
        }
    }

    fun decodeFrame(){
        header.syncFrame()
        header.parseFrameHeader()
    }
}