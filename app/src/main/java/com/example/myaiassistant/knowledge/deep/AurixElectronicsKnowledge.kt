package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixElectronicsKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Electronics", "Diode", listOf("diode semiconductor"), """A diode is a semiconductor device that commonly conducts preferentially in one direction depending on bias.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "Transistor", listOf("transistor bjt mosfet"), """Transistors are semiconductor devices widely used for switching and amplification.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "Resistor", listOf("resistor resistance"), """A resistor opposes current; an ideal ohmic resistor follows V = IR.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "Capacitor", listOf("capacitor capacitance"), """A capacitor stores electrical energy in an electric field; ideal capacitance C=Q/V.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "Inductor", listOf("inductor inductance"), """An inductor stores energy in a magnetic field and opposes changes in current.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "LED", listOf("led light emitting diode"), """An LED is a semiconductor diode that emits light through electroluminescence.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "AC", listOf("ac alternating current"), """AC is current whose direction and usually magnitude vary with time.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "DC", listOf("dc direct current"), """DC is current flowing in one direction, though its magnitude may vary.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "Transformer", listOf("transformer electrical"), """A transformer transfers AC electrical energy between circuits through electromagnetic induction, commonly changing voltage/current levels.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Electronics", "PCB", listOf("pcb printed circuit board"), """A PCB mechanically supports components and electrically interconnects them through conductive structures.""", ConfidenceLevel.HIGH, false),
    )
}
