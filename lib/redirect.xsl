<?xml version="1.0"?>
<!--
     Copyright (C) 2017 Virgo Venture, Inc.
     @%@~LICENSE~@%@
-->
<xsl:stylesheet version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    >

    <xsl:output
        method="xml"
        media-type="application/xhtml+xml"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        omit-xml-declaration="no"
        />

    <xsl:template match="/postdata">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="postdata/*">
        <input type="hidden" name="{local-name()}" value="{.}"/>
    </xsl:template>

</xsl:stylesheet>

