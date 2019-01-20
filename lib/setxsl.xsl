<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- path to stylesheet reference that we'll add (passed in as a parameter) -->
    <xsl:param name="href"/>

    <!--Identity template, provides default behavior that copies all content into the output -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!--More specific template that provides custom behavior -->
    <xsl:template match="/">  
        <xsl:copy>
            <xsl:processing-instruction name="xml-stylesheet">href="<xsl:value-of select="$href"/>" type="text/xsl"</xsl:processing-instruction>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

