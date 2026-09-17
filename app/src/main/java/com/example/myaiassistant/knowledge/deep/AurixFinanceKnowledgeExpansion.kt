package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixFinanceKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Finance", "Bond", listOf("bond coupon maturity yield", "bond"), """A bond is a debt instrument in which an issuer borrows from investors and typically promises interest payments and repayment according to stated terms.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Balance Sheet", listOf("balance sheet assets liabilities equity", "balance sheet"), """A balance sheet reports assets, liabilities and equity at a point in time, following the accounting relationship assets = liabilities + equity.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Finance", "Net Present Value", listOf("npv net present value discounted cash flow", "net present value", "npv"), """Net present value compares the present value of expected cash inflows and outflows using a chosen discount rate.""", ConfidenceLevel.HIGH, false)
    )
}
