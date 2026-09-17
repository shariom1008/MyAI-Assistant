package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixElectronicsKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Electronics", "Operational Amplifier", listOf("op amp operational amplifier", "operational amplifier"), """An operational amplifier is a high-gain differential amplifier commonly used with feedback for functions such as amplification, filtering and comparison.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "ADC and DAC", listOf("adc dac analog digital converter", "adc and dac", "adc"), """An ADC converts analog signals to digital representations, while a DAC converts digital values into analog output signals.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "Signal Filtering", listOf("electronic filter low pass high pass", "signal filtering"), """Filters selectively attenuate frequency components; low-pass, high-pass, band-pass and band-stop filters serve different signal-processing needs.""", ConfidenceLevel.HIGH, false)
    )
}
