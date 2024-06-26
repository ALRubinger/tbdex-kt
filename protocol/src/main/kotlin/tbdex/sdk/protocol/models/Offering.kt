package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Offering.Companion.create
import tbdex.sdk.protocol.models.Order.Companion.create
import tbdex.sdk.protocol.serialization.Json
import java.time.OffsetDateTime

/**
 * A class representing the Offering resource.
 * `Offering` implements the [Resource] class and contains Offering specific data
 * - Create resource ([create])
 *
 *  @property metadata An object containing fields about the resource
 *  @property data The actual resource content. This will always be a JSON object.
 *                 The Resource Kinds section specifies the content for each individual resource type
 *  @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 * ### Example Usage:
 * ```kotlin
 * val offering = Offering.create(from, data)
 * ```
 *
 */
class Offering private constructor(
  override val metadata: ResourceMetadata,
  override val data: OfferingData,
  override var signature: String? = null
) : Resource() {
  companion object {

    /**
     * Creates a new `Offering` resource, autopopulating the id, creation/updated time, and resource kind.
     *
     * @param from DID of the PFI.
     * @param data Specific parameters relevant to an Offering.
     * @param protocol version of the tbdex protocol.
     * @return Offering instance.
     */
    fun create(from: String, data: OfferingData, protocol: String = "1.0"): Offering {
      val now = OffsetDateTime.now()
      val metadata = ResourceMetadata(
        kind = ResourceKind.offering,
        from = from,
        id = TypeId.generate(ResourceKind.offering.name).toString(),
        protocol = protocol,
        createdAt = now,
        updatedAt = now
      )
      Validator.validateData(data, "offering")

      return Offering(metadata, data)
    }

    /**
     * Takes an existing Offering in the form of a json string and parses it into an Offering object.
     * Validates object structure and performs an integrity check using the resource signature.
     *
     * @param payload The Offering as a json string.
     * @return The json string parsed into an Offering
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     * @throws IllegalArgumentException if the payload is not an Offering
     */
    fun parse(payload: String): Offering {
      val jsonResource = Parser.parseResourceToJsonNode(payload)

      val kind = jsonResource.get("metadata").get("kind").asText()
      if (kind != "offering") {
        throw IllegalArgumentException("Message must be an Offering but resource kind was $kind")
      }

      val resource = Json.jsonMapper.convertValue(jsonResource, Offering::class.java)
      resource.verify()

      return resource
    }
  }
}