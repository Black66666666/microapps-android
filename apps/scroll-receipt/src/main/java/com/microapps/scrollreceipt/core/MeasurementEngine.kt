package com.microapps.scrollreceipt.core

class MeasurementEngine {
    private val detectors = TargetPlatform.entries.associateWith { ShortVideoDetector(it) }
    fun reset() = detectors.values.forEach { it.reset() }
    fun process(event: ProbeEvent): DetectorDecision? {
        val platform = TargetPlatform.fromPackage(event.packageName)
        if (platform == null) {
            detectors.values.forEach { it.process(event) }
            return null
        }
        detectors.filterKeys { it != platform }.values.forEach { it.process(event) }
        return detectors.getValue(platform).process(event)
    }
}
