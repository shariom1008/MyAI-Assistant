package com.example.myaiassistant.knowledge

class AurixSourceVerifier {

    fun verify(
        sourceName: String?,
        url: String?
    ): SourceType {

        val name = sourceName
            ?.trim()
            ?.lowercase()
            ?: ""

        val link = url
            ?.trim()
            ?.lowercase()
            ?: ""

        // =====================================================
        // OFFICIAL / GOVERNMENT SOURCES
        // =====================================================

        if (
            link.endsWith(".gov") ||
            link.contains(".gov.") ||
            link.contains(".nic.in") ||
            link.contains("who.int") ||
            link.contains("fda.gov") ||
            link.contains("ema.europa.eu") ||
            link.contains("cdc.gov") ||
            link.contains("nih.gov") ||
            link.contains("nasa.gov") ||
            link.contains("isro.gov.in")
        ) {
            return SourceType.GOVERNMENT
        }

        // =====================================================
        // SCIENTIFIC SOURCES
        // =====================================================

        if (
            link.contains("pubmed.ncbi.nlm.nih.gov") ||
            link.contains("ncbi.nlm.nih.gov") ||
            link.contains("nature.com") ||
            link.contains("sciencedirect.com") ||
            link.contains("springer.com") ||
            link.contains("wiley.com") ||
            link.contains("acs.org") ||
            link.contains("rsc.org")
        ) {
            return SourceType.SCIENTIFIC
        }

        // =====================================================
        // EDUCATIONAL SOURCES
        // =====================================================

        if (
            link.endsWith(".edu") ||
            link.contains(".edu.") ||
            link.contains("khanacademy.org") ||
            link.contains("britannica.com") ||
            link.contains("openstax.org")
        ) {
            return SourceType.EDUCATIONAL
        }

        // =====================================================
        // TRUSTED ORGANIZATIONS / INSTITUTIONS
        // =====================================================

        if (
            name.contains("government") ||
            name.contains("ministry") ||
            name.contains("university") ||
            name.contains("institute") ||
            name.contains("official")
        ) {
            return SourceType.TRUSTED
        }

        // =====================================================
        // SECONDARY SOURCES
        // =====================================================

        if (
            link.contains("wikipedia.org") ||
            link.contains("reuters.com") ||
            link.contains("bbc.com") ||
            link.contains("theguardian.com")
        ) {
            return SourceType.SECONDARY
        }

        // =====================================================
        // UNKNOWN
        // =====================================================

        return SourceType.UNKNOWN
    }

    fun verify(source: KnowledgeSource): KnowledgeSource {

        val verifiedType =
            verify(
                sourceName = source.name,
                url = source.url
            )

        return source.copy(
            sourceType = verifiedType
        )
    }

    fun verifyAll(
        sources: List<KnowledgeSource>
    ): List<KnowledgeSource> {

        return sources.map { source ->
            verify(source)
        }
    }
}
