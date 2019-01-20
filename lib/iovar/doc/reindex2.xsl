<?xml version="1.0"?>
<!--
     Copyright (C) 2019 Virgo Venture, Inc.
    @%@~LICENSE~@%@
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:doc="http://docbook.org/ns/docbook"
    >

    <xsl:output
        method="xml"
        media-type="text/xml"
        omit-xml-declaration="no"
        />

    <!-- index title -->
    <xsl:param name="title"/>

    <xsl:template match="text()"/>

    <xsl:key name="all" match="/refs/ref" use="@name"/>
    <xsl:key name="refs_only" match="/refs/ref[not(@type)]" use="@name"/>

    <xsl:template match="/refs">
        <doc:index version="5.1">
            <doc:title><xsl:value-of select="$title"/></doc:title>

            <xsl:for-each select="ref[generate-id() = generate-id(key('all', @name)[1])]">
                <xsl:sort select="@name"/>

                <doc:indexentry>
                    <doc:primaryie>
                        <doc:command><xsl:value-of select="@name"/></doc:command>
                        <xsl:for-each select="key('refs_only', @name)">
                            <xsl:text> </xsl:text><doc:link xlink:href="{@href}">[<xsl:value-of select="position()"/>]</doc:link>
                        </xsl:for-each>
                    </doc:primaryie>
                    <xsl:for-each select="key('all', @name)">
                        <xsl:if test="@type">
                            <doc:secondaryie>
                                <xsl:choose>
                                    <xsl:when test="@type = 'synopsis'">
                                        <doc:link xlink:href="{@href}">Synopsis</doc:link>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <doc:errortext>Unsupported type: <xsl:value-of select="@type"/></doc:errortext>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </doc:secondaryie>
                        </xsl:if>
                    </xsl:for-each>
                </doc:indexentry>
            </xsl:for-each>
        </doc:index>
    </xsl:template>

</xsl:stylesheet>

