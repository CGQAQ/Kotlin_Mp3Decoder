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

import com.cg.decoder.Header
import org.junit.Test
import com.cg.decoder.utils.BitStream

class BitStreama {
    @Test
    fun testPWD() {
        val bitStream = BitStream("test/test.mp3")
        println(bitStream.length())
        bitStream.readBits(10).array().iterator().forEach {
            print(it)
        }
        bitStream.readBits(1000000000).array().iterator().forEach {
            print(it)
        }
    }


    @Test
    fun testLambda() {
        var a = A@{a: Int, b: Int ->  return@A a+b}

        println(a(1,2))
    }


    @Test
    fun testBitwise(){
        var a: Byte = 0xFF.toByte()
        var b: Byte = 0xE0.toByte()
        val e = a.toInt().shl(8) +  b.toInt().and(0xff)


        println(e)
    }

    @Test
    fun testSyncFrame() {
        val bitStream = BitStream("test/test.mp3")
        val h = Header(bitStream)
        repeat(30){
            println("pos: ${h.syncFrame()}")
        }
    }
}