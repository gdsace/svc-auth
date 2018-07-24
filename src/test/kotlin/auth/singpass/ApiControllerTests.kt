package auth.singpass

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Assert.*
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.*
import io.jsonwebtoken.JwtException
import auth.helper.Jwt

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiControllerIntegrationTests {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun refresh() {
        val result = testRestTemplate.getForEntity("/sp/refresh", String::class.java)
        assertNotNull(result)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}

@RunWith(MockitoJUnitRunner::class)
class ApiControllerUnitTests {

    @Mock
    lateinit var mockJwt: Jwt

    @InjectMocks
    lateinit var apiController: ApiController

    @Before
    fun setupMock() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun refresh() {
        val validToken = "valid.token.value"
        Mockito.`when`(mockJwt.parse(validToken)).thenReturn(mapOf("userName" to "foo", "mobile" to "bar"))

        val result = apiController.refresh("Bearer ${validToken}")
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertTrue(result.body["success"] as Boolean)
    }

    @Test
    fun refreshWithInvalidToken() {
        val invalidToken = "general.invalid.token"
        val invalidJWTToken = "jwt.invalid.token"
        Mockito.`when`(mockJwt.parse(invalidToken)).thenThrow(RuntimeException())
        Mockito.`when`(mockJwt.parse(invalidJWTToken)).thenThrow(JwtException("error"))

        var result = apiController.refresh(invalidToken)
        assertNotNull(result)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

        result = apiController.refresh(invalidJWTToken)
        assertNotNull(result)
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }
}
