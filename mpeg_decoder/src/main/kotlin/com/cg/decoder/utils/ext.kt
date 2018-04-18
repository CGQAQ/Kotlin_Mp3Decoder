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

package com.cg.decoder.utils

import java.nio.ByteBuffer

fun ByteBuffer.getItemByIndex(index: Int): Byte{
    if(index>=0){
        return this.get(index)
    }
    else{
        return  this.get(this.capacity()+index)
    }
}

fun BitStream.sandbox(op: (BitStream) -> Unit){
    this.backup()
    op(this)
    this.restore()
}

enum class MASK(val mask: Int) {
    B1(0x1),
    B2(0x3),
    B3(0x7),
    B4(0xF),
    B5(0x1F),
    B6(0x3F),
    B7(0x7F),
    B9(0x1FF),
    B10(0x3FF),
    B11(0x7FF),
    B12(0xFFF),
    B13(0x1FFF),
    B14(0x3FFF),
    B15(0x7FFF),
    B16(0xFFFF),
    B17(0x1FFFF),
    B18(0x3FFFF),
    B19(0x7FFFF),
    B20(0xFFFFF),
    B21(0x1FFFFF),
    B22(0x3FFFFF),
    B23(0x7FFFFF),
    B24(0xFFFFFF),
    B25(0x1FFFFFF),
    B26(0x3FFFFFF),
    B27(0x7FFFFFF),
    B28(0xFFFFFFF),
    B29(0x1FFFFFFF),
    B30(0x3FFFFFFF),
    B31(0x7FFFFFFF),
    B32(0xFFFFFFFF.toInt()),
}


fun ByteBuffer.toInt(): Int{
    if (this.capacity()<32){
        var n: Int = 0
        for (i in 0 until this.capacity()){
            n += this[i].toInt().and(MASK.B1.mask).shl(this.capacity() - (i + 1))
        }
        return n
    }
    throw Exception("can't convert bytebuffer into Int which capacity greater or equal than 32")
}