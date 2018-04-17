import org.junit.Test
import java.nio.ByteBuffer

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

class CharsetTest {
    @Test
    fun decode() {
        var src = byteArrayOf(68, 0, 101, 0, 115, 0, 112, 0, 97, 0, 99, 0, 105, 0, 116, 0, 111, 0)
        val utf16 = charset("UTF-16LE")
        val raw = ByteBuffer.wrap(src)
        val a = utf16.decode(raw)
        raw.rewind()
        val b = utf16.decode(raw)

        println(a)
        println(b)
    }
}