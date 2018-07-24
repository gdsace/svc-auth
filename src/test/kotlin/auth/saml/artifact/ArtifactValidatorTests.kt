package auth.saml.artifact

import org.junit.Test
import org.junit.Assert.*
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.opensaml.saml.saml2.core.*
import org.springframework.mock.web.MockHttpServletRequest

class ArtifactValidatorUnitTests: ArtifactUnitTestBase() {
    @Test
    fun validateDestinationAndLifetime() {

        val filename = "/ArtifactValidatorTest/artifactResponse.xml"
        val artifactResponse = unMarshallElement(filename) as ArtifactResponse
        val response = artifactResponse.message as Response
        val request = MockHttpServletRequest()

        try {
            // Positive test
            // Correct lifetime and destination
            response.issueInstant = DateTime.now()
            response.destination = "http://destination"
            request.serverName = "destination"
            ArtifactValidator.validateDestinationAndLifetime(artifactResponse, request)
        } catch (e: Exception) {
            fail("Validation failed. Should not be throwing error")
        }

        try {
            // Positive test
            // Allow a skew time of 5s for lifetime
            response.issueInstant = DateTime.now() + 5000
            response.destination = "http://destination"
            request.serverName = "destination"
            ArtifactValidator.validateDestinationAndLifetime(artifactResponse, request, 5000)
        } catch (e: Exception) {
            fail("Validation failed. Should not be throwing error")
        }

        try {
            // Negative test
            // Incorrect lifetime
            response.issueInstant = DateTime.now() - 66000
            response.destination = "http://destination"
            request.serverName = "destination"
            ArtifactValidator.validateDestinationAndLifetime(artifactResponse, request)
            fail("Validation failed. Should be throwing error")
        } catch (e: Exception) {
            assertTrue(e is RuntimeException)
        }

       try {
           // Negative test
           // Incorrect destination
           response.issueInstant = DateTime.now()
           response.destination = "http://destination"
           request.serverName = "wrong_destination"
           ArtifactValidator.validateDestinationAndLifetime(artifactResponse, request)
           fail("Validation failed. Should be throwing error")
       } catch (e: Exception) {
           assertTrue(e is RuntimeException)
       }

       try {
           // Positive test
           // When service provider callback url is provided, it should compare against it
            response.issueInstant = DateTime.now()
            response.destination = "https://destination"
            request.serverName = "whatever_destination"
            ArtifactValidator.validateDestinationAndLifetime(artifactResponse, request, 0, "https://destination")
       } catch (e: Exception) {
            fail("Validation failed. Should not be throwing error")
       }

       try {
           // Negative test
           // When service provider callback url is provided, it should compare against it
            response.issueInstant = DateTime.now()
            response.destination = "http://wrong_destination"
            request.serverName = "whatever_destination"
            ArtifactValidator.validateDestinationAndLifetime(artifactResponse, request, 0, "https://destination")
            fail("Validation failed. Should be throwing error")
       } catch (e: Exception) {
            assertTrue(e is RuntimeException)
       }
    }

//    @Test
//    fun verifyAssertionSignature() {
//        val filename = "/ArtifactResolverTest/signedResponse-positive.xml"
//        val response = unmarshallElement(filename) as Response
//        val assertion = response.assertions[0]
//
//        artifactResolver.verifyAssertionSignature(assertion, credential)
//    }
}
