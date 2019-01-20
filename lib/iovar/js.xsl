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
         A simple attempt to get just a single JavaScript function from source code containing many functions.
    -->
    <xsl:template name="js-function">
        <xsl:param name="source" select="''"/>
        <xsl:param name="name"/>function <xsl:value-of select="$name"/> (<xsl:value-of select="substring-before(substring-after($source,concat('function ',$name,' (')),'&#10;}')"/>
}</xsl:template>

</xsl:stylesheet>

