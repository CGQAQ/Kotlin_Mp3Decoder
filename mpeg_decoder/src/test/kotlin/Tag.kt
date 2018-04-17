import com.cg.decoder.tags.TagV1
import com.cg.decoder.tags.TagV2
import com.cg.decoder.utils.BitStream
import org.junit.Test

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

class Tag {

    @Test
    fun v1(){
        val bitStream = BitStream("test/test.mp3")
        var t = TagV1(bitStream)
        t.parseTag()
        t.printTag()
    }

    @Test
    fun v2(){
        val t = TagV2(BitStream("test/test.mp3"))
        t.parseTag()
        println(t.version)
        println(t.reversion)
        println(t.unsynchronisation)
        println(t.extendedHeader)
        println(t.experimentalIndicator)
        println(t.size)
        t.printTag()
    }
}