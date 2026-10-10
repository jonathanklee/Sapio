package com.klee.sapio.data

import com.klee.sapio.data.attestation.HardwareAttestationClient
import com.klee.sapio.domain.model.VerifiedBootState
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.security.cert.X509Certificate

class HardwareAttestationClientTest {

    private val client = HardwareAttestationClient()

    @Test
    fun `a green root of trust is read from the tee enforced list`() {
        assertEquals(VerifiedBootState.GREEN, parse(rootOfTrust(bootState = 0)))
    }

    @Test
    fun `a yellow root of trust is read from the tee enforced list`() {
        assertEquals(VerifiedBootState.YELLOW, parse(rootOfTrust(bootState = 1)))
    }

    @Test
    fun `an orange root of trust is read from the tee enforced list`() {
        assertEquals(VerifiedBootState.ORANGE, parse(rootOfTrust(bootState = 2)))
    }

    @Test
    fun `a red root of trust is read from the tee enforced list`() {
        assertEquals(VerifiedBootState.RED, parse(rootOfTrust(bootState = 3)))
    }

    @Test
    fun `an out of range boot state is unknown`() {
        assertEquals(VerifiedBootState.UNKNOWN, parse(rootOfTrust(bootState = 7)))
    }

    @Test
    fun `the software enforced list is used when the tee one has no root of trust`() {
        val softwareEnforced = DERSequence(DERTaggedObject(true, ROOT_OF_TRUST_TAG, rootOfTrustSequence(1)))
        val teeEnforced = DERSequence(ASN1Integer(1))

        assertEquals(VerifiedBootState.YELLOW, parse(keyDescription(softwareEnforced, teeEnforced)))
    }

    @Test
    fun `a certificate without the attestation extension is not parsed`() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.getExtensionValue(ATTESTATION_OID)).thenReturn(null)

        assertNull(client.parseVerifiedBootState(certificate))
    }

    @Test
    fun `a key description with too few fields is not parsed`() {
        val tooShort = DERSequence(ASN1Integer(1))

        assertNull(parse(tooShort))
    }

    @Test
    fun `a key description without a root of trust is not parsed`() {
        val withoutRootOfTrust = DERSequence(
            arrayOf<ASN1Encodable>(DERSequence(ASN1Integer(1)), DERSequence(ASN1Integer(2)))
        )

        assertNull(parse(withoutRootOfTrust))
    }

    @Test
    fun `a truncated root of trust is not parsed`() {
        val truncated = DERSequence(ASN1Integer(1))
        val teeEnforced = DERSequence(DERTaggedObject(true, ROOT_OF_TRUST_TAG, truncated))

        assertNull(parse(keyDescription(DERSequence(ASN1Integer(0)), teeEnforced)))
    }

    private fun parse(keyDescription: DERSequence): VerifiedBootState? {
        val extension = DEROctetString(keyDescription.encoded).encoded
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.getExtensionValue(ATTESTATION_OID)).thenReturn(extension)

        return client.parseVerifiedBootState(certificate)
    }

    private fun rootOfTrust(bootState: Int): DERSequence {
        val teeEnforced = DERSequence(DERTaggedObject(true, ROOT_OF_TRUST_TAG, rootOfTrustSequence(bootState)))

        return keyDescription(DERSequence(ASN1Integer(0)), teeEnforced)
    }

    private fun rootOfTrustSequence(bootState: Int) = DERSequence(
        arrayOf<ASN1Encodable>(
            DEROctetString(ByteArray(1)),
            ASN1Integer(0),
            ASN1Enumerated(bootState)
        )
    )

    private fun keyDescription(softwareEnforced: DERSequence, teeEnforced: DERSequence) =
        DERSequence(arrayOf<ASN1Encodable>(softwareEnforced, teeEnforced))

    private companion object {
        const val ATTESTATION_OID = "1.3.6.1.4.1.11129.2.1.17"
        const val ROOT_OF_TRUST_TAG = 704
    }
}
