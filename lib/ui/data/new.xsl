<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ui="http://iovar.com/ui"
    >

    <xsl:output
        method="text"
        media-type="text/plain"
        />

    <xsl:param name="name"/>
    <xsl:param name="bin"/>

    <xsl:template match="/">#!/bin/iosh

        MODULE=data/<xsl:value-of select="$name"/>-new

        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="/*">
        get <xsl:value-of select="@ui:data"/> <xsl:apply-templates/> | xslt /bin/data/<xsl:value-of select="$name"/>/link.xsl | xlink | view
    </xsl:template>

    <xsl:template match="*[@ui:delete]"/>
    <xsl:template match="*[@ui:new]"> '<xsl:call-template name="xml2db"><xsl:with-param name="xml" select="local-name()"/></xsl:call-template>' </xsl:template>

    <xsl:template match="text()"/>



    <xsl:template name="xml2db">
        <xsl:param name="xml"/>

        <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$xml"/>
            <xsl:with-param name="replace" select="'-'"/>
            <xsl:with-param name="with" select="' '"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="string-replace-all">
        <xsl:param name="text" />
        <xsl:param name="replace" />
        <xsl:param name="with" />

        <xsl:choose>
            <xsl:when test="contains($text, $replace)">
                <xsl:value-of select="substring-before($text,$replace)" />
                <xsl:value-of select="$with" />
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="text" select="substring-after($text,$replace)" />
                    <xsl:with-param name="replace" select="$replace" />
                    <xsl:with-param name="with" select="$with" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

