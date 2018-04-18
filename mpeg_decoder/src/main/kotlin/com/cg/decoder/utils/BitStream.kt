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

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


/**
 * bit stream for file I/O operation
 */
class BitStream {
    private lateinit var _fileChannel: FileChannel

    constructor(file: File) {
        if (file.exists()) {
            this._fileChannel = file.inputStream().channel
        }
    }

    constructor(path: String) {
        val file = File(path)
        if (file.exists()) {
            this._fileChannel = file.inputStream().channel
        }
    }
    //constructor()


    /**
     * @return file size also are bit stream length
     */
    fun length() = _fileChannel.size()

    fun seek(pos: Long) {
        if(pos>=0){
            _fileChannel.position(pos)
        }
        else{
            _fileChannel.position(_fileChannel.size() + pos)
        }
    }

    fun position() = _fileChannel.position()

    fun readBytes(num: Int): ByteBuffer {
        val count: Int = if (num > 1024 * 4) 1024 * 4 else num
        val buffer = ByteBuffer.allocate(count)
        _bitPos = 0
        _fileChannel.read(buffer)
        return buffer
    }

    private var _bitPos = 0
    private var _pos = 0

    private fun readBit(buffer: ByteBuffer): Byte {
        val byte = buffer[_pos]
        val ret = (byte.toInt() shr (7 - _bitPos) and 0x1).toByte()

        _bitPos += 1
        if (_bitPos >= 8) {
            _bitPos = 0
            _pos += 1
        }

        return ret
    }

    fun readBits(num: Int): ByteBuffer {
        val count: Int = if (num > 1024 * 8 *4) 1024 * 8 *4 else num
        _pos = 0
        val remainBits = 8 - _bitPos
        val extraBits = (count - remainBits) % 8
        val bytes = (count - remainBits) / 8 + (if (extraBits != 0) 1 else 0) + (if (remainBits != 0) 1 else 0)
        if (remainBits != 0) {
            if (this.position() != 0.toLong())
                _fileChannel.position(this.position() - 1)
        }
        val buffer = ByteBuffer.allocate(bytes)
        _fileChannel.read(buffer)


        val ret = ByteArray(count)
        for (i in 0 until ret.size) {
            ret[i] = this.readBit(buffer)
        }
        return ByteBuffer.wrap(ret)
    }

    private var _positionBackup: Long = 0
    private var _bitPosBackup = 0

    fun backup(){
        _positionBackup = this.position()
        _bitPosBackup = _bitPos
    }

    fun restore(){
        _fileChannel.position(_positionBackup)
        _bitPos = _bitPosBackup
    }

    fun skip(num: Int){
        val pos = this.position() + num
        this.seek(pos)
    }


}