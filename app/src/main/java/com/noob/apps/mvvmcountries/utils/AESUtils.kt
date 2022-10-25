package com.noob.apps.mvvmcountries.utils

import android.util.Base64
import com.noob.apps.mvvmcountries.BuildConfig
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object AESUtils {
    private val key = BuildConfig.ENCRYPTION_KEY

    fun decryptResponse(code: String?): String? {
        try {
            val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
            val skey = SecretKeySpec(keyBytes, "AES")
            val input = Base64.decode(code, Base64.DEFAULT)

            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val ivSpec = IvParameterSpec("0000000000000000".toByteArray())
                cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec)
//                val plainText = ByteArray(cipher.getOutputSize(input.size))
//                var ptLength = cipher.update(input, 0, input.size, plainText, 0)
                val x = cipher.doFinal(input)
                val decryptedString = String(x, StandardCharsets.UTF_8)
                return decryptedString.trim { it <= ' ' }
            }
        } catch (uee: UnsupportedEncodingException) {
            uee.printStackTrace()
        } catch (ibse: IllegalBlockSizeException) {
            ibse.printStackTrace()
        } catch (bpe: BadPaddingException) {
            bpe.printStackTrace()
        } catch (ike: InvalidKeyException) {
            ike.printStackTrace()
        } catch (nspe: NoSuchPaddingException) {
            nspe.printStackTrace()
        } catch (nsae: NoSuchAlgorithmException) {
            nsae.printStackTrace()
        } catch (e: ShortBufferException) {
            e.printStackTrace()
        }
        return null
    }

//    fun doDecrypt(encodekey: String, encrptedStr: String): String? {
//        try {
//            val dcipher = Cipher.getInstance("AES")
//            val secretKeySpec: SecretKeySpec = getKey(encodekey)
//            dcipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
//            // decode with base64 to get bytes
//            val dec = Base64Utils.decode(encrptedStr)
//            val utf8 = dcipher.doFinal(dec)
//
//            // create new string based on the specified charset
//            return String(utf8, StandardCharsets.UTF_8)
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//        return null
//    }

//    private fun getKey(encodekey: String): SecretKeySpec {
//        var key = encodekey.toByteArray(StandardCharsets.UTF_8)
//        val sha: MessageDigest = MessageDigest.getInstance("SHA-256")
//        key = sha.digest(key)
//        println(byteArrayToHexString(key))
//        key = key.copyOf(32) // use only first 256 bit
//        println(byteArrayToHexString(key))
//        return SecretKeySpec(key, "AES")
//    }

//    private fun byteArrayToHexString(b: ByteArray): String {
//        var result = ""
//        for (i in b.indices) {
//            result += ((b[i] and 0xff.toByte()) + 0x100).toString(16).substring(1)
//        }
//        return result
//    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun decrypt(
//        strToDecrypt: String?,
//        mobileNumber: String?,
//        mail: String?,
//        userId: String?, date: String?
//    ): String? {
//        val  SIGMA="SIGMA"
//        val  dollarSign="$"
//        val  dash="_"
//        val  percentage="%"
//        val  hash="#"
//        val test="{P@ssw0rd@NoHopeYet$01554587098%Medhat@gmail.com#9e72963d-fb81-474f-9247-43a1940368fb_20211219%SIGMA}"
//        val SECRET_KEY = "EfpkwiMo9H/qJBD4LnA6SPj26ZOwQ4Zz"
//        val SECRET_KEY =
//            "{P@ssw0rd@NoHopeYet$dollarSign$mobileNumber$percentage$mail$hash$userId$dash$date$percentage$SIGMA}"
//        return strToDecrypt?.let { doDecrypt(SECRET_KEY, it) }
//        try {
//            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
//            val ivspec = IvParameterSpec(iv)
//            val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//            val spec: KeySpec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT.toByteArray(), 65536, 256)
//            val tmp: SecretKey = factory.generateSecret(spec)
//            val secretKey = SecretKeySpec(tmp.encoded, "AES")
//            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec)
//            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
//        } catch (e: Exception) {
//            println("Error while decrypting: $e")
//        }
//        return null
//    }

}