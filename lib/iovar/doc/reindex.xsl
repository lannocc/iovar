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
        omit-xml-declaration="yes"
        />

    <!-- source file path -->
    <xsl:param name="source"/>

    <!-- type (command, function, etc.) -->
    <xsl:param name="type"/>

    <!-- role/language (iosh, ant, javascript, etc.) -->
    <xsl:param name="role"/>

    <xsl:template match="text()"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$type = 'command'">
                <xsl:for-each select="//doc:command">
                    <xsl:variable name="name" select="normalize-space(text())"/>
                    <xsl:choose>
                        <xsl:when test="local-name(..) = 'cmdsynopsis'">
                            <xsl:if test="@role = $role or ../@role = $role">
                                <ref name="{$name}" href="{$source}" type="synopsis"/>
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="not(//doc:cmdsynopsis/doc:command[text()=$name])">
                            <xsl:if test="@role = $role">
                                <ref name="{$name}" href="{$source}#command-{position()}"/>
                            </xsl:if>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="$type = 'function'">
                <xsl:for-each select="//doc:function">
                    <xsl:variable name="name" select="normalize-space(text())"/>
                    <xsl:choose>
                        <xsl:when test="local-name(..) = 'funcdef' and local-name(../..) = 'funcprototype' and local-name(../../..) = 'funcsynopsis'">
                            <xsl:if test="@role = $role or ../../../@language = $role">
                                <ref name="{$name}" href="{$source}" type="synopsis"/>
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="not(//doc:funcsynopsis/doc:funcprototype/doc:funcdef/doc:function[text()=$name])">
                            <xsl:if test="@role = $role">
                                <ref name="{$name}" href="{$source}#function-{position()}"/>
                            </xsl:if>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <error>Unsupported type: '<xsl:value-of select="$type"/>'</error>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

