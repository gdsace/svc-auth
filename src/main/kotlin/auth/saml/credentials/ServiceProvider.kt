package auth.saml.credentials

import org.opensaml.core.config.InitializationService
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.security.credential.Credential
import org.opensaml.security.credential.BasicCredential
import org.opensaml.security.credential.UsageType
import org.opensaml.security.credential.criteria.impl.EvaluableUsageCredentialCriterion
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceImpl
import net.shibboleth.utilities.java.support.resolver.ResolverException
import java.io.File
import auth.saml.credentials.criterion.DefaultCriteriaSet
import auth.saml.credentials.resolver.EntityIdResolver
import auth.saml.credentials.resolver.EndpointResolver
import auth.saml.credentials.resolver.CredentialResolver
import auth.helper.Cryptography
import auth.helper.Properties
import auth.helper.ServiceProvider

class ServiceProviderMetaDataException(message: String, exception: ResolverException) : Exception(message, exception)

open class ServiceProviderMetaData(val serviceProvider: ServiceProvider) {
    val entityId: String
    val assertionConsumerService: String
    val credential: Credential

    init {
        try {
            InitializationService.initialize()
            val spMetadataResolver = FilesystemMetadataResolver(File(serviceProvider.metadataPath))
            spMetadataResolver.setRequireValidMetadata(true)
            spMetadataResolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool()!!)
            spMetadataResolver.setId(serviceProvider.metadataId)
            spMetadataResolver.initialize()

            entityId = EntityIdResolver(spMetadataResolver).resolveSingle()

            val criteriaSet = DefaultCriteriaSet(entityId).buildServiceProviderCriteria()
            assertionConsumerService =
                EndpointResolver(spMetadataResolver).resolveSingle<AssertionConsumerServiceImpl>(criteriaSet)

            criteriaSet.add(EvaluableUsageCredentialCriterion(UsageType.ENCRYPTION))
            credential = CredentialResolver(spMetadataResolver).resolveSingle(criteriaSet)

            val key = Cryptography.generatePrivate(serviceProvider.privateKey)
            (credential as BasicCredential).setPrivateKey(key)
        } catch (e: ResolverException) {
            throw ServiceProviderMetaDataException("Something went wrong reading sp metadata/credential", e)
        }
    }
}

class ServiceProvider {
    object Singpass : ServiceProviderMetaData(Properties.getPropertiesContext().singpass!!.serviceProvider!!)
    object Corppass : ServiceProviderMetaData(Properties.getPropertiesContext().corppass!!.serviceProvider!!)
}
