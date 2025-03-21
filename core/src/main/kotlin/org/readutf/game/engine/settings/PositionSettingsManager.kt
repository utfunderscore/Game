package org.readutf.game.engine.settings

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.settings.location.PositionMarker
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class PositionSettingsManager {
    private val logger = KotlinLogging.logger {}
    private val positionTypes = mutableMapOf<String, List<Regex>>()

    /**
     * Registering the requirements of a position data class
     * Doing so allows builders to check that the build they have created
     * has the correct markers placed.
     */
    @Throws(Exception::class)
    fun <T : PositionData> registerRequirements(
        gameType: String,
        positionRequirements: KClass<T>,
    ): Result<List<Regex>, Throwable> {
        logger.info { "Registering requirements for ${positionRequirements.simpleName}" }

        val primary = positionRequirements.primaryConstructor ?: let {
            logger.error { "Primary constructor not found" }
            return Err(Exception("Primary constructor not found"))
        }

        val requirements = mutableListOf<Regex>()

        for (parameter in primary.parameters) {
            val classifier = parameter.type.classifier
            if (classifier !is KClass<*>) {
                logger.error { "${parameter.name} is not a valid type" }
                return Err(Exception("${parameter.name} is not a valid type"))
            }

            if (classifier.isSubclassOf(PositionData::class)) {
                requirements.addAll(registerRequirements("reserved", classifier as KClass<out PositionData>).getOrElse { return Err(it) })
                continue
            }

            val annotation =
                parameter.findAnnotation<PositionMarker>()
                    ?: return Err(
                        Exception("${parameter.name} in ${positionRequirements::class.simpleName} is missing the @Position annotation"),
                    )

            when {
                annotation.name != "" -> requirements.add(Regex("^${annotation.name}$"))
                annotation.startsWith != "" -> requirements.add(Regex("${annotation.startsWith}.*"))
                annotation.endsWith != "" -> requirements.add(Regex(".*${annotation.endsWith}"))
                else -> {
                    logger.error { "Invalid position annotation, a filter must be set" }
                    return Err(Exception("Invalid position annotation, a filter must be set"))
                }
            }
        }

        positionTypes[gameType] = requirements
        positionTypes[positionRequirements.qualifiedName!!] = requirements

        return Ok(requirements)
    }

    fun <T : PositionData> loadPositionData(
        positions: Map<String, Marker>,
        positionSettingsType: KClass<out T>,
    ): Result<T, Throwable> {
        if (positionSettingsType.qualifiedName == null) {
            logger.error { "Invalid position settings type" }
            return Err(Exception("Invalid position settings type"))
        }

        val primaryConstructor = positionSettingsType.primaryConstructor ?: let {
            logger.error { "Primary constructor not found" }
            return Err(Exception("Primary constructor not found"))
        }

        val parameters = mutableListOf<Any>()

        for (parameter in primaryConstructor.parameters) {
            val classifier = parameter.type.classifier
            if (classifier !is KClass<*> ||
                (
                    classifier != List::class &&
                        classifier != Marker::class &&
                        !classifier.isSubclassOf(PositionData::class)
                    )
            ) {
                logger.error { "Invalid type for ${parameter.name}" }
                return Err(Exception("Invalid type for ${parameter.name}"))
            }

            if (classifier.isSubclassOf(PositionData::class)) {
                val subClass = classifier as KClass<out PositionData>
                parameters.add(loadPositionData(positions, subClass).getOrElse { return Err(it) })
            } else {
                val annotation = parameter.findAnnotation<PositionMarker>() ?: let {
                    logger.error { "Missing @Position annotation" }
                    return Err(Exception("Missing @Position annotation"))
                }
                val regex =
                    when {
                        annotation.name != "" -> Regex("^${annotation.name}$")
                        annotation.startsWith != "" -> Regex("${annotation.startsWith}.*")
                        annotation.endsWith != "" -> Regex(".*${annotation.endsWith}")
                        else -> {
                            logger.error { "Invalid position annotation, a filter must be set" }
                            return Err(Exception("Invalid position annotation, a filter must be set"))
                        }
                    }
                parameters.add(getParameterForType(regex, parameter, positions).getOrElse { return Err(it) })
            }
        }

        try {
            val positionData = primaryConstructor.call(*parameters.toTypedArray())
            return Ok(positionData)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create position data" }
            return Err(Exception("Failed to create position data: ${e.message}"))
        }
    }

    fun validatePositionRequirements(
        gameType: String,
        positions: Map<String, Marker>,
    ): Result<Unit, Throwable> {
        val types = positionTypes[gameType] ?: let {
            logger.error { "No position requirements found for $gameType" }
            return Err(Exception("No position requirements found for $gameType"))
        }

        types.forEach { regex ->
            val matchingPositions = positions.filter { it.key.matches(regex) }

            if (matchingPositions.isEmpty()) {
                logger.error { "No positions found matching $regex" }
                return Err(Exception("No positions found matching $regex"))
            }
        }
        return Ok(Unit)
    }

    private fun getParameterForType(
        regex: Regex,
        parameter: KParameter,
        positions: Map<String, Marker>,
    ): Result<Any, Throwable> {
        val isList = parameter.type.classifier == List::class

        if (isList) {
            val values = positions.filter { it.key.matches(regex) }.values

            if (values.isEmpty()) {
                logger.error { "No positions found for ${parameter.name} matching $regex" }
                return Err(Exception("No positions found for ${parameter.name} matching $regex"))
            }
            return Ok(values.toList())
        } else {
            val multipleOptions = positions.filter { it.key.matches(regex) }.values

            if (multipleOptions.isEmpty()) {
                logger.error { "No positions found for ${parameter.name} matching $regex" }
                return Err(Exception("No positions found for ${parameter.name} matching $regex"))
            }
            if (multipleOptions.size > 1) {
                logger.warn { "Multiple positions found for ${parameter.name} matching $regex" }
            }

            val value = multipleOptions.first()

            return Ok(value)
        }
    }
}
