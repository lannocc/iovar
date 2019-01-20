<?xml version="1.0"?>
<!--
    Copyright (C) 2019 Virgo Venture, Inc.
    @%@~LICENSE~@%@
-->
<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:func="http://exslt.org/functions"
    xmlns:io="http://iovar.net/ns"
    >


    <xsl:template name="date-text-from-digits">
        <xsl:param name="date" select="."/>
        <xsl:call-template name="date-month">
            <xsl:with-param name="date" select="substring-before(substring-after($date,'-'),'-')"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring-after(substring-after($date,'-'),'-')"/>, <xsl:value-of select="substring-before($date,'-')"/>
    </xsl:template>

    <xsl:template name="date-month">
        <xsl:param name="date"/>
        <xsl:choose>
            <xsl:when test="$date = '01' or $date = '1'">January</xsl:when>
            <xsl:when test="$date = '02' or $date = '2'">February</xsl:when>
            <xsl:when test="$date = '03' or $date = '3'">March</xsl:when>
            <xsl:when test="$date = '04' or $date = '4'">April</xsl:when>
            <xsl:when test="$date = '05' or $date = '5'">May</xsl:when>
            <xsl:when test="$date = '06' or $date = '6'">June</xsl:when>
            <xsl:when test="$date = '07' or $date = '7'">July</xsl:when>
            <xsl:when test="$date = '08' or $date = '8'">August</xsl:when>
            <xsl:when test="$date = '09' or $date = '9'">September</xsl:when>
            <xsl:when test="$date = '10'">October</xsl:when>
            <xsl:when test="$date = '11'">November</xsl:when>
            <xsl:when test="$date = '12'">December</xsl:when>
            <xsl:otherwise>???</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

