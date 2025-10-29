// src/main/java/com/developersbeeh/medcontrol/util/DebugUtils.kt
package com.developersbeeh.medcontrol.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

object DebugUtils {
    /**
     * Obtém a impressão digital SHA-1 do certificado do APK em execução.
     */
    @SuppressLint("PackageManagerGetSignatures")
    fun getCertificateSha1Fingerprint(context: Context): String? {
        try {
            val packageName = context.packageName
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            // ✅ CORREÇÃO: Adicionados operadores de chamada segura (?.) para lidar com valores nulos.
            val signatures: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            // ✅ CORREÇÃO: Adicionada verificação para garantir que a lista de assinaturas não é nula nem vazia.
            if (signatures?.isNotEmpty() == true) {
                val signature = signatures[0]
                val messageDigest = MessageDigest.getInstance("SHA-1")
                messageDigest.update(signature.toByteArray())
                val sha1 = messageDigest.digest()
                return sha1.joinToString(":") { "%02X".format(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Falha ao obter SHA-1"
    }
}