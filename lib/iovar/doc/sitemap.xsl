<?xml version="1.0"?>
<!--
     Copyright (C) 2019 Virgo Venture, Inc.
    @%@~LICENSE~@%@
-->
<xsl:stylesheet version="1.0"
    xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:doc="http://docbook.org/ns/docbook"
    >

    <xsl:output
        method="xml"
        media-type="text/xml"
        omit-xml-declaration="no"
        />

    <!-- canonical uri base -->
    <xsl:param name="canon_base"/>


    <xsl:template name="sitemap-url">
        <xsl:param name="href" select="@xlink:href"/>
        <xsl:param name="priority" select="0.5"/>
        <url>
            <loc><xsl:value-of select="$canon_base"/>/<xsl:value-of select="$href"/></loc>
            <changefreq>weekly</changefreq>
            <xsl:if test="substring-after($href,'.') = 'xml'">
                <!-- we use res: here to get file source and NOT auto-exec -->
                <xsl:variable name="target" select="document(concat('res:/doc/iovar/',$href))"/>
                <xsl:choose>
                    <xsl:when test="$target//doc:revhistory">
                        <lastmod><xsl:value-of select="$target//doc:revhistory/doc:revision[last()]/doc:date"/></lastmod>
                    </xsl:when>
                    <xsl:when test="$target//doc:pubdate">
                        <lastmod><xsl:value-of select="$target//doc:pubdate"/></lastmod>
                    </xsl:when>
                </xsl:choose>
            </xsl:if>
            <priority><xsl:value-of select="$priority"/></priority>
        </url>
    </xsl:template>


    <xsl:template match="/doc:toc">
        <urlset>
            <xsl:call-template name="sitemap-url">
                <xsl:with-param name="href" select="'index.xml'"/>
                <xsl:with-param name="priority" select="0.7"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </urlset>
    </xsl:template>

    <xsl:template match="doc:tocentry/doc:link">
        <xsl:call-template name="sitemap-url"/>
    </xsl:template>

    <xsl:template match="text()"/>

</xsl:stylesheet>

