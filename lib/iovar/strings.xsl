<?xml version="1.0"?>
<!--
    Copyright (C) 2016-2019 Virgo Venture, Inc.
    @%@~LICENSE~@%@
-->
<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:func="http://exslt.org/functions"
    xmlns:io="http://iovar.net/ns"
    >


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

    <!--
    <func:function name="io:ends-with" as="xs:boolean">
        <xsl:param name="str1" select="''"/>
        <xsl:param name="str2" select="''"/>

        <func:result>
            <xsl:value-of select="substring($str1, string-length($str1) - string-length($str2) + 1) = $str2"/>
        </func:result>
    </func:function>
    -->

    <xsl:variable name="STR_LOWER" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="STR_UPPER" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

    <xsl:template name="string-to-upper">
        <xsl:param name="text"/>

        <xsl:value-of select="translate($text, $STR_LOWER, $STR_UPPER)"/>
    </xsl:template>

    <xsl:template name="string-to-lower">
        <xsl:param name="text"/>

        <xsl:value-of select="translate($text, $STR_UPPER, $STR_LOWER)"/>
    </xsl:template>



    <xsl:template name="string-repeat">
        <xsl:param name="text"/>
        <xsl:param name="count"/>
        <xsl:param name="index" select="0"/>
        <xsl:if test="$index &lt; $count">
            <xsl:value-of select="$text"/>
            <xsl:call-template name="string-repeat">
                <xsl:with-param name="text" select="$text"/>
                <xsl:with-param name="count" select="$count"/>
                <xsl:with-param name="index" select="$index + 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>



</xsl:stylesheet>

