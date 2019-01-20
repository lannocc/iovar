<?xml version="1.0"?>
<!--
    Copyright (C) 2019 Virgo Venture, Inc.
    @%@~LICENSE~@%@
-->
<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    >

    <!--
         Copy XML tree, outputting it as text.
    -->
    <xsl:template name="xml-copy"><xsl:param name="node" select="."/>&lt;<xsl:value-of select="name($node)"/><xsl:for-each select="$node/@*"><xsl:text> </xsl:text><xsl:value-of select="name()"/>="<xsl:value-of select="."/>"</xsl:for-each><xsl:choose>
            <xsl:when test="$node/node()">&gt;<xsl:for-each select="$node/node()"><xsl:choose>
                        <xsl:when test="self::text()"><xsl:value-of select="."/></xsl:when>
                        <xsl:when test="self::comment()">&lt;!--<xsl:value-of select="."/>--&gt;</xsl:when>
                        <xsl:otherwise><xsl:call-template name="xml-copy"/></xsl:otherwise>
            </xsl:choose></xsl:for-each>&lt;/<xsl:value-of select="name($node)"/>&gt;</xsl:when>
            <xsl:otherwise>/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

