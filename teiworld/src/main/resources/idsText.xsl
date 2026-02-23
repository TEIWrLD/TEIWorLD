<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="tei"
    version="2.0">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:param name="Rights" select="''"/>
    <xsl:param name="korpusSigle" select="''"/>
    <xsl:param name="docSigle" select="''"/>
    <xsl:param name="textSigle" select="''"/>
    <xsl:param name="textTitle" select="''"/>
    <xsl:variable name="concatenated-sigle">
        <xsl:value-of select="$korpusSigle"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="$docSigle"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="$textSigle"/>
    </xsl:variable>
    <xsl:param name="creationDate" select="format-date(current-date(), '[Y].[M01].[D01]')"/>

    <xsl:template match="/">
        <idsText version="1.0">
            <xsl:call-template name="idsHeader"/>
            <text>
                <body>
                    <div n="0" complete="y" type="section">
                        <xsl:for-each select="tei:TEI/tei:text/tei:body/*|tei:TEI/tei:text/tei:body/tei:div/*">
                            <xsl:choose>
                                <xsl:when test="self::tei:p">
                                    <p>
                                        <xsl:apply-templates mode="main-text"/>
                                    </p>
                                </xsl:when>
                                <xsl:when test="self::tei:head">
                                    <p><s><xsl:value-of select="."/></s></p>
                                </xsl:when>
                                <xsl:when test="self::tei:list">
                                    <list>
                                        <xsl:for-each select="@*">
                                            <xsl:choose>
                                                <xsl:when test="name()='rend' and .='bulleted'">
                                                    <xsl:attribute name="type">
                                                        <xsl:value-of select="'ul'"/>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <!-- gibt es (mehrere) Alternativen? -->
                                                    <xsl:attribute name="{name()}">
                                                        <xsl:value-of select="."/>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                        <xsl:for-each select="tei:item">
                                            <xsl:element name="item">
                                                <s>
                                                    <xsl:value-of select="."/>
                                                </s>
                                            </xsl:element>
                                        </xsl:for-each>
                                    </list>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:for-each>
                    </div>
                </body>
                <back>
                    <div n="0" type="footnotes">
                        <xsl:apply-templates select="//tei:note[@place='foot']"/>
                    </div>
                </back>
            </text>
        </idsText>
    </xsl:template>

    <!-- template to tokenize paragraphs into sentences -->
    <xsl:template match="text()" mode="main-text">
        <xsl:variable name="text-with-token" select="replace(., '([.!?])\s', '$1||')"/>
        <xsl:for-each select="tokenize($text-with-token, '\|\|')">
            <s>
                <xsl:value-of select="normalize-space(.)"/>
            </s>
        </xsl:for-each>
    </xsl:template>

    <!-- template for ref elements -->
    <xsl:template match="tei:ref" mode="main-text">
        <ref target="{@target}">
            <xsl:value-of select="tei:hi"/>
        </ref>
    </xsl:template>

    <xsl:template match="tei:hi"  mode="main-text">
        <xsl:choose>
            <!-- no hi element for colors -->
            <xsl:when test="starts-with(@rend, 'color')">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <!-- comments in word file -->
                    <xsl:when test="@rend='annotation_reference'">
                        <note>
                            <xsl:value-of select="."/>
                        </note>
                    </xsl:when>
                    <!-- rend attribute -->
                    <xsl:when test="@rend">
                        <hi>
                            <xsl:attribute name="rend">
                                <xsl:choose>
                                    <!-- short form for value of rend attribute -->
                                    <xsl:when test="@rend='bold'">
                                        <xsl:value-of select="'bo'"/>
                                    </xsl:when>
                                    <xsl:when test="@rend='italic'">
                                        <xsl:value-of select="'it'"/>
                                    </xsl:when>
                                    <xsl:when test="@rend='underline'">
                                        <xsl:value-of select="'un'"/>
                                    </xsl:when>
                                    <xsl:when test="@rend='strikethrough'">
                                        <xsl:value-of select="'st'"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="@rend"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:value-of select="."/>
                        </hi>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- template for pointers in the main text -->
    <xsl:template match="tei:note[@place='foot']" mode="main-text">
        <ptr target="{concat(ancestor::tei:TEI/@xml:id, $concatenated-sigle, '-f', @n)}" rend="ref" targType="note" targOrder="u"/>
    </xsl:template>

    <!-- template for footnotes -->
    <xsl:template match="tei:note[@place='foot']">
        <note id="{concat(ancestor::tei:TEI/@xml:id, $concatenated-sigle, '-f', @n)}" place="foot">
            <ref>
                <xsl:choose>
                    <xsl:when test="matches(normalize-space(replace(., '^,\\s*', '')), '^(http://|https://|www\.)')">
                        <xsl:attribute name="target">
                            <xsl:value-of select="normalize-space(replace(., '^,\\s*', ''))"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:when test="descendant::tei:ptr/@target and matches(normalize-space(replace(descendant::tei:ptr/@target, '^,\\s*', '')), '^(http://|https://|www\\.)')">
                        <xsl:attribute name="target">
                            <xsl:value-of select="normalize-space(replace(descendant::tei:ptr/@target, '^,\\s*', ''))"/>
                        </xsl:attribute>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="descendant::tei:ref">
                        <xsl:value-of select="descendant::tei:ref/tei:hi"/>
                    </xsl:when>
                    <xsl:when test="descendant::tei:hi">
                        <xsl:value-of select="descendant::tei:hi"/>
                    </xsl:when>
                    <xsl:when test="descendant::tei:ptr">
                        <xsl:value-of select="descendant::tei:ptr/@target"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="normalize-space(.)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </ref>
        </note>
    </xsl:template>

    <!-- template for header -->
    <xsl:template name="idsHeader">
        <idsHeader type="text" pattern="text" version="1.0">
            <fileDesc>
                <titleStmt>
                    <textSigle>
                        <xsl:value-of select="$korpusSigle"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="$docSigle"/>
                        <xsl:text>.</xsl:text>
                        <xsl:value-of select="$textSigle"/>
                    </textSigle>
                    <t.title assemblage="external">
                        <xsl:value-of select="$textTitle"/>
                    </t.title>
                </titleStmt>
                <publicationStmt>
                    <distributor/>
                    <pubAddress/>
                    <availability region="world" status="unknown">
                        <xsl:value-of select="$Rights"/>
                    </availability>
                    <pubDate/>
                </publicationStmt>
                <sourceDesc>
                    <biblStruct>
                        <analytic>
                            <h.title type="main"><xsl:value-of select="tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title"/></h.title>
                            <h.author><xsl:value-of select="tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:author"/></h.author>
                            <imprint>
                                <pubPlace>
                                    <ref type="page_url" target=""/>
                                </pubPlace>
                            </imprint>
                            <idno type=""></idno>
                        </analytic>
                        <monogr>
                            <h.title type="main"/>
                            <imprint/>
                        </monogr>
                    </biblStruct>
                </sourceDesc>
            </fileDesc>
            <encodingDesc/>
            <profileDesc>
                <creation>
                    <creatDate><xsl:value-of select="tei:TEI/tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:edition/tei:date"/></creatDate>
                </creation>
                <textDesc>
                    <textTypeArt></textTypeArt>
                    <textDomain></textDomain>
                </textDesc>
            </profileDesc>
        </idsHeader>
    </xsl:template>

</xsl:stylesheet>