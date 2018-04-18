import com.cg.decoder.Decoder
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

class DecoderTest {
    @Test
    fun testDecodeFrame(){
        val bitStream = BitStream("test/test.mp3")
        val decoder = Decoder(bitStream)
        decoder.tagV1.printTag()
        decoder.tagV2.printTag()

        var str: String

        while (true){
            decoder.decodeFrame()
            if (decoder.header.currentFrameHeader.valid()) break
        }
        println(decoder.header.currentFrameHeader.toString())
    }
}