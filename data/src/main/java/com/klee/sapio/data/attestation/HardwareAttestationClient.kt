package com.klee.sapio.data.attestation

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.VisibleForTesting
import com.klee.sapio.domain.model.VerifiedBootState
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.ProviderException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareAttestationClient @Inject constructor() {

    private val secureRandom = SecureRandom()

    fun readVerifiedBootState(): VerifiedBootState {
        val alias = "sapio_attestation_${UUID.randomUUID()}"
        var keyStore: KeyStore? = null
        return try {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore = ks
            val attestationCert = generateKeyAndReturnLeaf(alias, ks)
            parseVerifiedBootState(attestationCert) ?: VerifiedBootState.UNKNOWN
        } catch (ignored: GeneralSecurityException) {
            VerifiedBootState.UNKNOWN
        } catch (ignored: IOException) {
            VerifiedBootState.UNKNOWN
        } catch (ignored: IllegalArgumentException) {
            VerifiedBootState.UNKNOWN
        } catch (ignored: ProviderException) {
            // Devices without provisioned attestation keys fail to generate the key pair.
            VerifiedBootState.UNKNOWN
        } finally {
            runCatching { keyStore?.deleteEntry(alias) }
        }
    }

    private fun generateKeyAndReturnLeaf(alias: String, keyStore: KeyStore): X509Certificate {
        val challenge = ByteArray(CHALLENGE_SIZE).also(secureRandom::nextBytes)
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setKeySize(KEY_SIZE)
            .setAttestationChallenge(challenge)
            .setUserAuthenticationRequired(false)
            .build()
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
        val chain = keyStore.getCertificateChain(alias)
        val certificate = chain?.firstOrNull() as? X509Certificate
        return certificate ?: throw GeneralSecurityException("Missing attestation certificate")
    }

    @VisibleForTesting
    internal fun parseVerifiedBootState(certificate: X509Certificate): VerifiedBootState? {
        val keyDescription = readKeyDescription(certificate) ?: return null
        val rootOfTrust = findRootOfTrust(keyDescription) ?: return null

        return readVerifiedBootState(ASN1Sequence.getInstance(rootOfTrust, true))
    }

    private fun readKeyDescription(certificate: X509Certificate): ASN1Sequence? {
        val extensionValue = certificate.getExtensionValue(ATTESTATION_OID) ?: return null
        val record = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(extractOctets(extensionValue)))

        return record.takeIf { it.size() >= MIN_KEY_DESCRIPTION_FIELDS }
    }

    private fun findRootOfTrust(keyDescription: ASN1Sequence): ASN1TaggedObject? {
        val teeEnforced = keyDescription.getSafeSequence(keyDescription.size() - 1) ?: return null
        val softwareEnforced = keyDescription.getSafeSequence(keyDescription.size() - 2)

        return findTaggedObject(teeEnforced, ROOT_OF_TRUST_TAG)
            ?: findTaggedObject(softwareEnforced, ROOT_OF_TRUST_TAG)
    }

    private fun readVerifiedBootState(rootOfTrust: ASN1Sequence): VerifiedBootState? {
        if (rootOfTrust.size() <= ROOT_OF_TRUST_VERIFIED_BOOT_STATE_INDEX) {
            return null
        }

        val enumerated = ASN1Enumerated.getInstance(
            rootOfTrust.getObjectAt(ROOT_OF_TRUST_VERIFIED_BOOT_STATE_INDEX)
        )

        return VerifiedBootState.fromAttestationValue(enumerated.value.toInt())
    }

    private fun extractOctets(extensionValue: ByteArray): ByteArray {
        ASN1InputStream(extensionValue).use { inputStream ->
            val octetString = inputStream.readObject() as? ASN1OctetString
                ?: throw IOException("Unexpected attestation extension format")
            return octetString.octets
        }
    }

    private fun findTaggedObject(sequence: ASN1Sequence?, tag: Int): ASN1TaggedObject? {
        val objects = sequence?.objects ?: return null
        while (objects.hasMoreElements()) {
            val next = objects.nextElement()
            if (next is ASN1TaggedObject && next.tagNo == tag) {
                return next
            }
        }
        return null
    }

    private fun ASN1Sequence.getSafeSequence(index: Int): ASN1Sequence? {
        if (index < 0 || index >= size()) {
            return null
        }

        return runCatching { ASN1Sequence.getInstance(getObjectAt(index)) }.getOrNull()
    }

    private companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ATTESTATION_OID = "1.3.6.1.4.1.11129.2.1.17"
        private const val CHALLENGE_SIZE = 32
        private const val KEY_SIZE = 256
        private const val ROOT_OF_TRUST_TAG = 704
        private const val ROOT_OF_TRUST_VERIFIED_BOOT_STATE_INDEX = 2
        private const val MIN_KEY_DESCRIPTION_FIELDS = 2
    }
}
