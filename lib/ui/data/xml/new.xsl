<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xxx="http://www.w3.org/1999/XSL/XXXTransformXXX"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:ui="http://iovar.com/ui"
    >

    <xsl:output
        method="xml"
        media-type="text/xml"
        omit-xml-declaration="no"
        indent="yes"
        />

    <xsl:template match="/">

        <xxx:stylesheet version="1.0">

            <xxx:output
                method="xml"
                media-type="text/xml"
                omit-xml-declaration="no"
                />

            <xsl:apply-templates/>

            <xxx:template match="@*|node()">
                <xxx:copy>
                    <xxx:apply-templates select="@*|node()"/>
                </xxx:copy>
            </xxx:template>

        </xxx:stylesheet>

    </xsl:template> 


    <xsl:template match="*"/>

</xsl:stylesheet>

