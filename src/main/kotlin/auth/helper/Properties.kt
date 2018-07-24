package auth.helper

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

@Configuration
@ConfigurationProperties(prefix = "app")
class Properties: ApplicationContextAware {
    var token: Token? = null
    var singpass: Provider? = null
    var corppass: Provider? = null
    var instrumentation: Instrumentation? = null
    lateinit var homepageUrl: String
    
    companion object {
        lateinit private var applicationContext: ApplicationContext

        fun getPropertiesContext(): Properties {
            return applicationContext.getBeansOfType(Properties::class.java).values.iterator().next()
        }
    }

    override fun setApplicationContext(context: ApplicationContext ) {
        applicationContext = context
    }
}

class Instrumentation {
    var zipkin: ZipkinInstrumentation? = null
}

class ZipkinInstrumentation {
    lateinit var url: String
    var env: String? = null
}

class Token {
    lateinit var privateKey: String
    lateinit var signatureAlgorithm: String
    var expirationTime: Long = 1_000
}

class Provider {
    var mockUserListUrl: String? = null
    var serviceProvider: ServiceProvider? = null
    var identityProvider: IdentityProvider? = null
}

class ServiceProvider {
    lateinit var loginUrl: String
    lateinit var privateKey: String
    lateinit var metadataPath: String
    lateinit var metadataId: String
}

class IdentityProvider {
    lateinit var host: String
    lateinit var serviceId: String
    lateinit var metadataPath: String
    lateinit var metadataId: String
    var artifactServiceProxyHost: String? = null
    var artifactServiceProxyPort: Int? = null
    var artifactServiceProxyUsername: String? = null
    var artifactServiceProxyPassword: String? = null
    var artifactLifetimeClockSkew: Long = 0
}