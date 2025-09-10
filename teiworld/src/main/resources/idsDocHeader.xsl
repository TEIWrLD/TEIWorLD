<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="UTF-8"
        exclude-result-prefixes="#all" indent="yes"/>
    
    <xsl:param name="Rights" select="''"/>
    <xsl:param name="korpusSigle" select="''"/>
    <xsl:param name="docSigle" select="''"/>
    
    <xsl:template match="/">
        <idsHeader type="document" pattern="text" version="1.0">
            <fileDesc>
                <titleStmt>
                    <dokumentSigle>
                        <!-- projektspezifisch -->
                        <xsl:value-of select="$korpusSigle"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="$docSigle"/>
                    </dokumentSigle>
                    <d.title></d.title>
                </titleStmt>
                <publicationStmt>
                    <distributor/>
                    <pubAddress/>
                    <availability region="world" status="unknown">
                        <!-- projektspezifisch -->
                        <xsl:value-of select="$Rights"/>
                    </availability>
                    <pubDate/>
                </publicationStmt>
                <sourceDesc>
                    <biblStruct>
                        <monogr>
                            <h.title type="main"/>
                            <imprint/>
                        </monogr>
                    </biblStruct>
                </sourceDesc>
            </fileDesc>
        </idsHeader>
    </xsl:template>    
    
</xsl:stylesheet>