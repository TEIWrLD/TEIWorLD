<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs tei"
    version="2.0">
    <xsl:output omit-xml-declaration="yes" method="xml" encoding="UTF-8"
        exclude-result-prefixes="#all" indent="yes"/>
    
    <xsl:param name="creationDate" select="format-date(current-date(), '[Y]-[M01]')"/>
    <xsl:variable name="Day" select="substring($Date, 1, 2)"/>
    <xsl:variable name="Month" select="substring($Date, 4, 2)"/>
    <xsl:variable name="Year" select="substring($Date, 7)"/>
    <xsl:param name="korpusSigle" select="''"/>
    <xsl:param name="Creator" select="''"/>
    <xsl:param name="Title" select="''"/>
    <xsl:param name="Publisher" select="''"/>
    <xsl:param name="PublisherYear" select="''"/>
    <xsl:param name="ResourceType" select="''"/>
    <xsl:param name="Subject" select="''"/>
    <xsl:param name="Contributor" select="''"/>
    <xsl:param name="Date" select="'18.11.2024'"/>
    <xsl:param name="Language" select="''"/>
    <xsl:param name="LanguageCode" select="''"/>
    <xsl:param name="Size" select="''"></xsl:param>
    <xsl:param name="Version" select="''"/>
    <xsl:param name="Rights" select="''"/>
    <xsl:param name="Description" select="''"/>
    <xsl:param name="Geolocation" select="''"/>
    <xsl:param name="FundingsReference" select="''"/>
    
    <xsl:template match="/">
        <idsHeader type="corpus" pattern="allesaußerZtg/Zschr" version="{$Version}">
            <fileDesc>
                <titleStmt>
                    <korpusSigle>
                        <xsl:value-of select="$korpusSigle"/>
                    </korpusSigle>
                    <c.title>
                        <xsl:value-of select="$Title"/>
                    </c.title>
                    <respStmt>
                        <resp>Creator</resp>
                        <persName><xsl:value-of select="$Creator"/></persName>
                    </respStmt>
                    <respStmt>
                        <resp>Contributor</resp>
                        <persName><xsl:value-of select="$Contributor"/></persName>
                    </respStmt>
                    <funder>
                        <xsl:value-of select="$FundingsReference"/>
                    </funder>
                </titleStmt>
                <editionStmt version="1.0"/>
                <extent><xsl:value-of select="$Size"/></extent>
                <publicationStmt>
                    <distributor></distributor>
                    <pubAddress></pubAddress>
                    <telephone></telephone>
                    <eAddress type="www"></eAddress>
                    <eAddress type="www"></eAddress>
                    <eAddress type="email"></eAddress>
                    <availability region="world" status="unknown">
                        <xsl:value-of select="$Rights"/>
                    </availability>
                    <pubDate type="year">
                        <xsl:value-of select="$PublisherYear"/>
                    </pubDate>
                </publicationStmt>
                <sourceDesc>
                    <biblStruct>
                        <monogr>
                            <h.title type="main">
                                <xsl:value-of select="$Title"/>
                            </h.title>
                            <h.author/>
                            <editor/>
                            <edition>
                                <further></further>
                                <kind/>
                                <appearance/>
                            </edition>
                            <imprint>
                                <publisher>
                                    <xsl:value-of select="$Publisher"/>
                                </publisher>
                                <pubPlace><xsl:value-of select="$Geolocation"/></pubPlace>
                                <pubDate type="year"><xsl:value-of select="$Year"/></pubDate>
                                <pubDate type="month"><xsl:value-of select="$Month"/></pubDate>
                                <pubDate type="day"><xsl:value-of select="$Day"/></pubDate>
                            </imprint>
                        </monogr>
                    </biblStruct>
                </sourceDesc>
            </fileDesc>
            <encodingDesc>
                <projectDesc>
                    <xsl:value-of select="$Description"/>
                </projectDesc>
                <editorialDecl>
                    <conformance>This document conforms to I5 (see http://jtei.revues.org/508)</conformance>
                    <xsl:element name="transduction">
                        <xsl:attribute name="n">1</xsl:attribute>
                        <xsl:text>Conversion to TEI P5 with TEIGarage</xsl:text>
                    </xsl:element>
                    <transduction n="2">
                        <xsl:text>Conversion from TEI P5 to I5: IDS, </xsl:text><xsl:value-of select="format-date(current-date(), '[MNn] [Y]')"/>
                    </transduction>
                </editorialDecl>
                <classDecl>
                    <taxonomy id="topic">
                        <h.bibl>None</h.bibl>
                    </taxonomy>
                </classDecl>
            </encodingDesc>
            <profileDesc>
                <langUsage>
                    <language id="{$LanguageCode}"><xsl:value-of select="$Language"/></language>
                </langUsage>
                <textDesc>
                    <textType>
                        <xsl:value-of select="$ResourceType"/>
                    </textType>
                    <textTypeRef/>
                    <textDomain><xsl:value-of select="$Subject"/></textDomain>
                </textDesc>
            </profileDesc>
            <revisionDesc>
                <listChange>
                    <change when="{$creationDate}" who="Conversion Pipeline">
                        <xsl:text>generated from TEI P5</xsl:text>
                    </change>
                </listChange>
            </revisionDesc>
        </idsHeader> 
    </xsl:template>
    
</xsl:stylesheet>