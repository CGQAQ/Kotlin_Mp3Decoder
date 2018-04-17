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

import com.cg.decoder.utils.BitStream

class Header(val bitStream: BitStream) {

    private var _syncMask = 0xFFE0

    fun syncFrame(): Long{
        var d1 = bitStream.readBytes(1).get(0)
        var d2 = bitStream.readBytes(1).get(0)
        var d: Int = (d1.toInt() shl 8) + d2

        val position = bitStream.position()
        val range = 1024*1024  //512 kb


        while ((d and _syncMask) != _syncMask || d1 == 0.toByte()){
            if (bitStream.position() - position > range) return -1
            d1 = d2
            d2 = bitStream.readBytes(1).get(0)
            d = (d1.toInt() shl 8) + d2.toInt().and(0xFF)
            //println("${bitStream.position()}  d1: $d1 d2: $d2 ")
        }

        //var result = (d and _syncMask)
        return bitStream.position()
    }
}